package com.juned.dicodingstoryapp.data.paging

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.databinding.StoryItemBinding
import com.juned.dicodingstoryapp.ui.view.story.DetailStoryActivity


class StoriesAdapter :
    PagingDataAdapter<StoryItem, StoriesAdapter.StoryHolder>(STORY_COMPARATOR) {

        class StoryHolder(private val binding: StoryItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(data: StoryItem) {
                binding.tvItemUsername.text = data.name
                val url = data.photoUrl
                Glide.with(binding.imgItemPhoto.context)
                    .load(url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            return false
                        }
                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.imgItemPhoto)
                itemView.setOnClickListener {
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(binding.imgItemPhoto , "profile"),
                            Pair(binding.tvItemUsername, "name"),
                        )
                    val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                    intent.putExtra(DetailStoryActivity.EXTRA_STORY, data)
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder {
            val itemBinding =
                StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return StoryHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: StoryHolder, position: Int) {
            val data = getItem(position)
            if (data != null) {
                holder.bind(data)
            }
        }

        companion object {
        val STORY_COMPARATOR = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem:StoryItem) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem) =
                oldItem.name == newItem.name && oldItem.description == newItem.description
                        && oldItem.photoUrl == newItem.photoUrl
        }
    }
}