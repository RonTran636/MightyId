package com.mightyId.activities.main.ui.message.chatRoom.fullscreenImageDialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.databinding.HolderFullscreenDialogBinding
import com.mightyId.workManager.DownloadFileWorker
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter
import com.mightyId.utils.loadImage
import java.util.concurrent.TimeUnit


class ImageDialog: BaseBottomSheetDialogFragment() {

    private lateinit var binding : HolderFullscreenDialogBinding
    private lateinit var listener : ImageDialogListener
    private var imageView : String?=null
    private var imageName : String?=null

    interface ImageDialogListener{
        fun onImageDownloadCallback()
    }

    companion object{
        const val TAG = "ImageDialog"
        @JvmStatic
        fun newInstance(imageName:String,imageUrl:String): ImageDialog = ImageDialog().apply {
            arguments = Bundle().apply {
                putString("previewImage", imageUrl)
                putString("fileName",imageName)
            }
        }
    }

    override fun onAttach(context: Context) {
        try{
            listener = context as ImageDialogListener
        }catch (e: ClassCastException){
            throw java.lang.ClassCastException("$context must implement ImageDialogListener")
        }
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.holder_fullscreen_dialog,
            container,
            false)
        imageView = arguments?.getString("previewImage")
        imageName = arguments?.getString("fileName")
        binding.previewImage.loadImage(imageView.toString(), null)
        binding.backSpace.setOnClickListener { dismiss() }
        binding.actionSaveImage.setOnClickListener {
            val workManager = WorkManager.getInstance(requireContext())
            val inputData = Data.Builder()
                .putString("fileType", ChatRoomAdapter.TYPE_IMAGE)
                .putString("fileDownload",imageView)
                .putString("fileName",imageName)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(DownloadFileWorker::class.java)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .setInputData(inputData)
                .addTag("fileDownload")
                .build()
            workManager.enqueue(workRequest)
            listener.onImageDownloadCallback()
            dismiss()
        }
        return binding.root
    }
}