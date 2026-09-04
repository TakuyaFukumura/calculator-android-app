package com.example.myapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * アプリケーションクラス（Hilt対応版）
 *
 * Androidアプリケーション全体のライフサイクルを管理するクラスです。
 */
@HiltAndroidApp
class MyApplication : Application()
