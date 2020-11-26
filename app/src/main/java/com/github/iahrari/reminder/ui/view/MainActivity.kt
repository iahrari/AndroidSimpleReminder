package com.github.iahrari.reminder.ui.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.ActivityMainBinding
import com.github.iahrari.reminder.service.alarm.ReminderService
import com.github.iahrari.reminder.ui.util.LanguageUtil
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var intentId: Int? = null
    var listener: OnBackPressed? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null){
            intentId = intent!!.getIntExtra(ReminderService.REMINDER_ID, -1)
            Log.i("ReminderEditActivity", intentId.toString())
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        val nav = findNavController(R.id.nav_host_fragment)
        setupActionBarWithNavController(nav)
    }

    override fun onBackPressed() {
        if (listener != null)
            listener!!.onBackPressed()
        else super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun setFloatingButtonAction(operation: () -> Unit){
        binding.floatingAction.setOnClickListener {
            operation()
        }
    }

    fun setToolbarTitle(title: Int?){
        if (title != null)
            binding.toolbarTitle.setText(title)
        else binding.toolbarTitle.text = ""
    }

    fun setFloatingButtonUi(title: Int, icon: Int){
        lifecycleScope.launch {
            ((binding.floatingAction.layoutParams as CoordinatorLayout.LayoutParams).behavior as HideBottomViewOnScrollBehavior)
                .slideUp(binding.floatingAction)
            binding.floatingAction.shrink()
            delay(250)
            binding.floatingAction.setIconResource(icon)
            delay(250)
            binding.floatingAction.setText(title)
            binding.floatingAction.extend()
        }
    }

    override fun attachBaseContext(base: Context?) {
        val language = LanguageUtil.getLanguage(base!!)
        super.attachBaseContext(LanguageUtil.applyLanguage(base, language))
    }



    interface OnBackPressed{
        fun onBackPressed()
    }
}