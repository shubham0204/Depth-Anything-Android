package com.ml.shubham0204.depthanything

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.exifinterface.media.ExifInterface
import com.ml.shubham0204.depthanything.ui.screens.WelcomeScreen
import com.ml.shubham0204.depthanything.ui.theme.DepthAnythingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    private var depthImageState = mutableStateOf<Bitmap?>( null )
    private var progressState = mutableStateOf( false )
    private lateinit var depthAnything: DepthAnything
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        depthAnything = DepthAnything( this )


        setContent {
            ActivityUI()
        }
    }

    @Composable
    private fun ActivityUI() {
        DepthAnythingTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val depthImage by remember{ depthImageState }
                ProgressDialog()
                if( depthImage != null ) {
                    DepthImageUI(depthImage = depthImage!!)
                }
                else {
                    ImageSelectionUI()
                }
            }
        }
    }


    @Composable
    private fun ImageSelectionUI() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally ,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Depth-Anything", style = MaterialTheme.typography.displayLarge)
            Text(text = "The model description" , style = MaterialTheme.typography.bodyLarge)
            Button(onClick = {
                dispatchTakePictureIntent()
            }) {
                Text(text = "Take A Picture")
            }
            Button(onClick = {
                pickMedia.launch( PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly) )
            }) {
                Text(text = "Select From Gallery")
            }
        }
    }

    @Composable
    private fun DepthImageUI(
        depthImage: Bitmap
    ) {
        Column( modifier = Modifier.padding( 16.dp ) ) {
            Text(text = "Depth Image", style=MaterialTheme.typography.headlineSmall )
            Image(
                modifier = Modifier
                    .zoomable(rememberZoomState())
                contentScale = ContentScale.Fit,
                bitmap = depthImage.asImageBitmap(),
                contentDescription = "Depth Image"
            )
            Button(
                modifier = Modifier.align( Alignment.CenterHorizontally ) ,
                onClick = { depthImageState.value = null }
            ) {
                Text(text = "Close")
            }
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            progressState.value = true
            val bitmap = getFixedBitmap( uri )
            CoroutineScope( Dispatchers.Default ).launch {
                val depthMap = depthAnything.predict( bitmap )
                depthImageState.value = applyColormap( depthMap )
                withContext( Dispatchers.Main ) {
                    progressState.value = false
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ProgressDialog() {
        val isShowingProgress by remember{ progressState }
        if( isShowingProgress ) {
            BasicAlertDialog(
                onDismissRequest = { /* ProgressDialog is not cancellable */ }
            ) {
                Surface(
                    color = androidx.compose.ui.graphics.Color.White
                ) {
                    Column(
                        modifier = Modifier.padding( 16.dp ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Processing image ...")
                    }
                }
            }
        }
    }


    private fun saveBitmap(
        context: Context,
        image: Bitmap,
        name: String
    ) {
        val fileOutputStream = FileOutputStream(File( context.filesDir.absolutePath + "/$name.png"))
        image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
    }

    private fun rotateBitmap( source: Bitmap , degrees : Float ): Bitmap {
        val matrix = Matrix()
        matrix.postRotate( degrees )
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix , false )
    }


    private fun getFixedBitmap( imageFileUri : Uri ) : Bitmap {
        var imageBitmap = BitmapFactory.decodeStream( contentResolver.openInputStream( imageFileUri ) )
        val exifInterface = ExifInterface( contentResolver.openInputStream( imageFileUri )!! )
        imageBitmap =
            when (exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION ,
                ExifInterface.ORIENTATION_UNDEFINED )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap( imageBitmap , 90f )
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap( imageBitmap , 180f )
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap( imageBitmap , 270f )
                else -> imageBitmap
            }
        return imageBitmap
    }


    private fun applyColormap(
        depthMap: Bitmap
    ): Bitmap {
        val colorBitmap = Bitmap.createBitmap( depthMap.width , depthMap.height , Bitmap.Config.ARGB_8888 )
        for( i in 0..<depthMap.width ) {
            for( j in 0..<depthMap.height ) {
                colorBitmap[i, j] = applyInfernoColormap( Color.alpha( depthMap[ i , j ] ) )
            }
        }
        return colorBitmap
    }

    // Dispatch an Intent which opens the camera application for the user.
    // The code is from -> https://developer.android.com/training/camera/photobasics#TaskPath
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent( MediaStore.ACTION_IMAGE_CAPTURE )
        if ( takePictureIntent.resolveActivity( packageManager ) != null ) {
            val photoFile: File? = try {
                val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                File.createTempFile("image", ".jpg", imagesDir).apply {
                    currentPhotoPath = absolutePath
                }
            }
            catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.ml.shubham0204.depthanything", it
                )
                takePictureLauncher.launch( photoURI )
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult( ActivityResultContracts.TakePicture() ) {
        if( it ) {
            var bitmap = BitmapFactory.decodeFile( currentPhotoPath )
            val exifInterface = ExifInterface( currentPhotoPath )
            bitmap =
                when (exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION , ExifInterface.ORIENTATION_UNDEFINED )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap( bitmap , 90f )
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap( bitmap , 180f )
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap( bitmap , 270f )
                    else -> bitmap
                }
            progressState.value = true
            CoroutineScope( Dispatchers.Default ).launch {
                val depthMap = depthAnything.predict( bitmap )
                depthImageState.value = applyColormap( depthMap )
                withContext( Dispatchers.Main ) {
                    progressState.value = false
                }
            }
        }
    }

}

