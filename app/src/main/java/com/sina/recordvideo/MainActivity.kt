package com.sina.recordvideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sina.recordvideo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val dialogFragment = CameraDialogFragment.newInstance()

        binding.tvShow.setOnClickListener {


            dialogFragment.show(supportFragmentManager, "CameraDialogFragment")
        }
    }
}