package com.example.hamal

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ResultsAdapter(
    private val results: List<VideoResult>,
    private val onDownloadClicked: (VideoResult) -> Unit
) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val downloadButton: Button = view.findViewById(R.id.downloadButton)
        val playButton: Button = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoResult = results[position]
        holder.titleTextView.text = videoResult.title

        holder.downloadButton.setOnClickListener {
            onDownloadClicked(videoResult)
            holder.playButton.visibility = View.VISIBLE
        }

        holder.playButton.setOnClickListener {
            if (holder.playButton.text == "Play") {
                playDownloadedFile(holder.playButton.context, videoResult.title, holder.playButton)
            } else {
                currentMediaPlayer?.pause()
                holder.playButton.text = "Play"
            }
        }
    }

    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayButton: Button? = null

    private fun playDownloadedFile(context: android.content.Context, title: String, playButton: Button) {
        val file = File(context.getExternalFilesDir(null), "$title.mp3")
        if (file.exists()) {
            if (currentMediaPlayer?.isPlaying == true) {
                currentMediaPlayer?.pause()
                currentPlayButton?.text = "Play"
                if (currentPlayButton == playButton) {
                    return
                }
            } else if (currentPlayButton == playButton) {
                currentMediaPlayer?.start()
                playButton.text = "Pause"
                return
            }

            currentMediaPlayer?.release()
            currentMediaPlayer = MediaPlayer().apply {
                setDataSource(file.path)
                prepare()
                start()
            }
            currentPlayButton = playButton
            playButton.text = "Pause"

            currentMediaPlayer?.setOnCompletionListener {
                it.release()
                playButton.text = "Play"
                currentMediaPlayer = null
                currentPlayButton = null
            }
        }
    }


    override fun getItemCount() = results.size
}
