package com.mightyId.activities.call.joinExistingCall

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mightyId.R
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_NAME

class RequestJoinMeetingDialogFragment: DialogFragment(){
        internal lateinit var listener: JoinCallDialogListener

        interface JoinCallDialogListener{
            fun onCancelJoin(dialog : DialogFragment)
            fun onRequestJoin(dialog: DialogFragment)
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            try {
                listener = context as JoinCallDialogListener
            }catch (e: ClassCastException){
                throw ClassCastException((context.toString() +
                        " must implement NoticeDialogListener"))
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let {
                val name = requireArguments().getString(REMOTE_MSG_CALLER_NAME)
                val builder = AlertDialog.Builder(it)
                builder.setTitle(getString(R.string.dialog_join_the_room_title, name))
                        .setMessage(getString(R.string.dialog_join_the_room_title, name))
                        .setPositiveButton(R.string.action_accept) { _, _ ->
                            //Accept to join the call, establish connection
                            listener.onRequestJoin(this)
                        }
                        .setNegativeButton(R.string.action_cancel) { _, _ ->
                            listener.onCancelJoin(this)
                        }
                // Create the AlertDialog object and return it
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }

}