package com.ml.shubham0204.depthanything

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

/**
 * Model details: Input shape -> ( B , W , H , 3 ), dtype=UINT8 Output shape -> ( B , 518 , 518 ),
 * dtype=UINT8
 */
class DepthAnything(context: Context) {

    private val ortEnvironment = OrtEnvironment.getEnvironment()

    // See the models-v2 release for models
    // https://github.com/shubham0204/Depth-Anything-Android/releases/tag/model-v2
    private val ortSession =
        ortEnvironment.createSession(context.assets.open("fused_model_uint8_256.onnx").readBytes())
    private val inputName = ortSession.inputNames.iterator().next()
    // For '_256' suffixed models
    private val inputDim = 256
    private val outputDim = 252
    // For other models
    // private val inputDim = 512
    // private val outputDim = 504
    private val rotateTransform = Matrix().apply { postRotate(90f) }

    /** Given an image, return the single-channel depth image */
    suspend fun predict(inputImage: Bitmap): Pair<Bitmap,Long> =
        withContext(Dispatchers.Default) {
            val resizedImage = Bitmap.createScaledBitmap(
                inputImage,
                inputDim,
                inputDim,
                true
            )
            val imagePixels = convert(resizedImage)
            val inputTensor =
                OnnxTensor.createTensor(
                    ortEnvironment,
                    imagePixels,
                    longArrayOf(1, inputDim.toLong(), inputDim.toLong(), 3),
                    OnnxJavaType.UINT8
                )
            val t1 = System.currentTimeMillis()
            val outputs = ortSession.run(mapOf(inputName to inputTensor))
            val inferenceTime = System.currentTimeMillis() - t1
            val outputTensor = outputs[0] as OnnxTensor
            // Single channel bitmap, where each value is encoded in a single byte
            var depthMap = Bitmap.createBitmap(outputDim, outputDim, Bitmap.Config.ALPHA_8)
            depthMap.copyPixelsFromBuffer(outputTensor.byteBuffer)
            depthMap = Bitmap.createBitmap(depthMap, 0, 0, outputDim, outputDim, rotateTransform, false)
            depthMap =
                Bitmap.createScaledBitmap(depthMap, inputImage.width, inputImage.height, true)
            return@withContext Pair( depthMap , inferenceTime )
        }

    /**
     * Convert the given `bitmap` to a `ByteBuffer` Each pixel encoded as an int32 is split into its
     * components A, R, G and B where a new buffer with R, G and B is formed, discarding A (alpha
     * channel) as the model's input shape is ( B , W , H , C=3 )
     */
    private fun convert(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocate(1 * bitmap.width * bitmap.height * 3)
        imgData.rewind()
        for (i in 0 ..< bitmap.width) {
            for (j in 0 ..< bitmap.height) {
                imgData.put(Color.red(bitmap[i, j]).toByte())
                imgData.put(Color.blue(bitmap[i, j]).toByte())
                imgData.put(Color.green(bitmap[i, j]).toByte())
            }
        }
        imgData.rewind()
        return imgData
    }
}
