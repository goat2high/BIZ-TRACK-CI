package com.example.biztrackci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.biztrackci.ui.navigation.BizTrackApp
import com.example.biztrackci.ui.theme.BIZTRACKCITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BIZTRACKCITheme {
                BizTrackApp()
            }
        }
    }
}
