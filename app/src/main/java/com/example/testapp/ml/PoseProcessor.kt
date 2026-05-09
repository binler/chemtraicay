package com.example.testapp.ml

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.testapp.services.PoseDetectorService
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PoseProcessor quản lý vòng đời nhận diện và truyền dữ liệu qua StateFlow.
 * Tuân thủ mục 3 trong Spec.
 */
class PoseProcessor(context: Context) : ImageAnalysis.Analyzer {

    private val _poseData = MutableStateFlow<List<PoseDetectorService.PoseResult>>(emptyList())
    val poseData: StateFlow<List<PoseDetectorService.PoseResult>> = _poseData.asStateFlow()

    private var poseLandmarker: PoseLandmarker? = null

    init {
        val baseOptions = BaseOptions.builder().setModelAssetPath("pose_landmarker_lite.task").build()
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(2)
            .setResultListener { result, _ ->
                val poses = result.landmarks().map { landmarks ->
                    val getPt = { id: Int -> 
                        if (landmarks.size > id) PoseDetectorService.Point(landmarks[id].x(), landmarks[id].y()) 
                        else null 
                    }
                    PoseDetectorService.PoseResult(
                        leftShoulder = getPt(11),
                        rightShoulder = getPt(12),
                        leftWrist = getPt(15),
                        rightWrist = getPt(16),
                        leftHip = getPt(23),
                        rightHip = getPt(24),
                        centerX = landmarks.map { it.x() }.average().toFloat()
                    )
                }
                _poseData.value = poses
            }
            .build()
        
        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val frameTime = System.currentTimeMillis()
        val bitmap = imageProxy.toBitmap()
        // Tối ưu: Chỉ lật bitmap một lần duy nhất tại đây
        val matrix = android.graphics.Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
        val mirroredBitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        
        val mpImage = BitmapImageBuilder(mirroredBitmap).build()
        poseLandmarker?.detectAsync(mpImage, frameTime)
        imageProxy.close()
    }

    fun stop() {
        poseLandmarker?.close()
    }
}
