package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.nestedRv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mightyId.R
import com.mightyId.activities.login.signup.FragmentWebView
import com.mightyId.activities.main.ui.message.chatRoom.fullscreenImageDialog.ImageDialog
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters.FileAdapter
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters.LinkAdapter
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters.MediaAdapter
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters.TodoAdapter
import com.mightyId.databinding.HolderViewPagerBinding
import com.mightyId.utils.Constant.Companion.PAGE_NUMBER
import com.mightyId.utils.Constant.Companion.TOPIC
import com.mightyId.utils.inDevelop
import com.mightyId.workManager.DownloadFileWorker
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.TodoDetailDialog
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.TodoDialog
import com.mightyId.models.TodoListItem
import com.mightyId.utils.snackbar
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class ViewPagerHolder : Fragment(), MediaAdapter.ItemClickListener, TodoAdapter.TodoListener,
    LinkAdapter.TopicLinkListener, FileAdapter.FileListener {

    private lateinit var binding: HolderViewPagerBinding
    private lateinit var topicId: String
    private var pagePosition by Delegates.notNull<Int>()
    private val viewModel: ViewPagerViewModel by viewModels()
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var linkAdapter: LinkAdapter
    private lateinit var fileAdapter: FileAdapter

    companion object {
        fun newInstance(position: Int, topicId: String) = ViewPagerHolder().apply {
            Timber.tag("ViewPagerHolder").d("newInstance: id:$id")
            Timber.tag("ViewPagerHolder").d("newInstance: tag:$tag")
            arguments = Bundle().apply {
                putInt(PAGE_NUMBER, position)
                putString(TOPIC, topicId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.holder_view_pager, container, false)
        pagePosition = arguments?.getInt(PAGE_NUMBER)!!
        topicId = arguments?.getString(TOPIC).toString()
        Timber.tag("ViewPagerHolder").d("onCreateView: Called , position is :$pagePosition")
        when (pagePosition) {
            0 -> {
                viewModel.getTopicTodoList(topicId)
            }
            1 -> {
                viewModel.getTopicPhotoAndVideo(topicId)
            }
            2 -> {
                viewModel.getTopicLinks(topicId)
            }
            3 -> {
                viewModel.getTopicFiles(topicId)
            }
        }
        observeViewModel()
        return binding.root
    }

    fun updateTodoList(todoId:Int,status: String){
        todoAdapter.updateTodoStatus(todoId, status)
        viewModel.updateStatus(todoId, status)
    }

    private fun observeViewModel() {
        viewModel.listTodo.observe(viewLifecycleOwner) {
            todoAdapter = TodoAdapter(mutableListOf(), this)
//            binding.actionFilter.visibility = View.VISIBLE
            binding.rvMedia.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMedia.adapter = todoAdapter
            todoAdapter.update(it)
            if (it.isNullOrEmpty()) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
                binding.emptyListText.text = getString(
                    R.string.stored_media_empty,
                    getString(R.string.to_do_list).decapitalize(Locale.getDefault())
                )
            } else {
                binding.resultEmptyLayout.visibility = View.GONE
            }
        }
        viewModel.listMedia.observe(viewLifecycleOwner) {
            mediaAdapter = MediaAdapter(mutableListOf(), this)
            binding.rvMedia.layoutManager = GridLayoutManager(requireContext(), 4)
            binding.rvMedia.adapter = mediaAdapter
            mediaAdapter.update(it)
            if (it.isNullOrEmpty()) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
                binding.emptyListText.text = getString(
                    R.string.stored_media_empty,
                    getString(R.string.media).decapitalize(Locale.getDefault())
                )
            } else {
                binding.resultEmptyLayout.visibility = View.GONE
            }
        }
        viewModel.listLink.observe(viewLifecycleOwner) {
            linkAdapter = LinkAdapter(it, this)
            binding.rvMedia.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMedia.adapter = linkAdapter
            linkAdapter.update(it)
            if (it.isNullOrEmpty()) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
                binding.emptyListText.text = getString(
                    R.string.stored_media_empty,
                    getString(R.string.links).decapitalize(Locale.getDefault())
                )
            } else {
                binding.resultEmptyLayout.visibility = View.GONE
            }
        }
        viewModel.listFile.observe(viewLifecycleOwner) {
            fileAdapter = FileAdapter(it, this)
            binding.rvMedia.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMedia.adapter = fileAdapter
            if (it.isNullOrEmpty()) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
                binding.emptyListText.text = getString(
                    R.string.stored_media_empty,
                    getString(R.string.files).decapitalize(Locale.getDefault())
                )
            } else {
                binding.resultEmptyLayout.visibility = View.GONE
            }
        }
    }

    override fun onItemClick(imageName: String, imageUrl: String) {
        ImageDialog.newInstance(imageName, imageUrl).show(childFragmentManager, ImageDialog.TAG)
    }

    override fun onTodoOptionClick(todoId: Int, status: String) {
        TodoDialog.newInstance(todoId, status).show(childFragmentManager, TodoDialog.TAG)
    }

    override fun onTodoDetail(todoItem: TodoListItem) {
        TodoDetailDialog.newInstance(todoItem).show(childFragmentManager, TodoDetailDialog.TAG)
    }

    override fun onLinkOptionClick() {
        requireContext().inDevelop()
    }

    override fun onLinkClick(url: String) {
        FragmentWebView.newInstant(url).show(childFragmentManager, FragmentWebView.TAG)
    }

    override fun onFileDownload(fileName: String, fileUrl: String) {
        val workManager = WorkManager.getInstance(requireContext())
        val inputData = Data.Builder()
            .putString("fileType", ChatRoomAdapter.TYPE_FILE)
            .putString("fileDownload", fileUrl)
            .putString("fileName", fileName)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(DownloadFileWorker::class.java)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .setInputData(inputData)
            .addTag("fileDownload")
            .build()
        workManager.enqueue(workRequest)
        binding.root.snackbar("File downloading", getString(android.R.string.cancel)) {
            workManager.cancelUniqueWork("downloadFile")
        }
    }
}