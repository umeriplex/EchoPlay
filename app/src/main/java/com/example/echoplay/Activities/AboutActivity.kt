package com.example.echoplay.Activities

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.echoplay.R
import com.example.echoplay.Utils.AdHelper
import com.example.echoplay.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    companion object{
        lateinit var binding : ActivityAboutBinding
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }
        val version = getAppVersionCode()
        val versionName  = getversionName()
        binding.versionText.text = "Version: $versionName ($version)"

    }

    private fun getversionName(): Any {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }

    private fun getAppVersionCode(): Long {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }


}
