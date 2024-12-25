package com.example.myconnect.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.myconnect.R
import com.example.myconnect.api.FlaskApi
import com.example.myconnect.databinding.ActivityResultBinding
import com.example.myconnect.handler.LoadingDialog
import com.example.myconnect.model.ApiResponse
import com.example.myconnect.retrofit.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class Result : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var loading: LoadingDialog
    private var imageFile: File? = null
    private var imageUri: Uri? = null
    private var imagePath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loading = LoadingDialog(this, "Processing...")

        imageUri = intent.getParcelableExtra("imageUri")
        imagePath = intent.getStringExtra("imagePath")
        Log.d("ImagePath", "Image Path: $imagePath")
        val imageFile = if (imagePath != null) imagePath?.let { File(it) } else null

        if (imageUri != null) {
            binding.image.setImageURI(imageUri)

        } else {

        }

        binding.generateBtn.setOnClickListener {
            loading.startLoading()
            val question = binding.question.text.toString()
            if (question.isEmpty()) {
                loading.isDismiss()
                binding.question.error = "Please enter a question"
                return@setOnClickListener
            } else {
                imageFile?.let { processImage(it, question) }
            }

        }

        setupClickListener()

    }

    private fun setupClickListener() {
        binding.apply {
            back.setOnClickListener {
                onBackPressed()
            }

            copyImg.setOnClickListener {
                val textToCopy = binding.output.text.toString()
                val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clipData = android.content.ClipData.newPlainText("text", textToCopy)
                clipboardManager.setPrimaryClip(clipData)
                android.widget.Toast.makeText(this@Result, "Text copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
            }

            shareImg.setOnClickListener {
                val textToShare = binding.output.text.toString()
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, textToShare)
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, "Generated Text")
                startActivity(shareIntent)

            }
        }
    }


    private fun processImage(imageFile: File, question: String) {
        val retrofit = RetrofitClient.instance.create(FlaskApi::class.java)

        // Create MultipartBody.Part
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

        // Add question as form data
        val questionBody = RequestBody.create("text/plain".toMediaTypeOrNull(), question)

        val call = retrofit.processImage(body, questionBody)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.result

                    if (result != null) {
                        loading.isDismiss()
                        binding.output.visibility = View.VISIBLE
                        binding.copyShare.visibility=View.VISIBLE
                        binding.question.visibility = View.GONE
                        binding.output.setText(result)
                    } else {
                        loading.isDismiss()
                        binding.output.visibility = View.VISIBLE
                        binding.question.visibility = View.GONE
                        binding.output.setText("Something went wrong")
                    }
                    Log.d("API Response", "Result: $result")
                } else {
                    loading.isDismiss()
                    Log.e("API Response", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                loading.isDismiss()
                Log.e("API Call", "Failure: ${t.message}")
            }
        })
    }
}