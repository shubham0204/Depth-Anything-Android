package com.ml.shubham0204.depthanything

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
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
    private val ortSession =
        ortEnvironment.createSession(context.assets.open("model.onnx").readBytes())
    private val inputName = ortSession.inputNames.iterator().next()

    private val rotateTransform = Matrix().apply { postRotate(90f) }

    /** Given an image, return the single-channel depth image */
    suspend fun predict(inputImage: Bitmap): Bitmap =
        withContext(Dispatchers.Default) {
            val imagePixels = convert(inputImage)
            val inputTensor =
                OnnxTensor.createTensor(
                    ortEnvironment,
                    imagePixels,
                    longArrayOf(1, inputImage.width.toLong(), inputImage.height.toLong(), 3),
                    OnnxJavaType.UINT8
                )
            val outputs = ortSession.run(mapOf(inputName to inputTensor))
            val outputTensor = outputs[0] as OnnxTensor
            // Single channel bitmap, where each value is encoded in a single byte
            var depthMap = Bitmap.createBitmap(518, 518, Bitmap.Config.ALPHA_8)
            depthMap.copyPixelsFromBuffer(outputTensor.byteBuffer)
            depthMap = Bitmap.createBitmap(depthMap, 0, 0, 518, 518, rotateTransform, false)
            depthMap =
                Bitmap.createScaledBitmap(depthMap, inputImage.width, inputImage.height, true)
            return@withContext depthMap
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
