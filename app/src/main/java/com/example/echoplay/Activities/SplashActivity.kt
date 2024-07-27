package com.example.echoplay.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.echoplay.R

class SplashActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_OLD = Manifest.permission.READ_EXTERNAL_STORAGE
    private val STORAGE_PERMISSION_NEW = Manifest.permission.READ_MEDIA_AUDIO
    private val STORAGE_PERMISSION_USER_SELECTED = Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    private val REQUEST_STORAGE_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        requestPermission()

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    STORAGE_PERMISSION_OLD
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    STORAGE_PERMISSION_NEW
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    STORAGE_PERMISSION_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(STORAGE_PERMISSION_OLD, STORAGE_PERMISSION_NEW, STORAGE_PERMISSION_USER_SELECTED),
                    REQUEST_STORAGE_PERMISSION
                )
            } else {
                Handler().postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 300)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Handler().postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 300)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        STORAGE_PERMISSION_OLD
                    )
                ) {
                    requestPermission()
                } else {
                    showPermissionDialog()
                }
            }
        }
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("This app needs the Storage permission to work properly. Grant permission?")
        builder.setPositiveButton("setting") { dialog, which ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)

        }
        builder.setNegativeButton("Cancel") { dialog, which -> finish() }
        builder.show()
    }


    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    STORAGE_PERMISSION_OLD
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    STORAGE_PERMISSION_NEW
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    this,
                    STORAGE_PERMISSION_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Handler().postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 300)
            }
        }

    }
}