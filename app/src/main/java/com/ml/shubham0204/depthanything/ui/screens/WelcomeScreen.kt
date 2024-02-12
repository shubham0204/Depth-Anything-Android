package com.ml.shubham0204.depthanything.ui.screens

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ml.shubham0204.depthanything.ui.theme.DepthAnythingTheme

@Composable
fun WelcomeScreen() {
    DepthAnythingTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally ,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Depth-Anything", style = MaterialTheme.typography.displayLarge)
                Text(text = "The model description" , style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Take A Picture")
                }
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Select From Gallery")
                }
            }
        }
    }
}