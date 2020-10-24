package com.github.iahrari.reminder.ui.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.iahrari.reminder.R
import com.github.iahrari.reminder.databinding.FragmentEditBinding
import com.github.iahrari.reminder.service.model.Reminder
import com.github.iahrari.reminder.service.model.ReminderType
import com.github.iahrari.reminder.viewmodel.MainViewModel
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class EditFragment : Fragment(), MainActivity.OnBackPressed {
    private var reminder: Reminder? = null
    private lateinit var binding: FragmentEditBinding
    private var reminderId: Int = -1
    private val viewModel: MainViewModel by viewModels()
    private val args: EditFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).apply {
            listener = this@EditFragment
            setToolbarTitle(null)
            setFloatingButtonUi(R.string.save, R.drawable.ic_save)
            setFloatingButtonAction {
                viewModel.insertOrUpdate(reminder!!, false)
                navigateBack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit, container, false)
        reminderId = args.reminderId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("ReminderEdit", reminderId.toString())
        viewModel.getReminder(reminderId).observe(viewLifecycleOwner) {
            if (it != null) {
                reminder = it
                binding.reminder = reminder
                binding.monthPickerContainer.type = reminder!!.type
                Log.i("ReminderEdit", reminder.toString())
                setUI()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete)
            setAlertDialog(
                R.string.delete_question, {
                    viewModel.deleteReminders(reminder!!)
                }, {})

        return super.onOptionsItemSelected(item)
    }

    private fun setUI() {
        setTimePicker()
        setTypePicker()
        setDatePicker()
        setWeekDayPicker()
    }

    private fun setDatePicker() {
        binding.datePickerContainer.datePicker.apply {
            minDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }.time.time

            init(
                reminder!!.getCalendar().get(Calendar.YEAR),
                reminder!!.getCalendar().get(Calendar.MONTH),
                reminder!!.getCalendar().get(Calendar.DAY_OF_MONTH)
            ) { _, p1, p2, p3 ->
                reminder!!.time =
                    reminder!!.getCalendar().apply {
                        set(Calendar.YEAR, p1)
                        set(Calendar.MONTH, p2)
                        set(Calendar.DAY_OF_MONTH, p3)
                    }.time
            }
        }
    }

    private fun setTypePicker() {
        binding.apply {
            remindTypes.setOnCheckedChangeListener { _, i ->
                when (i) {
                    R.id.remind_daily ->
                        checkDailyType()

                    R.id.remind_monthly -> {
                        val type =
                            if (reminder!!.type == ReminderType.START_OF_MONTH || reminder!!.type == ReminderType.END_OF_MONTH)
                                reminder!!.type
                            else ReminderType.START_OF_MONTH

                        reminder!!.type = type
                        binding.monthPickerContainer.type = reminder!!.type
                        binding.monthPickerContainer.monthPicker.setOnCheckedChangeListener { _, itemId ->
                            if (itemId == R.id.start_of_month)
                                reminder!!.type = ReminderType.START_OF_MONTH
                            else
                                reminder!!.type = ReminderType.END_OF_MONTH
                        }
                    }

                    R.id.remind_exact_day ->
                        reminder!!.type = ReminderType.EXACT_TIME
                }

                binding.reminder = reminder!!
            }
        }
    }

    private fun checkDailyType() {
        reminder!!.type = ReminderType.DAILY
        if (reminder!!.type == ReminderType.DAILY || reminder!!.type == ReminderType.ONCE || reminder!!.type == ReminderType.WEEKLY || reminder!!.type == ReminderType.DAYS_OF_WEEK) {
            var enabledDays = 0
            for (d in reminder!!.weeksDay)
                if (d == Reminder.WEEK_DAY_ENABLE)
                    enabledDays++

            reminder!!.type = when (enabledDays) {
                0 -> ReminderType.ONCE
                1 -> ReminderType.WEEKLY
                7 -> ReminderType.DAILY
                else -> ReminderType.DAYS_OF_WEEK
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setTimePicker() {
        binding.apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.hour = reminder!!.getCalendar().get(Calendar.HOUR_OF_DAY)
            else
                timePicker.currentHour = reminder!!.getCalendar().get(Calendar.HOUR_OF_DAY)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.minute = reminder!!.getCalendar().get(Calendar.MINUTE)
            else
                timePicker.currentMinute =
                    reminder!!.getCalendar().get(Calendar.MINUTE)

            timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                reminder!!.time = reminder!!.getCalendar().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }.time
            }
        }
    }


    private fun setWeekDayPicker() {
        binding.weekPicker.apply {
            setWeekDayBackgroundColor(Calendar.SATURDAY, weekSaturday)
            setWeekDayBackgroundColor(Calendar.SUNDAY, weekSunday)
            setWeekDayBackgroundColor(Calendar.MONDAY, weekMonday)
            setWeekDayBackgroundColor(Calendar.TUESDAY, weekTuesday)
            setWeekDayBackgroundColor(Calendar.WEDNESDAY, weekWednesday)
            setWeekDayBackgroundColor(Calendar.THURSDAY, weekThursday)
            setWeekDayBackgroundColor(Calendar.FRIDAY, weekFriday)

            setWeekDayClickListener(Calendar.SATURDAY, weekSaturday)
            setWeekDayClickListener(Calendar.SUNDAY, weekSunday)
            setWeekDayClickListener(Calendar.MONDAY, weekMonday)
            setWeekDayClickListener(Calendar.TUESDAY, weekTuesday)
            setWeekDayClickListener(Calendar.WEDNESDAY, weekWednesday)
            setWeekDayClickListener(Calendar.THURSDAY, weekThursday)
            setWeekDayClickListener(Calendar.FRIDAY, weekFriday)
        }
    }

    private fun setWeekDayClickListener(position: Int, weekDay: CircularRevealCardView) {
        reminder!!.apply {
            weekDay.setOnClickListener {
                weeksDay[position - 1] =
                    if (weeksDay[position - 1] == Reminder.WEEK_DAY_ENABLE)
                        Reminder.WEEK_DAY_DISABLE
                    else Reminder.WEEK_DAY_ENABLE
                checkDailyType()

                setWeekDayBackgroundColor(position, weekDay)
            }
        }
    }

    private fun setWeekDayBackgroundColor(position: Int, weekDay: CircularRevealCardView) {
        val isEnabled = reminder!!.weeksDay[position - 1] == Reminder.WEEK_DAY_ENABLE
        weekDay.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (isEnabled) R.color.colorAccent else R.color.colorDeselected
            )
        )
    }

    override fun onBackPressed() {
        if (viewModel.isReminderUpdated(reminder))
            setAlertDialog(R.string.save_reminder, {
                viewModel.insertOrUpdate(reminder!!, false)
            }, { navigateBack() })
        else navigateBack()
    }

    private fun navigateBack() {
        (activity as MainActivity).apply {
            listener = null
            onBackPressed()
        }
    }

    private fun setAlertDialog(
        title: Int,
        positiveOperation: () -> Unit,
        negativeOperation: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setTitle(title)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                positiveOperation()
                dialog.dismiss()
                navigateBack()
            }.setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
                negativeOperation()
            }.create().show()
    }
}