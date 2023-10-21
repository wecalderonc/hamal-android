package com.example.hamal

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ResultsAdapter(
    private val results: List<VideoResult>,
    private val onDownloadClicked: (VideoResult) -> Unit
) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val downloadIcon: ImageView = view.findViewById(R.id.downloadIcon)
        val playButton: ImageView = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoResult = results[position]
        holder.titleTextView.text = videoResult.title

        // Check if file is downloaded
        val file = File(holder.playButton.context.getExternalFilesDir(null), "${videoResult.title}.mp3")
        if (file.exists()) {
            holder.playButton.visibility = View.VISIBLE
        } else {
            holder.playButton.visibility = View.GONE
        }

        holder.downloadIcon.setOnClickListener {
            hideSoftKeyboard(it.context as Activity)
            onDownloadClicked(videoResult)
            holder.playButton.visibility = View.VISIBLE
        }

        holder.playButton.setOnClickListener {
            hideSoftKeyboard(it.context as Activity)
            if (currentMediaPlayer?.isPlaying == true && currentPlayButton == holder.playButton) {
                currentMediaPlayer?.pause()
                holder.playButton.setImageResource(R.drawable.ic_play) // Set Play icon
            } else {
                playDownloadedFile(holder.playButton.context, videoResult.title, holder.playButton)
            }
        }
    }

    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayButton: ImageView? = null

    private fun playDownloadedFile(context: android.content.Context, title: String, playButton: ImageView) {
        val file = File(context.getExternalFilesDir(null), "$title.mp3")
        if (file.exists()) {
            if (currentMediaPlayer?.isPlaying == true) {
                currentMediaPlayer?.pause()
                currentPlayButton?.setImageResource(R.drawable.ic_play) // Set Play icon
                if (currentPlayButton == playButton) {
                    return
                }
            } else if (currentPlayButton == playButton) {
                currentMediaPlayer?.start()
                playButton.setImageResource(R.drawable.ic_pause) // Set Pause icon
                return
            }

            currentMediaPlayer?.release()
            currentMediaPlayer = MediaPlayer().apply {
                setDataSource(file.path)
                prepare()
                start()
            }
            currentPlayButton = playButton
            playButton.setImageResource(R.drawable.ic_pause) // Set Pause icon

            currentMediaPlayer?.setOnCompletionListener {
                it.release()
                playButton.setImageResource(R.drawable.ic_play) // Set Play icon
                currentMediaPlayer = null
                currentPlayButton = null
            }
        }
    }

    override fun getItemCount() = results.size

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
