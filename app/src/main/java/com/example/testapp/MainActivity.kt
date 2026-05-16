package com.example.testapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.testapp.game.GameViewModel
import com.example.testapp.ui.components.KidTopBar
import com.example.testapp.ui.screens.KnowledgePlanetScreen
import com.example.testapp.ui.theme.*
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            val context = LocalContext.current
            LaunchedEffect(Unit) { viewModel.initServices(context) }

            RyRoTheme {
                var hasCameraPermission by remember {
                    mutableStateOf(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                }

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }

                LaunchedEffect(Unit) { if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (viewModel.activeGame == null) {
                        KnowledgePlanetScreen(viewModel) { game, w, h ->
                            viewModel.speak("Cùng chơi " + game.name)
                            viewModel.selectGame(context, game, w, h)
                        }
                    } else {
                        GameContainer(viewModel, cameraExecutor)
                    }
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
fun GameContainer(viewModel: GameViewModel, executor: java.util.concurrent.ExecutorService) {
    val context = LocalContext.current
    val activeGame = viewModel.activeGame ?: return
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
        // LAYER 0: CAMERA/AI
        if (activeGame.isAR) {
            AndroidView(factory = { ctx ->
                PreviewView(ctx).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
            }, modifier = Modifier.fillMaxSize(), update = { view ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build().also { it.setAnalyzer(executor, viewModel.getPoseAnalyzer()!!) }
                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
                    } catch (e: Exception) { e.printStackTrace() }
                }, ContextCompat.getMainExecutor(context))
            })
        }

        // LAYER 1: CONTENT/GAME
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures { activeGame.onTouch(it.x, it.y) }
        }) {
            activeGame.draw(this, textMeasurer)
        }

        // LAYER 2: UI OVERLAY
        Column(modifier = Modifier.fillMaxSize()) {
            KidTopBar(
                title = activeGame.name.uppercase(),
                containerColor = SkyBlue.copy(alpha = 0.8f)
            )
            
            Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                // Nút Exit 3D (Z-Index Cao)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(80.dp)
                        .clickable { 
                            viewModel.speak("Nghỉ xíu con nhé!")
                            viewModel.exitGame() 
                        }
                        .background(SoftPink, RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, "Exit", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                
                // Điểm số nổi bật (Phong cách Khan Academy)
                activeGame.players.firstOrNull()?.let { player ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(SunnyYellow, RoundedCornerShape(32.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "GIỎI QUÁ: ${player.score}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextDark,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
