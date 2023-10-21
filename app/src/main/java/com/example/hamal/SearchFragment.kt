package com.example.hamal

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hamal.databinding.FragmentSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var api: ApiService
    //private val api: ApiService = (activity as MainActivity).api

    override fun onAttach(context: Context) {
        super.onAttach(context)
        api = (context as MainActivity).api
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.recyclerViewResults.layoutManager = LinearLayoutManager(context)
        binding.buttonSearch.setOnClickListener { onSearchClicked() }
        return binding.root
    }

    private fun onSearchClicked() {
        Log.d("SearchFragment", "Search button clicked")
        val query = binding.editTextSearch.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = api.searchYoutube(query)
                withContext(Dispatchers.Main) {
                    val adapter = ResultsAdapter(results) { videoResult ->
                        onDownloadClicked(videoResult)
                    }
                    binding.recyclerViewResults.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SearchFragment", "Error fetching results: ${e.message}", e)
                    Toast.makeText(context, "Error fetching results", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onDownloadClicked(videoResult: VideoResult) {
        Log.d("SearchFragment", "videoUrl: ${videoResult.url}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = DownloadRequestBody(videoResult.url, videoResult.title)
                Log.d("SearchFragment", "Before API call")
                val file = api.downloadVideo(requestBody)
                saveFileToStorage(file.byteStream(), "${videoResult.title}.mp3")
                Log.d("SearchFragment", "After API call")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SearchFragment", "Error downloading: ${e.message}", e)
                    Toast.makeText(context, "Error downloading file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveFileToStorage(inputStream: InputStream, filename: String) {
        val file = File(requireContext().getExternalFilesDir(null), filename)
        file.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }
}
