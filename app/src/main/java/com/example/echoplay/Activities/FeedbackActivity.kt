package com.example.echoplay.Activities
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.echoplay.databinding.ActivityFeedbackBinding


class FeedbackActivity : AppCompatActivity() {


    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendFeedbackBtn.setOnClickListener {
            if (isInternetAvailable()) {
                sendFeedback()
            } else {
                Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
    private fun sendFeedback() {
        val topic = binding.feedbackTopic.text.toString().trim()
        val feedback = binding.feedbackText.text.toString().trim()

        if (topic.isEmpty() || feedback.isEmpty()) {
            binding.feedbackText.error = "Please fill in this field"
            binding.feedbackTopic.error = "Please fill in this field"
            return
        }

        binding.loadingBar.visibility = View.VISIBLE

        // Create an Intent to send an email
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf("ahmediftikharsheikh401@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Topic: $topic")
            putExtra(Intent.EXTRA_TEXT, "Feedback: $feedback")
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"))
            Toast.makeText(this, "Feedback sent successfully", Toast.LENGTH_SHORT).show()
            binding.feedbackTopic.text?.clear()
            binding.feedbackText.text?.clear()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send feedback", Toast.LENGTH_SHORT).show()
        } finally {
            binding.loadingBar.visibility = View.GONE
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}
