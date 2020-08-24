package com.github.iahrari.reminder.ui.view

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.FragmentMainBinding
import com.github.iahrari.reminder.databinding.SettingLayoutBinding
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.ui.adapter.ListAdapter
import com.github.iahrari.reminder.ui.util.LanguageUtil
import com.github.iahrari.reminder.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(), ListAdapter.OnItemClick {
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ListAdapter
    private lateinit var settingBinding: SettingLayoutBinding
    private lateinit var settingDialog: BottomSheetDialog
    private val viewModel: MainViewModel by viewModels()
    private var isInSelectedMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_main, container, false)
        settingBinding = DataBindingUtil
            .inflate(LayoutInflater.from(requireContext()), R.layout.setting_layout, container, false)
        settingDialog = BottomSheetDialog(requireContext())
        settingDialog.setContentView(settingBinding.root)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ListAdapter(this)
        binding.reminderRecycler.adapter = adapter

        viewModel.getReminders().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            listener = null
            setToolbarTitle(R.string.app_name)
            setFloatingActionButton(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings)
            callSettings()
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(reminder: Reminder) {
        navigateTo(reminder.id)
    }

    override fun onSwitchChanged(reminder: Reminder) {
        viewModel.insertOrUpdate(reminder, true)
    }

    override fun onItemsSelected(items: MutableList<Reminder>) {
        isInSelectedMode = items.size > 0
        setFloatingActionButton(items)
    }

    private fun navigateTo(id: Int) {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToEditFragment(id)
        )
    }

    private fun setFloatingActionButton(items: MutableList<Reminder>?) {
        (activity as MainActivity).apply {
            if (isInSelectedMode) {
                setFloatingButtonUi(R.string.delete, R.drawable.ic_delete)
                setFloatingButtonAction {
                    viewModel.deleteReminders(*items!!.toTypedArray())
                    items.clear()
                    isInSelectedMode = false
                    setFloatingActionButton(null)
                }
            } else {
                setFloatingButtonUi(R.string.add, R.drawable.ic_reminder)
                setFloatingButtonAction {
                    navigateTo(-1)
                }
            }
        }
    }

    private fun callSettings(){
        val sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        var lang = sharedPreferences.getString("language", LanguageUtil.DEFAULT)
        var theme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        settingBinding.languageSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.languages)
        )

        settingBinding.themeSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.theme)
        )

        settingBinding.themeSpinner.setSelection(
            when (theme) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 0
                AppCompatDelegate.MODE_NIGHT_YES -> 1
                else -> 2
            }
        )

        settingBinding.languageSpinner.setSelection(
            when(lang) {
                LanguageUtil.DEFAULT -> 0
                LanguageUtil.EN -> 1
                else -> 2
            }
        )

        settingBinding.doneButton.setOnClickListener {
            val edit = sharedPreferences.edit()
            lang = when(settingBinding.languageSpinner.selectedItemId){
                0L -> LanguageUtil.DEFAULT
                1L -> LanguageUtil.EN
                else -> LanguageUtil.FA
            }
            edit.putString("language", lang)

            theme = when(settingBinding.themeSpinner.selectedItemId){
                0L -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                1L -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }

            edit.putInt("theme", theme)

            edit.apply()
            AppCompatDelegate.setDefaultNightMode(theme)
            settingDialog.dismiss()
            requireActivity().finish()
            requireActivity().overridePendingTransition(0,0)
            startActivity(requireActivity().intent)
            requireActivity().overridePendingTransition(0,0)
        }
        settingDialog.setContentView(settingBinding.root)
        settingDialog.show()
    }
}