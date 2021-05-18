package com.mightyId.activities.login.signup

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.databinding.FragmentWebViewBinding
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.MESSAGE_WEB_URL

class FragmentWebView : BaseBottomSheetDialogFragment() {

    private lateinit var binding : FragmentWebViewBinding
    private var url : String?=null

    companion object{
        fun newInstant(url:String?) = FragmentWebView().apply {
            arguments = Bundle().apply {
                putString(MESSAGE_WEB_URL,url)
            }
        }
        const val TAG = "FragmentWebView"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet as View).apply {
                isDraggable = false
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_web_view,container,false)
        url = arguments?.getString(MESSAGE_WEB_URL)
        binding.webView.apply {
            settings.javaScriptEnabled = true
            url?.let { loadUrl(it) }
            if (url==null)loadUrl(Constant.TERM_AND_PRIVACY)
        }
        binding.backSpace.setOnClickListener{ dismiss() }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.settings.javaScriptEnabled = false
    }
}