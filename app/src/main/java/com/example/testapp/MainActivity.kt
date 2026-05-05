package com.example.testapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.testapp.game.GameViewModel
import com.example.testapp.services.PoseDetectorService
import com.example.testapp.ui.theme.RyRoTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Giữ màn hình luôn sáng khi đang chơi
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            RyRoTheme {
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted ->
                        hasCameraPermission = granted
                    }
                )

                LaunchedEffect(Unit) {
                    if (!hasCameraPermission) {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                }

                if (hasCameraPermission) {
                    val activeGame = viewModel.activeGame
                    if (activeGame == null) {
                        GameListScreen(viewModel)
                    } else {
                        FruitNinjaGame(viewModel, cameraExecutor)
                    }
                } else {
                    Text(stringResource(R.string.camera_permission_denied))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun GameListScreen(viewModel: GameViewModel) {
    var screenSize by remember { mutableStateOf(Size(0, 0)) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        // Invisible canvas to detect screen size
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (screenSize.width == 0) {
                screenSize = Size(size.width.toInt(), size.height.toInt())
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "RyRo Games",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.availableGames) { game ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clickable { 
                                if (screenSize.width > 0) {
                                    viewModel.selectGame(game, screenSize.width, screenSize.height)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = game.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.Yellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FruitNinjaGame(viewModel: GameViewModel, executor: java.util.concurrent.ExecutorService) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    var screenSize by remember { mutableStateOf(Size(0, 0)) }
    val activeGame = viewModel.activeGame ?: return

    // Phản hồi rung khi bất kỳ ai có điểm
    val totalScore = activeGame.players.sumOf { it.score }
    LaunchedEffect(totalScore) {
        if (totalScore > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val poseDetectorService = PoseDetectorService(context) { poses ->
                        if (screenSize.width > 0) {
                            // Phân loại poses dựa trên vị trí X để gán cho Player 1 và Player 2
                            // Người đứng bên trái (X nhỏ) và người đứng bên phải (X lớn)
                            val sortedPoses = poses.sortedBy { it.centerX }
                            
                            viewModel.updateMultiPlayerPoses(
                                p1Pose = sortedPoses.getOrNull(0),
                                p2Pose = sortedPoses.getOrNull(1),
                                width = screenSize.width.toFloat(),
                                height = screenSize.height.toFloat()
                            )
                        }
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().also { it.setAnalyzer(executor, poseDetectorService) }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis)
                    } catch (e: Exception) { e.printStackTrace() }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // 2. Game Rendering Layer
        val textMeasurer = rememberTextMeasurer()
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (screenSize.width == 0) {
                screenSize = Size(size.width.toInt(), size.height.toInt())
            }
            // Vẽ trò chơi hiện tại
            activeGame.draw(this, textMeasurer)
        }

        // 3. UI Overlay (HUD Multiplayer)
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            // Top HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Bảng điểm danh sách người chơi
                Column {
                    Text(
                        text = activeGame.name,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    activeGame.players.forEach { player ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(player.color, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "${player.name}: ${player.score}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                
                // Nút Exit
                IconButton(
                    onClick = { viewModel.exitGame() },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Red.copy(alpha = 0.6f), RoundedCornerShape(25.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit Game",
                        tint = Color.White
                    )
                }
            }

            // Instruction Overlay
            if (viewModel.leftWrist == null && viewModel.rightWrist == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.instruction_text),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(16.dp)
                    )
                }
            }
        }
    }
}

