package com.example.myapplication.di

import android.util.Log
import javax.inject.Inject

interface ErrorLogger {
    fun error(
        tag: String,
        message: String,
        throwable: Throwable,
    )
}

class AndroidErrorLogger
    @Inject
    constructor() : ErrorLogger {
        override fun error(
            tag: String,
            message: String,
            throwable: Throwable,
        ) {
            Log.e(tag, message, throwable)
        }
    }
