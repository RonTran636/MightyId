package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayoutMediator
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.activities.main.ui.message.chatRoom.fullscreenImageDialog.ImageDialog
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.nestedRv.ViewPagerAdapter
import com.mightyId.databinding.ActivityStoredMediaBinding
import com.mightyId.utils.Common
import com.mightyId.utils.connectionLostSnackBar
import com.mightyId.utils.setLightStatusBar
import com.mightyId.utils.snackbar

class StoredMediaActivity : BaseActivity(), ImageDialog.ImageDialogListener,
    TodoDialog.TodoDialogCallback {

    private lateinit var binding : ActivityStoredMediaBinding
    private lateinit var topicId:String
    private lateinit var viewPagerAdapter : ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stored_media)
        topicId = intent.getStringExtra(ChatRoomActivity.TOPIC_ID).toString()
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setLightStatusBar(false)
            statusBarColor = resources.getColor(R.color.primary_color,theme)
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        viewPagerAdapter = ViewPagerAdapter(this, topicId)
        binding.viewPager.adapter = viewPagerAdapter

        binding.backSpace.setOnClickListener { finish() }
        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position->
            when (position){
                0 -> {
                    tab.text = getString(R.string.to_do_list)
                }
                1 -> {
                    tab.text = getString(R.string.media)
                }
                2 -> {
                    tab.text = getString(R.string.links)
                }
                3 -> {
                    tab.text = getString(R.string.files)
                }
            }
        }.attach()
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, theme)))
        }
        Common.isConnected.observe(this){
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
    }

    override fun onImageDownloadCallback() {
        binding.root.snackbar("File downloading",getString(android.R.string.cancel)) {
            val workManager = WorkManager.getInstance(this)
            workManager.cancelUniqueWork("downloadFile")
        }
    }

    override fun onUpdateStatusCallback(todoId: Int, status: String) {
        viewPagerAdapter.onTodoStatusChange(todoId, status)
    }
}

