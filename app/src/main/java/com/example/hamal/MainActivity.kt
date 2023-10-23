package com.example.hamal

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hamal.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OnAudioControlListener {
    fun onPlayRequested(filePath: String)
    fun onPauseRequested()
}

class MainActivity : AppCompatActivity(), OnAudioControlListener {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var bottomPlayerLayout: LinearLayout
    private lateinit var playPauseButton: ImageButton
    private lateinit var progressSeekBar: SeekBar

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .connectTimeout(2, TimeUnit.MINUTES)
        .build()

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SearchFragment()
                1 -> DownloadsFragment()
                else -> throw IllegalStateException("Invalid position $position")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomPlayerLayout = findViewById(R.id.layoutBottomPlayer)
        playPauseButton = bottomPlayerLayout.findViewById(R.id.playPauseButton)
        progressSeekBar = bottomPlayerLayout.findViewById(R.id.progressSeekBar)

        binding.viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Search"
                1 -> "Downloads"
                else -> throw IllegalStateException("Invalid position $position")
            }
        }.attach()

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnCompletionListener {
            playPauseButton.setBackgroundResource(R.drawable.ic_play)
        }

        playPauseButton.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                playPauseButton.setBackgroundResource(R.drawable.ic_play)
                handler.removeCallbacks(updateSeekBarRunnable)
            } else {
                mediaPlayer!!.start()
                playPauseButton.setBackgroundResource(R.drawable.ic_pause)
                handler.postDelayed(updateSeekBarRunnable, 1000)
            }
        }

        progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    val handler = Handler(Looper.getMainLooper())

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer?.isPlaying == true) {
                val currentPosition = mediaPlayer?.currentPosition!!
                progressSeekBar.progress = currentPosition

                val elapsedTimeTextView: TextView = findViewById(R.id.elapsedTime)
                // Si tienes un TextView para el tiempo transcurrido, actualízalo aquí
                elapsedTimeTextView.text = convertDuration(currentPosition)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun playAudio(filePath: String) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.reset()
        }
        mediaPlayer?.setDataSource(filePath)
        mediaPlayer?.prepare()
        // Establecer el valor máximo para la SeekBar.
        val maxDuration = mediaPlayer?.duration ?: 0
        progressSeekBar.max = maxDuration

        // Actualizar el TextView para mostrar la duración total del audio.
        val trackTimeTextView: TextView = findViewById(R.id.trackTime)
        trackTimeTextView.text = convertDuration(maxDuration)
        mediaPlayer?.start()

        playPauseButton.setBackgroundResource(R.drawable.ic_pause)

        handler.removeCallbacks(updateSeekBarRunnable)  // Elimina cualquier instancia previa
        handler.postDelayed(updateSeekBarRunnable, 1000)
    }

    private fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer!!.pause()
            playPauseButton.setBackgroundResource(R.drawable.ic_play)
        }
        handler.removeCallbacks(updateSeekBarRunnable)
    }

    override fun onPlayRequested(filePath: String) {
        playAudio(filePath)
    }

    override fun onPauseRequested() {
        pauseAudio()
    }

    private fun convertDuration(duration: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong()).toInt()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()).toInt() % 60
        return String.format("%02d:%02d", minutes, seconds)
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
