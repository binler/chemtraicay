package com.example.testapp.services

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

/**
 * Service xử lý nhận diện khung xương (Pose Detection) từ camera.
 */
class PoseDetectorService(private val onPoseDetected: (PoseResult) -> Unit) : ImageAnalysis.Analyzer {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    data class PoseResult(
        val leftWrist: Point?,
        val rightWrist: Point?
    )

    data class Point(val x: Float, val y: Float)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    val result = processPose(pose, imageProxy.width, imageProxy.height)
                    onPoseDetected(result)
                }
                .addOnFailureListener {
                    // Xử lý lỗi nếu cần
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processPose(pose: Pose, imageWidth: Int, imageHeight: Int): PoseResult {
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        // Trả về tọa độ chuẩn hóa (0.0 - 1.0) và thực hiện Mirroring
        return PoseResult(
            leftWrist = leftWrist?.let { 
                Point((imageWidth - it.position.x) / imageWidth, it.position.y / imageHeight) 
            },
            rightWrist = rightWrist?.let { 
                Point((imageWidth - it.position.x) / imageWidth, it.position.y / imageHeight)
            }
        )
    }

    fun stop() {
        poseDetector.close()
    }
}
