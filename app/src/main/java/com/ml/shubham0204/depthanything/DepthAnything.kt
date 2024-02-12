package com.ml.shubham0204.depthanything

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.IntBuffer

class DepthAnything(
    private val context: Context
) {

    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession = ortEnvironment.createSession( context.assets.open( "model.onnx" ).readBytes() )
    private val inputName = ortSession.inputNames.iterator().next()

    private val rotateTransform = Matrix().apply {
        postRotate( 90f )
    }

    suspend fun predict(
        inputImage: Bitmap
    ): Bitmap = withContext( Dispatchers.Default ) {
        // val t1 = System.currentTimeMillis()
        val imagePixels = convert( inputImage )
        // val t3 = System.currentTimeMillis()
        val inputTensor  = OnnxTensor.createTensor(
            ortEnvironment ,
            imagePixels ,
            longArrayOf( 1 , inputImage.width.toLong() , inputImage.height.toLong() , 3 ) ,
            OnnxJavaType.UINT8 )
        // val t2 = System.currentTimeMillis()
        val outputs = ortSession.run( mapOf( inputName to inputTensor ) )
        val outputTensor = outputs[0] as OnnxTensor
        // Log.e( "APP" , "Conversion time -> ${t3-t1}")
        // Log.e( "APP" , "Preprocessing time -> ${t2-t3}")
        // Log.e( "APP" , "Inference time -> ${System.currentTimeMillis() - t2}")

        var depthMap = Bitmap.createBitmap( 518 , 518 , Bitmap.Config.ALPHA_8 )
        depthMap.copyPixelsFromBuffer( outputTensor.byteBuffer )
        depthMap = Bitmap.createBitmap( depthMap , 0 , 0 , 518 , 518 , rotateTransform , false )
        depthMap = Bitmap.createScaledBitmap( depthMap , inputImage.width , inputImage.height , true )

        return@withContext depthMap
    }

    private fun convert(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocate(
            1 * bitmap.width * bitmap.height * 3
        )
        imgData.rewind()
        for (i in 0..<bitmap.width) {
            for (j in 0..<bitmap.height) {
                imgData.put( Color.red( bitmap[ i , j ] ).toByte() )
                imgData.put( Color.blue( bitmap[ i , j ] ).toByte() )
                imgData.put( Color.green( bitmap[ i , j ] ).toByte() )
            }
        }
        imgData.rewind()
        return imgData
    }

}