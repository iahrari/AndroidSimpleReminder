package com.github.iahrari.reminder.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.FragmentMainBinding
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.ui.adapter.ListAdapter
import com.github.iahrari.reminder.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment: Fragment(), ListAdapter.OnItemClick {
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ListAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ListAdapter(this)
        binding.reminderRecycler.adapter = adapter

        viewModel.getReminders().observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        (activity as MainActivity).apply{
            setToolbarTitle(R.string.app_name)
            setFloatingButtonAction {
                MainFragmentDirections.actionMainFragmentToEditFragment(-1)
            }
        }
    }

    override fun onItemClick(reminder: Reminder) {
        MainFragmentDirections.actionMainFragmentToEditFragment(reminder.id)
    }

    override fun onSwitchChanged(reminder: Reminder) {
        viewModel.insertOrUpdate(reminder)
    }

    override fun onItemsSelected(items: MutableList<Reminder>) {
        if (items.size > 0)
            (activity as MainActivity).apply {
                setFloatingButtonUi(R.string.delete, R.drawable.ic_delete)
                setFloatingButtonAction {
                    viewModel.deleteReminders(*items.toTypedArray())
                    items.clear()
                }
            }
        else (activity as MainActivity).apply {
            setFloatingButtonUi(R.string.add, R.drawable.ic_reminder)
            setFloatingButtonAction {
                MainFragmentDirections.actionMainFragmentToEditFragment(-1)
            }
        }
    }
}