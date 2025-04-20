package com.example.newsify.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsify.adapter.NewsAdapter
import com.example.newsify.ui.MainActivity
import com.example.newsify.ui.NewsViewModel
import com.example.newsify.util.Constant
import com.example.newsify.util.Constant.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.newsify.util.Resource
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.FragmentSearchBinding
import com.example.newsprojectpractice.databinding.ItemErrorBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentSearchBinding
    private var itemErrorBinding: ItemErrorBinding? = null

    private var isError = false
    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        newsViewModel = (activity as MainActivity).newsViewModel

        setupSearchRecycler()
        setupSearchInput()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_search_to_articleFragment, bundle)
        }

        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPage = newsResponse.totalResults / Constant.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinesPage == totalPage
                        if (isLastPage) {
                            binding.recyclerSearch.setPadding(0, 0, 0, 0)
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

                is Resource.Loading -> showProgressBar()
            }
        })
    }

    private fun setupSearchInput() {
        var job: Job? = null
        binding.searchEdit.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        newsViewModel.searchNews(editable.toString())
                    }
                }
            }
        }
    }

    private fun setupSearchRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerSearch.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListener)
        }
    }

    private fun showErrorMessage(message: String) {
        if (itemErrorBinding == null) {
            itemErrorBinding = ItemErrorBinding.inflate(layoutInflater)
            binding.searchErrorContainer.addView(itemErrorBinding!!.root)
        }

        itemErrorBinding?.apply {
            errorText.text = message
            retryButton.setOnClickListener {
                if (binding.searchEdit.text.toString().isNotEmpty()) {
                    newsViewModel.searchNews(binding.searchEdit.text.toString())
                } else {
                    hideErrorMessage()
                }
            }
        }

        binding.searchErrorContainer.visibility = View.VISIBLE
        isError = true
    }

    private fun hideErrorMessage() {
        binding.searchErrorContainer.visibility = View.GONE
        isError = false
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val shouldPaginate = !isLoading && !isLastPage && !isError &&
                    firstVisibleItemPosition + visibleItemCount >= totalItemCount &&
                    firstVisibleItemPosition >= 0 &&
                    totalItemCount >= Constant.QUERY_PAGE_SIZE &&
                    isScrolling

            if (shouldPaginate) {
                newsViewModel.searchNews(binding.searchEdit.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }
}
