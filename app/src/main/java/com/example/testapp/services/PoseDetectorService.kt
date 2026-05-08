package com.example.testapp.services

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Service nâng cấp sử dụng MediaPipe Pose Landmarker để nhận diện NHIỀU NGƯỜI (Multi-pose).
 * Lưu ý: Bạn cần tải tệp 'pose_landmarker_lite.task' vào thư mục 'app/src/main/assets/'
 */
class PoseDetectorService(
    context: Context,
    private val onMultiPoseDetected: (List<PoseResult>) -> Unit
) : ImageAnalysis.Analyzer {

    private var poseLandmarker: PoseLandmarker? = null

    init {
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath("pose_landmarker_lite.task")
        val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(2) // Nhận diện tối đa 2 người (Cha và Con)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ ->
                processResults(result)
            }
        
        try {
            poseLandmarker = PoseLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class PoseResult(
        val leftWrist: Point?,
        val rightWrist: Point?,
        val leftShoulder: Point? = null,
        val rightShoulder: Point? = null,
        val leftHip: Point? = null,
        val rightHip: Point? = null,
        val centerX: Float 
    )

    data class Point(val x: Float, val y: Float)

    override fun analyze(imageProxy: ImageProxy) {
        val frameTime = System.currentTimeMillis()
        val bitmap = imageProxy.toBitmap()
        val matrix = android.graphics.Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
        val mirroredBitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        
        val mpImage = BitmapImageBuilder(mirroredBitmap).build()
        poseLandmarker?.detectAsync(mpImage, frameTime)
        imageProxy.close()
    }

    private fun processResults(result: PoseLandmarkerResult) {
        val poses = result.landmarks().map { landmarks ->
            // MediaPipe Landmark IDs: 11: L_Shoulder, 12: R_Shoulder, 15: L_Wrist, 16: R_Wrist, 23: L_Hip, 24: R_Hip
            val getPt = { id: Int -> if (landmarks.size > id) Point(landmarks[id].x(), landmarks[id].y()) else null }

            PoseResult(
                leftShoulder = getPt(11),
                rightShoulder = getPt(12),
                leftWrist = getPt(15),
                rightWrist = getPt(16),
                leftHip = getPt(23),
                rightHip = getPt(24),
                centerX = landmarks.map { it.x() }.average().toFloat()
            )
        }
        onMultiPoseDetected(poses)
    }

    fun stop() {
        poseLandmarker?.close()
    }
}
