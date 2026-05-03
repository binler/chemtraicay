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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
                    FruitNinjaGame(viewModel, cameraExecutor)
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
fun FruitNinjaGame(viewModel: GameViewModel, executor: java.util.concurrent.ExecutorService) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    var screenSize by remember { mutableStateOf(Size(0, 0)) }

    // Rung khi điểm số thay đổi (chém trúng)
    LaunchedEffect(viewModel.score) {
        if (viewModel.score > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            // SỐ 4: ÂM THANH (AUDIO)
            // Bạn có thể thêm MediaPlayer.create(context, R.raw.slice_sound).start() ở đây
        }
    }
    
    // SỐ 4: ÂM THANH KHI TRÚNG BOM
    LaunchedEffect(viewModel.comboCount) {
        if (viewModel.comboCount == 0 && viewModel.score > 0) {
            // Bom nổ hoặc mất combo
            println("Feedback: Hit bomb or lost combo")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
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
                    
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val poseDetectorService = PoseDetectorService { result ->
                        // Scale normalized coordinates to screen size
                        if (screenSize.width > 0) {
                            viewModel.leftWrist = result.leftWrist?.let { 
                                PoseDetectorService.Point(it.x * screenSize.width, it.y * screenSize.height)
                            }
                            viewModel.rightWrist = result.rightWrist?.let { 
                                PoseDetectorService.Point(it.x * screenSize.width, it.y * screenSize.height)
                            }
                        }
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor, poseDetectorService)
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Game Logic Loop & UI
        val textMeasurer = rememberTextMeasurer()
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (screenSize.width == 0) {
                screenSize = Size(size.width.toInt(), size.height.toInt())
                viewModel.startGame(screenSize.width, screenSize.height)
            }

            // 1. Vẽ Trail (Vệt kiếm)
            drawTrail(viewModel.leftTrail, Color.Cyan)
            drawTrail(viewModel.rightTrail, Color.Magenta)

            // 2. Vẽ các hạt bắn tung tóe (SỐ 3: SPLATTER)
            viewModel.particles.forEach { p ->
                drawCircle(
                    color = Color(p.color).copy(alpha = p.alpha),
                    radius = p.size,
                    center = androidx.compose.ui.geometry.Offset(p.x, p.y)
                )
            }

            // 3. Vẽ trái cây/Bom chưa bị chém (SỐ 1: BOM)
            viewModel.fruits.forEach { fruit ->
                if (!fruit.isSliced) {
                    val emojiSize = if (fruit.isBomb) 120.sp else 100.sp
                    val textLayoutResult = textMeasurer.measure(
                        text = fruit.emoji,
                        style = TextStyle(fontSize = emojiSize)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            fruit.x - textLayoutResult.size.width / 2,
                            fruit.y - textLayoutResult.size.height / 2
                        )
                    )
                }
            }

            // 4. Vẽ các nửa trái cây đã bị chém
            viewModel.slicedFruits.forEach { half ->
                val emojiSize = 100.sp
                val textLayoutResult = textMeasurer.measure(
                    text = half.emoji,
                    style = TextStyle(fontSize = emojiSize)
                )
                
                withTransform({
                    translate(half.x, half.y)
                    rotate(half.rotation)
                    // Clip một nửa emoji để tạo cảm giác bị chém đôi
                    if (half.isLeft) {
                        clipRect(
                            left = -100f, top = -100f, right = 0f, bottom = 100f
                        )
                    } else {
                        clipRect(
                            left = 0f, top = -100f, right = 100f, bottom = 100f
                        )
                    }
                }) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            -textLayoutResult.size.width.toFloat() / 2,
                            -textLayoutResult.size.height.toFloat() / 2
                        ),
                        alpha = half.alpha
                    )
                }
            }

            // 5. Vẽ COMBO (SỐ 2: COMBO)
            if (viewModel.comboCount >= 2) {
                val comboTextValue = "COMBO X${viewModel.comboCount}!"
                val comboLayout = textMeasurer.measure(
                    text = comboTextValue,
                    style = TextStyle(
                        fontSize = 40.sp, 
                        fontWeight = FontWeight.Black,
                        color = Color.Yellow
                    )
                )
                drawText(
                    textLayoutResult = comboLayout,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width / 2 - comboLayout.size.width / 2, 200f)
                )
            }

            // 4. Vẽ điểm cổ tay hiện tại (Lưỡi kiếm)
            viewModel.leftWrist?.let {
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = androidx.compose.ui.geometry.Offset(it.x, it.y)
                )
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.3f),
                    radius = 60f,
                    center = androidx.compose.ui.geometry.Offset(it.x, it.y)
                )
            }
            viewModel.rightWrist?.let {
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = androidx.compose.ui.geometry.Offset(it.x, it.y)
                )
                drawCircle(
                    color = Color.Magenta.copy(alpha = 0.3f),
                    radius = 60f,
                    center = androidx.compose.ui.geometry.Offset(it.x, it.y)
                )
            }
        }

        // Score & Feedback
        Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text(
                text = stringResource(R.string.score_label, viewModel.score),
                color = Color.Yellow,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (viewModel.leftWrist == null && viewModel.rightWrist == null) {
                Text(
                    text = stringResource(R.string.instruction_text),
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 80.dp)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrail(
    trail: List<PoseDetectorService.Point>,
    color: Color
) {
    if (trail.size < 2) return
    for (i in 0 until trail.size - 1) {
        val start = trail[i]
        val end = trail[i + 1]
        val alpha = (i.toFloat() / trail.size) * 0.8f
        val strokeWidth = (i.toFloat() / trail.size) * 30f
        
        drawLine(
            color = color.copy(alpha = alpha),
            start = androidx.compose.ui.geometry.Offset(start.x, start.y),
            end = androidx.compose.ui.geometry.Offset(end.x, end.y),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
