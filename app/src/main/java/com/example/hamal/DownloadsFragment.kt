package com.example.hamal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DownloadsFragment : Fragment() {

    private lateinit var adapter: DownloadedFilesAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onResume() {
        super.onResume()
        fetchAndDisplayDownloads()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewDownloads)
        recyclerView.layoutManager = LinearLayoutManager(context)
        fetchAndDisplayDownloads()
    }

    private fun fetchAndDisplayDownloads() {
        val downloadsDir = context?.getExternalFilesDir(null)
        val files = downloadsDir?.listFiles { _, name -> name.endsWith(".mp3") }?.toList() ?: listOf()

        adapter = DownloadedFilesAdapter(files)
        recyclerView.adapter = adapter
    }
}

