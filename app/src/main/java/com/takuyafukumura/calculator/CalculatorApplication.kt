package com.takuyafukumura.calculator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * アプリケーションクラス（Hilt対応版）
 *
 * Androidアプリケーション全体のライフサイクルを管理するクラスです。
 */
@HiltAndroidApp
class CalculatorApplication : Application()
