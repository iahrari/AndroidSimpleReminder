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
        viewModel.getReminder(reminderId).observe(viewLifecycleOwner) {
            if (it != null) {
                reminder = it
                binding.reminder = reminder
                Log.i("Reminder", reminder.toString())
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
                R.string.delete_question,{
                    viewModel.deleteReminder(reminder!!)
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
            minDate = reminder!!.time.time

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
                        reminder!!.type = ReminderType.DAILY

                    R.id.remind_monthly -> {
                        reminder!!.type = ReminderType.START_OF_MONTH
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

    @Suppress("DEPRECATION")
    private fun setTimePicker() {
        binding.apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                timePicker.hour = reminder!!.getCalendar().get(Calendar.HOUR)
            else
                timePicker.currentHour = reminder!!.getCalendar().get(Calendar.HOUR)

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
            reminder!!.apply {
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.SATURDAY - 1] == Reminder.WEEK_DAY_ENABLE,
                    weekSaturday
                )
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.SUNDAY - 1] == Reminder.WEEK_DAY_ENABLE,
                    weekSunday
                )
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.MONDAY - 1] == Reminder.WEEK_DAY_ENABLE,
                    weekMonday
                )
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.TUESDAY - 1] == Reminder.WEEK_DAY_ENABLE,
                    weekTuesday
                )
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.WEDNESDAY] == Reminder.WEEK_DAY_ENABLE,
                    weekWednesday
                )
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.THURSDAY - 1] == Reminder.WEEK_DAY_ENABLE,
                    weekThursday
                )
                setWeekDayBackgroundColor(
                    weeksDay[Calendar.FRIDAY - 1] == Reminder.WEEK_DAY_ENABLE,
                    weekFriday
                )

                setWeekDayClickListener(Calendar.SATURDAY - 1, weekSaturday)
                setWeekDayClickListener(Calendar.SUNDAY - 1, weekSunday)
                setWeekDayClickListener(Calendar.MONDAY - 1, weekMonday)
                setWeekDayClickListener(Calendar.TUESDAY - 1, weekTuesday)
                setWeekDayClickListener(Calendar.WEDNESDAY - 1, weekWednesday)
                setWeekDayClickListener(Calendar.THURSDAY - 1, weekThursday)
                setWeekDayClickListener(Calendar.FRIDAY - 1, weekFriday)
            }
        }
    }

    private fun setWeekDayClickListener(position: Int, weekDay: CircularRevealCardView) {
        reminder!!.apply {
            weekDay.setOnClickListener {
                weeksDay[position] =
                    if (weeksDay[position] == Reminder.WEEK_DAY_ENABLE)
                        Reminder.WEEK_DAY_DISABLE
                    else Reminder.WEEK_DAY_ENABLE

                setWeekDayBackgroundColor(weeksDay[position] == Reminder.WEEK_DAY_ENABLE, weekDay)
            }
        }
    }

    private fun setWeekDayBackgroundColor(isEnabled: Boolean, weekDay: CircularRevealCardView) {
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