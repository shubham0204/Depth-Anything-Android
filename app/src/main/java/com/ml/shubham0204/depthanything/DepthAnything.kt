package com.ml.shubham0204.depthanything

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import androidx.core.graphics.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class DepthAnything(context: Context, val modelName: String) {

    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession = ortEnvironment.createSession(context.assets.open(modelName).readBytes())
    private val inputName = ortSession.inputNames.iterator().next()

    private val inputDim: Int
    private val outputDim: Int

    init {
        when {
            modelName.contains("_256") -> {
                inputDim = 256
                outputDim = 252
            }
            modelName.contains("_512") -> {
                inputDim = 512
                outputDim = 504
            }
            else -> throw IllegalArgumentException("Unsupported model size")
        }
    }

    private val rotateTransform = Matrix().apply { postRotate(90f) }

    suspend fun predict(inputImage: Bitmap): Pair<Bitmap, Long> =
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
            var depthMap = Bitmap.createBitmap(outputDim, outputDim, Bitmap.Config.ALPHA_8)
            depthMap.copyPixelsFromBuffer(outputTensor.byteBuffer)
            depthMap = Bitmap.createBitmap(depthMap, 0, 0, outputDim, outputDim, rotateTransform, false)
            depthMap = Bitmap.createScaledBitmap(depthMap, inputImage.width, inputImage.height, true)
            return@withContext Pair(depthMap, inferenceTime)
        }

    private fun convert(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocate(1 * bitmap.width * bitmap.height * 3)
        imgData.rewind()
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                imgData.put(Color.red(bitmap[i, j]).toByte())
                imgData.put(Color.blue(bitmap[i, j]).toByte())
                imgData.put(Color.green(bitmap[i, j]).toByte())
            }
        }
        imgData.rewind()
        return imgData
    }
}