package com.ksale.calculatorapp

// for a 'val' variable
// for a `var` variable also add
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Calculator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Calculator()
}



