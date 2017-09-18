package com.example.mladen.kotlin_diploma

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import utll.Constants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.logging.Logger
import org.json.JSONObject


class  MainActivity : AppCompatActivity() {
    companion object {
        val Log = Logger.getLogger(MainActivity::class.java.name)
    }

    private val MEDIA_TYPE_PNG = MediaType.parse("image/png")
    private val filename = "mladen.jpg"
    private val MY_CAMERA_ACTIVITY_CODE: Int = 2;
    private val MY_REQUEST_CODE: Int = 1;
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createCameraButton();
    }


    /**
     * Creates camera button and set an listenr to it.
     */
    private fun createCameraButton() {
        val buttonCamera = findViewById<Button>(R.id.button_camera) as Button
        buttonCamera.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.CAMERA),
                        MY_REQUEST_CODE)
            }
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, MY_CAMERA_ACTIVITY_CODE)
        }
    }

    /**
     * Consuming an activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == MY_CAMERA_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data.extras!!.get("data") as Bitmap
            val fileForSaving = File(applicationContext.cacheDir, filename)

            savePhoto(fileForSaving, photo)

            if (fileForSaving.exists()) {
                val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", "Square Logo")
                        .addFormDataPart("file", fileForSaving.name,
                                RequestBody.create(MEDIA_TYPE_PNG, fileForSaving))
                        .build()

                Log.warning(fileForSaving.name)

                val request = Request.Builder()
                        .header("Authorization", "Client-ID " + 1)
                        .url(Constants.urlToServer)
                        .post(requestBody)
                        .build()


                val response = client.newCall(request)
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.warning("Error" + e.printStackTrace())
                                runOnUiThread {
                                    // Error
                                }
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                val jsonResp = response.body()?.string()
                                Log.warning("Success" + response.code())
                                Log.warning(response.message())
                                //Update UI
                                runOnUiThread() {
                                    val Jobject = JSONObject(jsonResp)
                                    var guessedDigit = Jobject.get("number");

                                    var textView = findViewById<TextView>(R.id.txt_main) as TextView
                                    textView.text = String.format("Your digit is was %d", guessedDigit);
                                }
                            }
                        });

                println(request)
                println(response)

            }
        } else {
            if (resultCode == Activity.RESULT_CANCELED){
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    fun savePhoto(pathForSaving: File, photo: Bitmap) {
        pathForSaving.createNewFile()

        val bitmap = photo
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

        FileOutputStream(pathForSaving).use { fos -> fos.write(bitmapdata) }
    }
}
