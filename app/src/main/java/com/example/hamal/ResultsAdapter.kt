package com.example.hamal

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ResultsAdapter(
    private val results: List<VideoResult>,
    private val onDownloadClicked: (VideoResult, (Boolean) -> Unit) -> Unit,
    private val audioControlListener: OnAudioControlListener
) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val downloadIcon: ImageView = view.findViewById(R.id.downloadIcon)
        val playButton: ImageView = view.findViewById(R.id.playButton)
        var isPlaying: Boolean = false
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

        if (holder.isPlaying) {
            holder.playButton.setImageResource(R.drawable.ic_pause)  // Cambia al ícono de "Pause"
        } else {
            holder.playButton.setImageResource(R.drawable.ic_play)  // Cambia al ícono de "Play"
        }

        holder.downloadIcon.setOnClickListener {
            hideSoftKeyboard(it.context as Activity)

            // Hide the download and play buttons
            holder.downloadIcon.visibility = View.GONE
            holder.playButton.visibility = View.GONE

            // Show the progress bar
            val loadingProgressBar: ProgressBar = holder.itemView.findViewById(R.id.loadingProgressBar)
            loadingProgressBar.visibility = View.VISIBLE

            // Trigger the download
            onDownloadClicked(videoResult) { success ->
                // Callback: executed when download finishes (success or failure)
                loadingProgressBar.visibility = View.GONE
                if (success) {
                    holder.playButton.visibility = View.VISIBLE
                } else {
                    holder.downloadIcon.visibility = View.VISIBLE
                }
            }
        }

        holder.playButton.setOnClickListener {
            hideSoftKeyboard(it.context as Activity)
            val filePath = file.path

            if (holder.isPlaying) {
                audioControlListener.onPauseRequested()
                holder.isPlaying = false  // Actualiza el estado
                holder.playButton.setImageResource(R.drawable.ic_play)  // Cambia al ícono de "Play"
            } else {
                audioControlListener.onPlayRequested(filePath)
                holder.isPlaying = true  // Actualiza el estado
                holder.playButton.setImageResource(R.drawable.ic_pause)  // Cambia al ícono de "Pause"
            }
        }
    }

    private var currentPlayButton: ImageView? = null

    override fun getItemCount() = results.size

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
