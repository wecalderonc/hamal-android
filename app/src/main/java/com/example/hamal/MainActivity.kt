package com.example.hamal

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hamal.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var binding: ActivityMainBinding

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(2, TimeUnit.MINUTES)  // Adjust the timeout as needed
        .writeTimeout(2, TimeUnit.MINUTES)
        .connectTimeout(2, TimeUnit.MINUTES)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewResults.layoutManager = LinearLayoutManager(this)

        binding.buttonSearch.setOnClickListener {
            onSearchClicked()
        }
    }

    private fun onSearchClicked() {
        Log.d("MainActivity", "Search button clicked")
        val query = binding.editTextSearch.text.toString()

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val results = api.searchYoutube(query)
                withContext(Dispatchers.Main) {
                    val adapter = ResultsAdapter(results) { videoResult ->
                        onDownloadClicked(videoResult)
                    }
                    binding.recyclerViewResults.adapter = adapter
                    binding.recyclerViewResults.invalidate()

                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    Log.e("MainActivity", "Error fetching results: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "Error fetching results", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onDownloadClicked(videoResult: VideoResult) {
        Log.d("MainActivity", "videoUrl: ${videoResult.url}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = DownloadRequestBody(videoResult.url, videoResult.title)
                Log.d("MainActivity", "Before API call")
                val file = api.downloadVideo(requestBody)
                //val response = api.downloadVideo(requestBody)
                saveFileToStorage(file.byteStream(), "${videoResult.title}.mp3")
                Log.d("MainActivity", "After API call")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MainActivity", "Error downloading: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "Error downloading file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveFileToStorage(inputStream: InputStream, filename: String) {
        val file = File(getExternalFilesDir(null), filename)
        file.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }
}

interface ApiService {
    @GET("youtube_search/search")
    suspend fun searchYoutube(@Query("query") query: String): List<VideoResult>

    @POST("downloads")
    suspend fun downloadVideo(@Body requestBody: DownloadRequestBody): ResponseBody
}

data class VideoResult(val title: String, val url: String)

data class DownloadRequestBody(val url: String, val title: String)
