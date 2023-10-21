package com.example.hamal

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DownloadedFilesAdapter(
    private val files: List<File>
) : RecyclerView.Adapter<DownloadedFilesAdapter.ViewHolder>() {

    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayButton: Button? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val playButton: Button = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_downloaded_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.titleTextView.text = file.nameWithoutExtension

        holder.playButton.setOnClickListener {
            if (holder.playButton.text == "Play") {
                playDownloadedFile(file, holder.playButton)
            } else {
                currentMediaPlayer?.pause()
                holder.playButton.text = "Play"
            }
        }
    }

    private fun playDownloadedFile(file: File, playButton: Button) {
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

    override fun getItemCount() = files.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }
}

