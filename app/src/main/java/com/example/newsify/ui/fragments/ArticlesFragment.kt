package com.example.newsify.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.example.newsify.ui.MainActivity
import com.example.newsify.ui.NewsViewModel
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.FragmentArticlesBinding
import com.google.android.material.snackbar.Snackbar


class ArticlesFragment : Fragment(R.layout.fragment_articles) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var binding: FragmentArticlesBinding
    val args: ArticlesFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentArticlesBinding.bind(view)

        newsViewModel=(activity as MainActivity).newsViewModel
        val article=args.article

        binding.webView.apply {
            webViewClient= WebViewClient()
            article.url?.let{
                loadUrl(it)
            }
        }
        binding.fab.setOnClickListener{
            newsViewModel.addToFavourite(article)
            Toast.makeText(requireContext(),"Added to Favourites!",Toast.LENGTH_SHORT).show()

        }

    }


}