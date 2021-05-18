package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderTodoListBinding
import com.mightyId.utils.Common
import com.mightyId.utils.TimeUtils.convertToDay
import com.mightyId.models.TodoListItem
import com.mightyId.utils.displayTodoIcon
import com.mightyId.utils.loadImage
import timber.log.Timber
import java.util.*

class TodoAdapter(
    private var todoList: MutableList<TodoListItem>,
    private val todoListener: TodoListener,
) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    interface TodoListener {
        fun onTodoOptionClick(todoId: Int, status: String)
        fun onTodoDetail(todoItem: TodoListItem)
    }

    fun update(newList: MutableList<TodoListItem>) {
        if (todoList != newList) {
            todoList = newList
            notifyDataSetChanged()
        }
    }

    fun updateTodoStatus(todoId: Int, status: String) {
        for (item in todoList) {
            if (item.todoId == todoId) {
                item.todoStatus = status
                notifyItemChanged(todoList.indexOf(item))
                Timber.tag("TodoAdapter").d("updateTodoStatus: item: $item")
                return
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_todo_list, parent, false)
        )
    }

    override fun getItemCount(): Int = todoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        todoList[position].let { holder.setData(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = HolderTodoListBinding.bind(itemView)
        fun setData(todoItem: TodoListItem) {
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
            binding.holderActionOption.setOnClickListener {
                var allowToEditTodo = false
                for (user in todoItem.listAssigneeId){
                    if (user == Common.currentAccount!!.customerId){
                        allowToEditTodo = true
                    }
                }
                if (allowToEditTodo) {
                    todoListener.onTodoOptionClick(todoItem.todoId, todoItem.todoStatus)
                } else {
                    AlertDialog.Builder(itemView.context)
                        .setTitle(itemView.context.getString(R.string.todo_status_title))
                        .setMessage(itemView.context.getString(R.string.todo_status_permission))
                        .setNegativeButton(android.R.string.ok) { _, _ -> }
                        .show()
                }
            }
            binding.holderSuggestingContactContainer.setOnClickListener {
                todoListener.onTodoDetail(todoItem)
            }
        }
    }
}