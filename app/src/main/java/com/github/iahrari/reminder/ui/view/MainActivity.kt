package com.github.iahrari.reminder.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
    }

    fun setFloatingButtonAction(operation: () -> Unit){
        binding.floatingAction.setOnClickListener {
            operation()
        }
    }

    fun setToolbarTitle(title: Int){
        TODO("Implement this method")
    }

    fun setFloatingButtonUi(title: Int, icon: Int){
        binding.floatingAction.setText(title)
        binding.floatingAction.setIconResource(icon)
    }
}