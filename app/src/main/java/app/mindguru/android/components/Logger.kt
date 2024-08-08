package app.mindguru.android.components

import android.util.Log

const val TAG = "MindGuru"
class Logger {
    companion object {

        fun e(tag: String, msg: String) {
            if (app.mindguru.android.BuildConfig.DEBUG || Remote.checkGate("DEBUG")) {
                if (msg.length > 4000) {
                    Log.e(TAG, tag + " : " + msg.substring(0, 4000))
                    e(tag, msg.substring(4000))
                } else {
                    Log.e(TAG, "$tag : $msg")
                }
            }
        }

        fun d(tag: String, msg: String) {
            if (app.mindguru.android.BuildConfig.DEBUG || Remote.checkGate("DEBUG")) {
                if (msg.length > 4000) {
                    Log.d(TAG, tag + " : " + msg.substring(0, 4000))
                    d(tag, msg.substring(4000))
                } else {
                    Log.d(TAG, "$tag : $msg")
                }
            }
        }

        fun w(tag: String, msg: String) {
            if (app.mindguru.android.BuildConfig.DEBUG || Remote.checkGate("DEBUG")) {
                if (msg.length > 4000) {
                    Log.w(TAG, tag + " : " + msg.substring(0, 4000))
                    w(tag, msg.substring(4000))
                } else {
                    Log.w(TAG, "$tag : $msg")
                }
            }
        }

        fun i(tag: String, msg: String) {
            if (app.mindguru.android.BuildConfig.DEBUG || Remote.checkGate("DEBUG")) {
                if (msg.length > 4000) {
                    Log.i(TAG, tag + " : " + msg.substring(0, 4000))
                    i(tag, msg.substring(4000))
                } else {
                    Log.i(TAG, "$tag : $msg")
                }
            }
        }

        fun logV(tag: String, msg: String) {
            if (app.mindguru.android.BuildConfig.DEBUG || Remote.checkGate("DEBUG")) {
                if (msg.length > 4000) {
                    Log.v(TAG, tag + " : " + msg.substring(0, 4000))
                    logV(tag, msg.substring(4000))
                } else {
                    Log.v(TAG, "$tag : $msg")
                }
            }
        }
    }
}