package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.CurrencyRepository
import com.example.data.HistoryRepository
import com.example.data.SettingsManager
import com.example.engine.SoundFeedback
import com.example.ui.MainCalculatorApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var historyRepo: HistoryRepository
    private lateinit var currencyRepo: CurrencyRepository
    private lateinit var soundFeedback: SoundFeedback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsManager = SettingsManager(applicationContext)
        historyRepo = HistoryRepository(applicationContext)
        currencyRepo = CurrencyRepository(applicationContext)
        soundFeedback = SoundFeedback(applicationContext)

        setContent {
            val settings by settingsManager.settings.collectAsState()

            MyApplicationTheme(themeMode = settings.themeMode) {
                MainCalculatorApp(
                    settingsManager = settingsManager,
                    historyRepo = historyRepo,
                    currencyRepo = currencyRepo,
                    soundFeedback = soundFeedback,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
