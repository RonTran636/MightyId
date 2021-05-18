package com.mightyId.activities.main.ui.message.chatRoom.todo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_ID
import com.mightyId.activities.main.ui.message.chatRoom.options.MenuOptionViewModel
import com.mightyId.databinding.FragmentCreateTaskBinding
import com.mightyId.models.Account
import org.jetbrains.anko.sdk27.coroutines.onCreateContextMenu
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates


class FragmentCreateTask : BaseBottomSheetDialogFragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: FragmentCreateTaskBinding
    private lateinit var listener: CreateTaskListener
    private lateinit var deadline: String
    private val menuOptionViewModel: MenuOptionViewModel by viewModels()
    private var listMemberInTopic = arrayListOf<Account>()
    private var listAssignee = mutableListOf<Account>()
    private var assignee : String =""
    private var topicId: String = ""
    private var proceed = true
    private val groupId = 303

    //Date time picker:
    private var hour by Delegates.notNull<Int>()
    private var minute by Delegates.notNull<Int>()
    private var day by Delegates.notNull<Int>()
    private var month by Delegates.notNull<Int>()
    private var year by Delegates.notNull<Int>()

    interface CreateTaskListener {
        fun onAssignTaskCallback(
            title: String,
            content: String,
            assignee: MutableList<Account>,
            deadline: String,
        )
    }

    companion object {
        const val TAG = "FragmentCreateTask"
        fun newInstance(topicId: String) = FragmentCreateTask().apply {
            arguments = Bundle().apply {
                putString(TOPIC_ID, topicId)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as CreateTaskListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_task, container, false)
        topicId = arguments?.getString(TOPIC_ID).toString()
        menuOptionViewModel.getMember(topicId)
        requireActivity().registerForContextMenu(binding.todoAssign)
        observeViewModel()
        binding.todoDeadline.setOnClickListener {
            Timber.tag("FragmentCreateTask").d("onCreateView todoDeadline: called")
            showDateTimePicker()
        }
        binding.todoAssign.onCreateContextMenu { menu, _, _ ->
            menu!!.clear()
            for ((i, item) in listMemberInTopic.withIndex()) {
                menu.addItemClickListener(item, i)
            }
        }
        binding.todoAssign.setOnClickListener { it.showContextMenu(it.x, it.y) }

        binding.actionAssignTask.setOnClickListener {
            verifyInput()
            if (proceed) {
                val title = binding.todoTitle.text.toString()
                val content = binding.todoContent.text.toString()
                listener.onAssignTaskCallback(title, content, listAssignee, deadline)
                dismiss()
            }
        }
        binding.backSpace.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    private fun observeViewModel() {
        menuOptionViewModel.listMember.observe(viewLifecycleOwner) {
            listMemberInTopic = it
        }
    }

    private fun verifyInput() {
        if (binding.todoTitle.length() <= 0) {
            binding.todoTitleLayout.error = getString(R.string.field_not_null)
            proceed = false
        }
//        if (binding.todoAssign.length() <= 0) {
//            binding.todoAssignLayout.error = getString(R.string.field_not_null)
//            proceed = false
//        }
        if (binding.todoDeadline.length() <= 0) {
            binding.todoDeadlineLayout.error = getString(R.string.field_not_null)
            proceed = false
        }
        binding.todoTitle.doOnTextChanged { _, _, _, _ ->
            proceed = true
            binding.todoTitleLayout.error = ""
        }
//        binding.todoAssign.doOnTextChanged { _, _, _, _ ->
//            proceed = true
//            binding.todoAssignLayout.error = ""
//        }
        binding.todoDeadline.doOnTextChanged { _, _, _, _ ->
            proceed = true
            binding.todoDeadlineLayout.error = ""
        }
    }

    private fun ContextMenu.addItemClickListener(account: Account, position: Int) {
        this.add(groupId, position, position, account.customerName)
            .setOnMenuItemClickListener {
                /**Assign multiple users, pending temporary
                if (listAssignee.contains(account)) {
                    listAssignee.remove(account)
                    var temp = binding.todoAssign.text.toString()
                    temp = temp.replace(account.customerName!!, "")
                    Timber.tag("FragmentCreateTask").d("addItemClickListener: new text: $temp")
                    binding.todoAssign.setText(temp)
                } else {
                    listAssignee.add(account)
                    binding.todoAssign.append("${account.customerName} ")
                }
                */
                listAssignee.clear()
                listAssignee.add(account)
                binding.todoAssign.setText(account.customerName!!)
                return@setOnMenuItemClickListener true
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        Timber.tag("FragmentCreateTask").d("showDateTimePicker: calendar $calendar")
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        Timber.tag("FragmentCreateTask").d("showDateTimePicker: date initial : $day,$month,$year")
        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Timber.tag("FragmentCreateTask").d("onDateSet: date set : $dayOfMonth,$month,$year")
        this.year = year
        this.month = month+1
        this.day = dayOfMonth
        val calendar: Calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        Timber.tag("FragmentCreateTask").d("onDateSet: time initial: $hour,$minute")
        val timePickerDialog = TimePickerDialog(requireContext(), this, hour, minute, true)
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        Timber.tag("FragmentCreateTask").d("onTimeSet: $hourOfDay, $minute")
        this.hour = hourOfDay
        this.minute = minute
        val formatMonth = if (this.month < 10) "0${this.month}" else this.month.toString()
        val formatDay = if (this.day < 10) "0${this.day}" else this.day.toString()
        val formatHour = if (this.hour < 10) "0${this.hour}" else this.hour.toString()
        val formatMinute = if (this.minute < 10) "0${this.minute}" else this.minute.toString()
        val formatSecond = Calendar.getInstance().get(Calendar.SECOND)
        deadline = "${this.year}-$formatMonth-$formatDay $formatHour:$formatMinute:$formatSecond"
        Timber.tag("FragmentCreateTask").d("onTimeSet: deadline: $deadline")
        binding.todoDeadline.setText(deadline)
    }
}