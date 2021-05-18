package com.mightyId.activities.main.ui.message.chatRoom.messageOption

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_EDIT
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_IMAGE
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_LINK
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_TEXT
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.chatPartner
import com.mightyId.databinding.DialogMessageOptionBinding
import kotlin.properties.Delegates

class DialogMessageOption : BaseBottomSheetDialogFragment(),View.OnClickListener {

    private lateinit var binding : DialogMessageOptionBinding
    private var messageId by Delegates.notNull<Int>()
    private lateinit var messageType: String
    private var viewType by Delegates.notNull<Int>()

    private lateinit var listener: DialogMessageCallback

    companion object {
        const val TAG = "DialogMessageOption"
        private const val MESSAGE_ID = "messageId"
        private const val MESSAGE_TYPE = "messageType"
        private const val MESSAGE_VIEW_TYPE = "messageViewType"

        const val ACTION_PIN = "messagePin"
        const val ACTION_EDIT = "messageEdit"
        const val ACTION_REPLY = "messageReply"
        const val ACTION_FORWARD = "messageForward"
        const val ACTION_COPY = "messageCopy"
        const val ACTION_DELETE = "messageDelete"
        const val ACTION_SAVE ="imageSave"

        @JvmStatic
        fun newInstance(messageId: Int,messageType:String,viewType:Int) =
            DialogMessageOption().apply {
                arguments = Bundle().apply {
                    putString(MESSAGE_TYPE,messageType)
                    putInt(MESSAGE_VIEW_TYPE,viewType)
                    putInt(MESSAGE_ID, messageId)
                }
            }
    }

    interface DialogMessageCallback{
        fun onDialogMessageCallback(messageId: Int,action: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        arguments?.let {
            messageType = it.getString(MESSAGE_TYPE).toString()
            messageId = it.getInt(MESSAGE_ID)
            viewType = it.getInt(MESSAGE_VIEW_TYPE)
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_message_option,container,false)
        binding.actionReply.setOnClickListener(this)
        binding.actionForward.setOnClickListener(this)
        binding.actionCopy.setOnClickListener(this)
        binding.actionDelete.setOnClickListener(this)
        binding.actionPin.setOnClickListener(this)
        binding.actionEdit.setOnClickListener(this)
        updateUIWithGivenType(messageType)
        return binding.root
    }

    private fun updateUIWithGivenType(messageType: String) {
        if (viewType == chatPartner){
            binding.actionDelete.visibility = View.GONE
            binding.actionEdit.visibility = View.GONE
        }
        when(messageType){
            TYPE_TEXT, TYPE_EDIT->{
                binding.actionSave.visibility = View.GONE
            }
            TYPE_IMAGE->{
                binding.actionEdit.visibility = View.GONE
                binding.actionCopy.visibility = View.GONE
            }
            TYPE_LINK->{
                binding.actionEdit.visibility = View.GONE
                binding.actionSave.visibility =View.GONE
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as DialogMessageCallback
        }catch (e: ClassCastException){
            throw java.lang.ClassCastException(context.toString()+ "must implement DialogMessageCallback")
        }
    }

    override fun onClick(v: View?) {
        when(v){
            binding.actionCopy->{
                listener.onDialogMessageCallback(messageId, ACTION_COPY)
                dismiss()
            }
            binding.actionReply->{
                listener.onDialogMessageCallback(messageId, ACTION_REPLY)
                dismiss()
            }
            binding.actionForward->{
                listener.onDialogMessageCallback(messageId, ACTION_FORWARD)
                dismiss()
            }
            binding.actionEdit->{
                listener.onDialogMessageCallback(messageId, ACTION_EDIT)
                dismiss()
            }
            binding.actionPin->{
                listener.onDialogMessageCallback(messageId, ACTION_PIN)
                dismiss()
            }
            binding.actionDelete->{
                listener.onDialogMessageCallback(messageId, ACTION_DELETE)
                dismiss()
            }
            binding.actionSave->{
                listener.onDialogMessageCallback(messageId, ACTION_SAVE)
            }
        }
    }
}