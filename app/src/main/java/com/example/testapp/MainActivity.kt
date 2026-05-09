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
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.testapp.game.GameViewModel
import com.example.testapp.game.core.AgeGroup
import com.example.testapp.game.core.CategoryType
import com.example.testapp.game.core.GameLogic
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
                        GameListScreen(viewModel)
                    } else {
                        GameContainer(viewModel, cameraExecutor)
                    }
                }
            }
        }
    }
}

@Composable
fun GameListScreen(viewModel: GameViewModel) {
    var screenSize by remember { mutableStateOf(Size(0, 0)) }
    val backgroundColor = when(viewModel.selectedCategory) {
        CategoryType.EXERCISE -> PastelOrange
        CategoryType.LEARNING -> PastelBlue
        CategoryType.SCIENCE -> PastelGreen
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) { if (screenSize.width == 0) screenSize = Size(size.width.toInt(), size.height.toInt()) }

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Chào bé yêu! 👋", color = TextGray, fontSize = 20.sp)
                    Text("HÀNH TINH TRI THỨC", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Black)
                }
                
                Row(modifier = Modifier.background(Color.Black.copy(0.05f), RoundedCornerShape(32.dp)).padding(4.dp)) {
                    AgeTabButton("Bé 2 Tuổi", viewModel.selectedAge == AgeGroup.TODDLER) { viewModel.selectedAge = AgeGroup.TODDLER }
                    AgeTabButton("Bé 5 Tuổi", viewModel.selectedAge == AgeGroup.PRESCHOOL) { viewModel.selectedAge = AgeGroup.PRESCHOOL }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Categories
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                CategoryButton("VẬN ĐỘNG", "🎮", DeepOrange, viewModel.selectedCategory == CategoryType.EXERCISE) { viewModel.selectedCategory = CategoryType.EXERCISE }
                CategoryButton("HỌC TẬP", "📚", DeepBlue, viewModel.selectedCategory == CategoryType.LEARNING) { viewModel.selectedCategory = CategoryType.LEARNING }
                CategoryButton("KHOA HỌC", "🔬", DeepGreen, viewModel.selectedCategory == CategoryType.SCIENCE) { viewModel.selectedCategory = CategoryType.SCIENCE }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Games
            val filtered = viewModel.availableGames.filter { it.targetAge == viewModel.selectedAge && it.category == viewModel.selectedCategory }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filtered) { game ->
                    GameCard(game) { viewModel.selectGame(game, screenSize.width, screenSize.height) }
                }
            }
        }
    }
}

@Composable
fun GameContainer(viewModel: GameViewModel, executor: java.util.concurrent.ExecutorService) {
    val context = LocalContext.current
    val activeGame = viewModel.activeGame ?: return
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = Modifier.fillMaxSize()) {
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
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.White))
        }

        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures { activeGame.onTouch(it.x, it.y) }
        }) {
            activeGame.draw(this, textMeasurer)
        }

        IconButton(onClick = { viewModel.exitGame() }, modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(56.dp).background(Color.Red.copy(0.7f), RoundedCornerShape(28.dp))) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Exit", tint = Color.White)
        }
    }
}

@Composable
fun AgeTabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(28.dp)).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(text, color = if (isSelected) Color.Black else TextGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryButton(name: String, icon: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.alpha(if (isSelected) 1f else 0.4f)) {
        Box(modifier = Modifier.size(100.dp).background(color, RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 48.sp)
        }
        Text(name, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun GameCard(game: GameLogic, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.9f))) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(game.icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(game.name, color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(if (game.isAR) "Vận động cùng AI" else "Khám phá cảm ứng", color = TextGray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("BẮT ĐẦU ✨", color = DeepOrange, fontWeight = FontWeight.Bold)
        }
    }
}
