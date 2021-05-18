package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.mightyId.R
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter
import com.mightyId.databinding.DialogTodoStatusBinding
import timber.log.Timber
import java.util.*

class TodoDialog : DialogFragment() {
    private lateinit var binding: DialogTodoStatusBinding
    private lateinit var listener: TodoDialogCallback
    private var todoId:Int =-1

    interface TodoDialogCallback {
        fun onUpdateStatusCallback(todoId: Int,status: String)
    }

    companion object {
        const val TAG = "TodoDialog"
        const val TODO_STATUS = "todoStatus"
        const val TODO_ID = "todoId"
        fun newInstance(todoId:Int,todoStatus: String) = TodoDialog().apply {
            arguments = Bundle().apply {
                putInt(TODO_ID,todoId)
                putString(TODO_STATUS, todoStatus)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as TodoDialogCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_todo_status, container, false)
        todoId = arguments?.getInt(TODO_ID)!!
        Timber.tag("TodoDialog").d("onCreateView: todostatus:${arguments?.getString(TODO_STATUS)} ")
        when (arguments?.getString(TODO_STATUS)) {
            ChatRoomAdapter.TODO_PENDING -> binding.statusPending.isChecked = true
            ChatRoomAdapter.TODO_CONFIRM -> binding.statusConfirm.isChecked = true
            ChatRoomAdapter.TODO_REJECT -> binding.statusReject.isChecked = true
            ChatRoomAdapter.TODO_FAILED -> binding.statusFail.isChecked = true
            ChatRoomAdapter.TODO_COMPLETE -> binding.statusComplete.isChecked = true
        }
        binding.statusPending.onStatusChange()
        binding.statusConfirm.onStatusChange()
        binding.statusReject.onStatusChange()
        binding.statusFail.onStatusChange()
        binding.statusComplete.onStatusChange()
        return binding.root
    }

    private fun RadioButton.onStatusChange(){
        this.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                Timber.tag("TodoDialog").d("onStatusChange: new status:${buttonView.text}")
                listener.onUpdateStatusCallback(todoId,buttonView.text.toString()
                    .decapitalize(Locale.getDefault()))
                this@TodoDialog.dismiss()
            }
        }
    }
}