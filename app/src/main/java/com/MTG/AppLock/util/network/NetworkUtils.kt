package com.MTG.AppLock.util.network

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.StrictMode
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = this.isConnected
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    private fun hasInternetAccess(context: Context): Boolean {
        if (isInternetAvailable(context)) {
            return try {
                val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val httpURLConnection: HttpURLConnection = URL("https://www.google.com").openConnection() as HttpURLConnection
                httpURLConnection.setRequestProperty("User-Agent", "Android")
                httpURLConnection.setRequestProperty("Connection", "close")
                httpURLConnection.requestMethod = "GET"
                httpURLConnection.connectTimeout = 1500
                httpURLConnection.readTimeout = 1500
                httpURLConnection.connect()
                httpURLConnection.responseCode == 200
            } catch (e: Exception) {
                false
            }
        } else {
            return false
        }
    }

    fun hasInternetAccessCheck(doTask: () -> Unit, doException: () -> Unit, activity: Activity) {
        val loader = Thread {
            when {
                hasInternetAccess(activity) -> activity.runOnUiThread {
                    doTask.invoke()
                }
                else -> activity.runOnUiThread {
                    doException.invoke()
                }
            }
        }
        loader.start()
    }

    fun hasInternetAccessCheck(context: Context, onCallbackCheckNetwork: OnCallbackCheckNetwork) {
        val loader = Thread {
            when {
                hasInternetAccess(context) -> {
                    onCallbackCheckNetwork.hasInternetAccess()
                }
                else -> {
                    onCallbackCheckNetwork.errorInternetAccess()
                }
            }
        }
        loader.start()
    }

    interface OnCallbackCheckNetwork {
        fun hasInternetAccess()
        fun errorInternetAccess()
    }
}
