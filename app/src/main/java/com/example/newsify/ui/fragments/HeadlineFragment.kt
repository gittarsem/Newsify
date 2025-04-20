package com.example.newsify.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsify.adapter.NewsAdapter
import com.example.newsify.ui.MainActivity
import com.example.newsify.ui.NewsViewModel
import com.example.newsify.util.Constant
import com.example.newsify.util.Resource
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.FragmentHeadlineBinding

class HeadlineFragment : Fragment(R.layout.fragment_headline) {

    private lateinit var binding: FragmentHeadlineBinding
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewBinding
        binding = FragmentHeadlineBinding.bind(view)

        // Initialize ViewModel
        newsViewModel = (activity as MainActivity).newsViewModel

        // Set up RecyclerView
        setupHeadlinesRecycler()

        // Handle news item click
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_headlines_to_articleFragment, bundle)
        }

        // Observe API response
        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constant.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinesPage == totalPages
                        if (isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        // Retry button click
        binding.HeadlinesError.retryButton.setOnClickListener {
            newsViewModel.getHeadlines("in")
        }
    }

    // Pagination scroll listener for RecyclerView
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val shouldPaginate =
                !isLoading && !isLastPage && firstVisibleItemPosition + visibleItemCount >= totalItemCount
                        && firstVisibleItemPosition >= 0 && totalItemCount >= Constant.QUERY_PAGE_SIZE

            if (shouldPaginate) {
                newsViewModel.getHeadlines("in")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
            }
        }
    }

    private fun setupHeadlinesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListener)
        }
    }

    // Show ProgressBar while loading data
    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    // Hide ProgressBar when loading is done
    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    // Show error message when there's an error
    private fun showErrorMessage(message: String) {
        binding.HeadlinesError.root.visibility = View.VISIBLE
        binding.HeadlinesError.errorText.text = message
    }

    // Hide error message
    private fun hideErrorMessage() {
        binding.HeadlinesError.root.visibility = View.GONE
    }
}
