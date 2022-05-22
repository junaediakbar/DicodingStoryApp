package com.juned.dicodingstoryapp.data.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.juned.dicodingstoryapp.databinding.LoadingBinding


class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.ViewHolder>() {
    class ViewHolder(private val binding: LoadingBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.generalRetryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                if (loadState is LoadState.Error) {
                    generalErrorText.text = loadState.error.localizedMessage
                }

                generalProgressBar.isVisible = loadState is LoadState.Loading
                generalLoadingText.isVisible = loadState is LoadState.Loading
                generalRetryButton.isVisible = loadState is LoadState.Error
                generalErrorText.isVisible = loadState is LoadState.Error
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val binding = LoadingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}