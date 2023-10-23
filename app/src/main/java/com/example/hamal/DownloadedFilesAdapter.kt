package com.example.hamal

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DownloadedFilesAdapter(
    private val files: List<File>,
    private val audioControlListener: OnAudioControlListener
) : RecyclerView.Adapter<DownloadedFilesAdapter.ViewHolder>() {

    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayButton: ImageView? = null
    private var currentlyPlayingFile: File? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val playButton: ImageView = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_downloaded_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.titleTextView.text = file.nameWithoutExtension

        // Actualiza el ícono del botón en función del estado de reproducción
        if (currentlyPlayingFile == file) {
            holder.playButton.setImageResource(R.drawable.ic_pause)
        } else {
            holder.playButton.setImageResource(R.drawable.ic_play)
        }

        holder.playButton.setOnClickListener {
            val filePath = file.path

            if (currentlyPlayingFile == file) {
                audioControlListener.onPauseRequested()
                currentlyPlayingFile = null
                holder.playButton.setImageResource(R.drawable.ic_play)
            } else {
                audioControlListener.onPlayRequested(filePath)
                currentlyPlayingFile = file
                holder.playButton.setImageResource(R.drawable.ic_pause)
            }

            notifyDataSetChanged()  // Esto actualizará todos los elementos para asegurarse de que sólo un botón muestra el ícono de pausa
        }
    }

    override fun getItemCount() = files.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }
}

