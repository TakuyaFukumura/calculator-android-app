package com.takuyafukumura.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.takuyafukumura.calculator.ui.MainScreen
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
            CalculatorTheme {
                MainScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
