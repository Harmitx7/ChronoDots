package com.dotmatrix.calendar.ui.fluid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class FluidCalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FluidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FluidTheme.colors.background
                ) {
                    FluidCalendarScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewFluidCalendar() {
    FluidTheme {
        FluidCalendarScreen()
    }
}
