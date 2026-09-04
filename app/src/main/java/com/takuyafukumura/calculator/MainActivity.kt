package com.takuyafukumura.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.takuyafukumura.calculator.ui.MainScreen
import com.takuyafukumura.calculator.ui.components.themeToggle
import com.takuyafukumura.calculator.ui.theme.CalculatorTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * アプリケーションのメインアクティビティ（Hilt対応版）
 *
 * Androidアプリの最初に起動されるコンポーネントです。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * アクティビティが作成されたときに呼び出されるメソッド
     *
     * @param savedInstanceState 以前のアクティビティ状態の保存データ。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var darkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }

            CalculatorTheme(darkTheme = darkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        themeToggle(
                            darkTheme = darkTheme,
                            onToggle = { darkTheme = it },
                        )
                    },
                ) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
