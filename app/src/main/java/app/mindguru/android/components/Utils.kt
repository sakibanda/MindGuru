package app.mindguru.android.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import app.mindguru.android.data.model.User
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Locale

class Utils {

    companion object {
        fun getFirstPrompt(): String{
            val age  = 2024 - User.currentUser!!.dob.drop(6).toInt()
            var userProfile = "Hi, "
            User.currentUser!!.apply {
                if(name != "" && dob != "" && gender != "" && employment != "" && country != "")
                    userProfile = "My name is ${User.currentUser!!.name}, " +
                            "I am $age years old, ${User.currentUser!!.gender}. My profession is ${User.currentUser!!.employment} and I am from ${User.currentUser!!.country}."
                else {
                    if (name != "")
                        userProfile += "My name is ${User.currentUser!!.name}."
                    if (dob != "")
                        userProfile += "I am $age years old."
                    if (gender != "")
                        userProfile += "I am $gender. "
                    if (employment != "")
                        userProfile += "My profession is $employment. "
                    if (country != "")
                        userProfile += "I am from $country. "
                }
            }
            val symptoms = "I am experiencing these ${User.currentUser!!.healthSeverity} symptoms: ${User.currentUser!!.symptoms}. "
            val instructions = "Please provide me with the necessary help."
            return userProfile + symptoms + instructions
        }

        fun readJSONFromAsset(application: Context, fileName: String): String? {
            var json: String? = null
            try {
                //TO DO: check file exists
                val inputStream: InputStream = application.assets.open(fileName)
                json = inputStream.bufferedReader().use { it.readText() }
            } catch (ex: IOException) {
                Remote.captureException(ex)
                //ex.printStackTrace()
                return null
            }
            return json
        }

        fun downloadFile(url: String, fileName: String, context: Context): String {
            kotlin.runCatching {
                val file =
                    File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                if (file.exists()) {
                    file.delete()
                }
                val destinationUri = Uri.fromFile(file)
                val outputStream = context.contentResolver.openOutputStream(destinationUri)
                val connection = URL(url).openConnection()
                val inputStream = connection.getInputStream()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                outputStream?.use { outputStreamNew ->
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStreamNew.write(buffer, 0, bytesRead)
                    }
                    outputStreamNew.close()
                }
                inputStream.close()
                return file.absolutePath
            }.onFailure {
                //it.printStackTrace()
                Remote.captureException(it)
            }
            return ""
        }

        fun getHttpGETRequest(url: String): String {
            try {
                val response = StringBuilder()
                val connection = URL(url).openConnection()
                connection.connect()
                connection.getInputStream().bufferedReader().use {
                    it.lines().forEach { line ->
                        response.append(line)
                    }
                }
                return response.toString()
            } catch (e: Exception) {
                //e.printStackTrace()
                Remote.captureException(e)
            }
            return ""
        }

        suspend fun saveBitmapToExternalDir(bitmap: Bitmap, context: Context): String {

            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_${System.currentTimeMillis()}.jpg"
            )
            if (file.exists()) {
                file.delete()
            }
            val path = file.absolutePath
            val destinationUri = Uri.fromFile(file)
            val outputStream = context.contentResolver.openOutputStream(destinationUri)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
            outputStream.close()
            outputStream.flush()
            return path
        }

        fun isConnectedToNetwork(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }


        fun resizeBitmap(image: Bitmap, maxHeight: Int, maxWidth: Int): Bitmap {


            if (maxHeight > 0 && maxWidth > 0) {

                val sourceWidth: Int = image.width
                val sourceHeight: Int = image.height

                var targetWidth = maxWidth
                var targetHeight = maxHeight

                val sourceRatio = sourceWidth.toFloat() / sourceHeight.toFloat()
                val targetRatio = maxWidth.toFloat() / maxHeight.toFloat()

                if (targetRatio > sourceRatio) {
                    targetWidth = (maxHeight.toFloat() * sourceRatio).toInt()
                } else {
                    targetHeight = (maxWidth.toFloat() / sourceRatio).toInt()
                }

                return Bitmap.createScaledBitmap(
                    image, targetWidth, targetHeight, true
                )

            } else {
                throw RuntimeException()
            }
        }

        fun changeLanguage(context: Context, language: String) {
            Logger.d("Utils", "changeLanguage: $language")
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.create(Locale.forLanguageTag(language))
            )

        }

        @SuppressLint("HardwareIds")
        fun getAndroidId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }// Galaxy nexus 品牌类型

    }
}