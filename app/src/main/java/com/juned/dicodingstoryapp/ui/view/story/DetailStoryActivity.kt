package com.juned.dicodingstoryapp.ui.view.story

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {

    private var _binding: ActivityDetailStoryBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val story = intent.getParcelableExtra<StoryItem>(EXTRA_STORY) as StoryItem
        setupStoryDetail(story)
    }

    private fun setupStoryDetail(story : StoryItem){
        binding?.apply {
            binding?.imgStoryDetail?.let {
                Glide.with(applicationContext)
                    .load(story.photoUrl)
                    .into(it)
            }
            tvDetailName.text = story.name
            tvDetailDescription.text =story.description
        }
    }

    companion object{
        const val EXTRA_STORY  = "extra_story"
    }
}