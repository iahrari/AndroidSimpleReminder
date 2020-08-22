package com.github.iahrari.reminder.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.ReminderItemBinding
import com.github.iahrari.reminder.service.model.Reminder

class ListAdapter(private val listener: OnItemClick):
    androidx.recyclerview.widget.ListAdapter<Reminder, ListAdapter.VHolder> (ReminderDiffUtil()){
    private val selectedList = mutableListOf<Reminder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder =
        VHolder.from(parent, R.layout.reminder_item, listener, selectedList)

    override fun onBindViewHolder(holder: VHolder, position: Int) =
        holder.bind(getItem(position))

    class VHolder private constructor(
        private val binding: ReminderItemBinding,
        private val listener: OnItemClick,
        private val selectedList: MutableList<Reminder>
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(data: Reminder){
            binding.hover.visibility = if(data.isSelected) View.VISIBLE else View.GONE

            binding.reminder = data

            binding.root.setOnClickListener {
                if (!data.isSelected) listener.onItemClick(data)
                else {
                    itemSelected(data, false)
                    selectedList.remove(data)
                }
            }

            binding.root.setOnLongClickListener {
                if (!data.isSelected) {
                    itemSelected(data, true)
                    selectedList.add(data)
                    true
                } else false
            }

            binding.isEnabled.setOnCheckedChangeListener { _, b ->
                data.isEnabled = b
                listener.onSwitchChanged(data)
            }
        }

        private fun itemSelected(reminder: Reminder, selected: Boolean){
            listener.onItemsSelected(selectedList)
            reminder.isSelected = selected
            binding.hover.visibility = if(selected) View.VISIBLE else View.GONE
        }

        companion object {
            fun from(parent: ViewGroup, layout: Int, listener: OnItemClick, selectedList: MutableList<Reminder>): VHolder =
                VHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        layout,
                        parent,
                        false
                    ),
                    listener,
                    selectedList
                )
        }
    }

    class ReminderDiffUtil: DiffUtil.ItemCallback<Reminder>(){
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean  =
            newItem.isEnabled == oldItem.isEnabled &&
                    newItem.time == oldItem.time &&
                    newItem.title == oldItem.title &&
                    newItem.type == oldItem.type &&
                    newItem.weeksDay.equals(oldItem)

    }

    interface OnItemClick{
        fun onItemClick(reminder: Reminder)
        fun onSwitchChanged(reminder: Reminder)
        fun onItemsSelected(items: MutableList<Reminder>)
    }
}