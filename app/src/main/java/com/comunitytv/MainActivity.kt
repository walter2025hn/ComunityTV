package com.comunitytv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.comunitytv.ui.screens.HomeScreen
import com.comunitytv.ui.screens.PlayerScreen
import com.comunitytv.ui.theme.ComunityTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComunityTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showPlayer by remember { mutableStateOf(false) }
                    if (showPlayer) {
                        PlayerScreen(
                            onBackPressed = { showPlayer = false }
                        )
                    } else {
                        HomeScreen(
                            onNavigateToPlayer = { showPlayer = true }
                        )
                    }
                }
            }
        }
    }
}
