package com.businessempire.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.businessempire.game.ui.AppRoot
import com.businessempire.game.ui.theme.BusinessEmpireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusinessEmpireTheme {
                AppRoot()
            }
        }
    }
}
