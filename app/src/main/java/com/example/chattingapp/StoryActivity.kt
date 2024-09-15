package com.example.chattingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.chattingapp.databinding.ActivityStoryBinding
import com.example.chattingapp.models.Status
import dagger.hilt.android.AndroidEntryPoint
import jp.shts.android.storiesprogressview.StoriesProgressView

@AndroidEntryPoint
class StoryActivity : AppCompatActivity() {

    private lateinit var binding:ActivityStoryBinding
    var count = 0
    private var imagesList :List<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent.extras
        val status = intent!!.getSerializable("status") as Status
        imagesList=status.imageUrls


        binding.storiesView.apply {
            setStoriesCount(imagesList.size)
            setStoryDuration(1500L)
            Glide.with(this).load(imagesList[0])
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.statusImages)
            startStories()
            setStoriesListener(object : StoriesProgressView.StoriesListener {
                override fun onNext() {
                    if (count < imagesList.size) {
                        count++
                        Glide.with(this@StoryActivity).load(imagesList[count])
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.statusImages)
                    }
                }

                override fun onPrev() {
                    if (count > 0) {

                        count--
                        Glide.with(this@StoryActivity).load(imagesList[count])
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.statusImages)

                    }
                }

                override fun onComplete() {
                   // findNavController().navigateUp()
                }

            })
        }

    }
}