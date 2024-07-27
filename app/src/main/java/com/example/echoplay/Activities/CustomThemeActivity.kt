package com.example.echoplay.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.echoplay.R
import com.example.echoplay.Utils.Constants
import com.example.echoplay.Utils.Constants.SELECTED_THEME
import com.example.echoplay.databinding.ActivityCustomThemeBinding

class CustomThemeActivity : AppCompatActivity() {
    companion object{
        lateinit var binding: ActivityCustomThemeBinding
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        val themes: List<Pair<View, Int>> = listOf(
            binding.theme1 to R.drawable.theme1,
            binding.theme2 to R.drawable.theme2,
            binding.theme3 to R.drawable.theme3,
            binding.theme4 to R.drawable.theme4,
            binding.theme5 to R.drawable.theme5,
            binding.theme6 to R.drawable.theme6,
            binding.theme7 to R.drawable.theme7,
            binding.theme8 to R.drawable.theme8
        )

        for ((themeView, drawableRes) in themes) {
            themeView.setOnClickListener {
                showConfirmationDialog(drawableRes)
            }
        }

        binding.defaultTheme.setOnClickListener {
            showConfirmationDialog(R.drawable.theme_default)
        }



    }

    private fun showConfirmationDialog(selectedDrawable: Int) {
        AlertDialog.Builder(this)
            .setTitle("Apply Theme")
            .setMessage("Do you want to apply this theme?")
            .setPositiveButton("Yes") { _, _ ->
                // Save the selected theme in SharedPreferences
                saveSelectedTheme(selectedDrawable)
                // Finish this activity
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun saveSelectedTheme(drawableRes: Int) {
        val sharedPreferences = getSharedPreferences(Constants.SharedPreferences_name, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(SELECTED_THEME, drawableRes).apply()
    }

}