package com.ml.shubham0204.depthanything

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.ml.shubham0204.depthanything.ui.theme.DepthAnythingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {

    private var depthImageState = mutableStateOf<Bitmap?>(null)
    private var inferenceTimeState = mutableLongStateOf(0)
    private var progressState = mutableStateOf(false)
    private lateinit var depthAnything: DepthAnything
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        depthAnything = DepthAnything(this)

        setContent { ActivityUI() }
    }

    @Composable
    private fun ActivityUI() {
        DepthAnythingTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val depthImage by remember { depthImageState }
                ProgressDialog()
                if (depthImage != null) {
                    DepthImageUI(depthImage = depthImage!!)
                } else {
                    ImageSelectionUI()
                }
            }
        }
    }

    @Composable
    private fun ImageSelectionUI() {
        val pickMediaLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) {
                if (it != null) {
                    progressState.value = true
                    val bitmap = getFixedBitmap(it)
                    CoroutineScope(Dispatchers.Default).launch {
                        val (depthMap,inferenceTime) = depthAnything.predict(bitmap)
                        depthImageState.value = colormapInferno(depthMap)
                        inferenceTimeState.longValue = inferenceTime
                        withContext(Dispatchers.Main) { progressState.value = false }
                    }
                }
            }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = getString(R.string.model_name),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = getString(R.string.model_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Hyperlink-style text
            // Reference: https://stackoverflow.com/a/69549929/13546426
            val annotatedString = buildAnnotatedString {
                pushStringAnnotation(
                    tag = "paper",
                    annotation = getString(R.string.model_paper_url)
                )
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("View Paper")
                }
                pop()
                append("   ")
                pushStringAnnotation(
                    tag = "github",
                    annotation = getString(R.string.model_github_url)
                )
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("GitHub")
                }
                pop()
            }
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                onClick = { offset ->
                    annotatedString
                        .getStringAnnotations(tag = "paper", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            Intent(Intent.ACTION_VIEW, Uri.parse(it.item)).apply {
                                startActivity(this)
                            }
                        }
                    annotatedString
                        .getStringAnnotations(tag = "github", start = offset, end = offset)
                        .firstOrNull()
                        ?.let {
                            Intent(Intent.ACTION_VIEW, Uri.parse(it.item)).apply {
                                startActivity(this)
                            }
                        }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { dispatchTakePictureIntent() }) { Text(text = "Take A Picture") }

            Button(
                onClick = {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text(text = "Select From Gallery")
            }
        }
    }

    @Composable
    private fun DepthImageUI(depthImage: Bitmap) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f),
                    text = "Depth Image",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onClick = { depthImageState.value = null }
                ) {
                    Text(text = "Close")
                }
            }
            Image(
                modifier =
                Modifier
                    .aspectRatio(depthImage.width.toFloat() / depthImage.height.toFloat())
                    .zoomable(rememberZoomState()),
                bitmap = depthImage.asImageBitmap(),
                contentDescription = "Depth Image"
            )
            Text(text = "Inference time: ${inferenceTimeState.longValue} ms")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ProgressDialog() {
        val isShowingProgress by remember { progressState }
        if (isShowingProgress) {
            BasicAlertDialog(onDismissRequest = { /* ProgressDialog is not cancellable */}) {
                Surface(color = androidx.compose.ui.graphics.Color.White) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, false)
    }

    private fun getFixedBitmap(imageFileUri: Uri): Bitmap {
        var imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageFileUri))
        val exifInterface = ExifInterface(contentResolver.openInputStream(imageFileUri)!!)
        imageBitmap =
            when (
                exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(imageBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(imageBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(imageBitmap, 270f)
                else -> imageBitmap
            }
        return imageBitmap
    }

    // Dispatch an Intent which opens the camera application for the user.
    // The code is from -> https://developer.android.com/training/camera/photobasics#TaskPath
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? =
                try {
                    val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    File.createTempFile("image", ".jpg", imagesDir).apply {
                        currentPhotoPath = absolutePath
                    }
                } catch (ex: IOException) {
                    null
                }
            photoFile?.also {
                val photoURI =
                    FileProvider.getUriForFile(this, "com.ml.shubham0204.depthanything", it)
                takePictureLauncher.launch(photoURI)
            }
        }
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                var bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                val exifInterface = ExifInterface(currentPhotoPath)
                bitmap =
                    when (
                        exifInterface.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED
                        )
                    ) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                        else -> bitmap
                    }
                progressState.value = true
                CoroutineScope(Dispatchers.Default).launch {
                    val (depthMap, inferenceTime) = depthAnything.predict(bitmap)
                    depthImageState.value = colormapInferno(depthMap)
                    inferenceTimeState.longValue = inferenceTime
                    withContext(Dispatchers.Main) { progressState.value = false }
                }
            }
        }
}
