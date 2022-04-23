package com.juned.dicodingstoryapp.data.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.databinding.StoryItemBinding
import com.juned.dicodingstoryapp.ui.view.story.DetailStoryActivity


class StoriesAdapter(private val storyList: ArrayList<StoryItem>) :
    RecyclerView.Adapter<StoriesAdapter.StoryHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    override fun getItemCount(): Int = storyList.size

    class StoryHolder(itemBinding: StoryItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        var storyImage = itemBinding.imgStoryUser
        var storyUser = itemBinding.tvStoryUser
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder {
        val itemBinding =
            StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: StoryHolder, position: Int) {
        val story = storyList[position]
        holder.apply {
            Glide.with(holder.storyImage.context)
                .load(story.photoUrl).listener(
                    object : RequestListener<Drawable> {
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

                    }
                )
                .into(holder.storyImage)

            storyUser.text = story.name

           storyImage.setOnClickListener {
               onItemClickCallback?.onItemClicked(storyList[holder.adapterPosition])
               val intent = Intent(itemView.context, DetailStoryActivity::class.java)
               intent.putExtra(DetailStoryActivity.EXTRA_STORY, story)

               val optionsCompat: ActivityOptionsCompat =
                   ActivityOptionsCompat.makeSceneTransitionAnimation(
                       itemView.context as Activity,
                       Pair(holder.storyImage , "profile"),
                       Pair(holder.storyUser, "name"),
                   )
               itemView.context.startActivity(intent, optionsCompat.toBundle())
           }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: StoryItem)
    }
}