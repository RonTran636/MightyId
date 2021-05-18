package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.mightyId.R
import com.mightyId.databinding.DialogTodoDetailBinding
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.TimeUtils.convertToDay
import com.mightyId.models.TodoListItem
import com.mightyId.utils.displayTodoIcon
import com.mightyId.utils.loadImage
import java.util.*

class TodoDetailDialog : DialogFragment() {
    private lateinit var binding: DialogTodoDetailBinding
    private lateinit var todoItem: TodoListItem

    companion object {
        const val TAG = "TodoDetailDialog"
        const val TODO_ITEM = "TodoItem"
        fun newInstance(todoItem: TodoListItem) = TodoDetailDialog().apply {
            arguments = Bundle().apply {
                putInfoExtra(TODO_ITEM, todoItem)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_todo_detail,container,false)
        todoItem = arguments?.getInfoExtra(TODO_ITEM)!!
        binding.holderTodoTitle.text = todoItem.todoTitle
        binding.holderTodoDetail.text = todoItem.todoContent
        binding.holderTodoStart.text = binding.root.context.getString(
            R.string.todo_start,
            todoItem.todoTimeStart.convertToDay(binding.root.context)
        )
        binding.holderTodoEnd.text = binding.root.context.getString(
            R.string.todo_end,
            todoItem.todoDeadline.convertToDay(binding.root.context)
        )
        binding.holderContactAvatarContainer.loadImage(todoItem.photoUrl, null)
        binding.todoStatusText.text = todoItem.todoStatus.capitalize(Locale.getDefault())
        binding.todoStatusIcon.displayTodoIcon(todoItem.todoStatus)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NO_TITLE,R.style.custom_dialog)
        val dialog = super.onCreateDialog(savedInstanceState)
        val params = dialog.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.attributes = params
        return dialog
    }
}