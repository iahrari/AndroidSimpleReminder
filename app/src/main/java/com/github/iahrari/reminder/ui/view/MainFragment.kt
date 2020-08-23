package com.github.iahrari.reminder.ui.view

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.FragmentMainBinding
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.ui.adapter.ListAdapter
import com.github.iahrari.reminder.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(), ListAdapter.OnItemClick {
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: ListAdapter
    private val viewModel: MainViewModel by viewModels()
    private var isInSelectedMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_main, container, false)
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
        //TODO: Implement this method
    }
}