package io.cyberdise.anyway

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.cyberdise.anyway.ui.AnywayApp
import io.cyberdise.anyway.ui.AnywayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val state = AppState(Store(applicationContext))
        Nudges.schedule(applicationContext)
        setContent {
            AnywayTheme {
                AnywayApp(state)
            }
        }
    }
}
