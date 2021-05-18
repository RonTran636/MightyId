package com.mightyId.activities.main.ui.setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.facebook.login.LoginManager
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mightyId.R
import com.mightyId.activities.login.LoginActivity
import com.mightyId.activities.main.ui.setting.privacy.PrivacyFragment
import com.mightyId.databinding.FragmentSettingBinding
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.inDevelop
import com.mightyId.utils.loadImage
import timber.log.Timber


class SettingFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: SettingViewModel
    private lateinit var binding: FragmentSettingBinding
    private lateinit var viewRoot: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
        viewRoot = binding.root

        binding.settingName.text = Common.currentAccount!!.customerName
        binding.settingWorkid.text = getString(R.string.holder_id, Common.currentAccount!!.workId)
        binding.settingAvatar.loadImage(Common.currentAccount!!.photoUrl, null)

        binding.logOutText.setOnClickListener(this)
        binding.accountAndSecurityText.setOnClickListener(this)
        binding.callText.setOnClickListener(this)
        binding.themeText.setOnClickListener(this)
        binding.notificationText.setOnClickListener(this)
        binding.messageText.setOnClickListener(this)
        binding.languageText.setOnClickListener(this)
        binding.aboutUsText.setOnClickListener(this)
        binding.privacyText.setOnClickListener(this)
        binding.actionChangeAvatar.setOnClickListener(this)

        observeViewModel()
        return viewRoot
    }

    private fun observeViewModel() {
        viewModel.loggedOut.observe(viewLifecycleOwner) {
            if (it) Toast.makeText(
                requireContext(),
                getString(R.string.logging_out),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.logOutText -> {
                showPopup()
            }
            binding.callText -> {
                requireActivity().inDevelop()
            }
            binding.themeText, binding.messageText, binding.notificationText, binding.languageText, binding.aboutUsText -> {
                requireActivity().inDevelop()
            }
            binding.privacyText -> {
                PrivacyFragment.newInstance().show(childFragmentManager, PrivacyFragment.TAG)
            }
            binding.actionChangeAvatar -> {
                checkForStoragePermission()
            }
        }
    }

    private fun checkForStoragePermission() {
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val listener = object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            getContent.launch("image/*")
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?,
                ) {
                    AlertDialog.Builder(requireContext())
                        .setIcon(R.drawable.notification_icon)
                        .setTitle("Access storage permission")
                        .setMessage("In order to change your avatar, access storage permission are needed. Please grant permission for the best experience.")
                        .setPositiveButton("Ok") { _, _ -> }
                        .show()
                }
            }
            Dexter.withContext(requireContext())
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(listener)
                .onSameThread()
                .check()
        } else {
            getContent.launch("image/*")
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.tag("SettingFragment").d("registerForActivityResult: data return : $it")
        viewModel.changeAvatar(it)
        binding.settingAvatar.loadImage(it.toString(), null)
        Common.currentAccount!!.photoUrl = it.toString()
    }

    private fun showPopup() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage("Are you sure want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                Timber.tag("SettingFragment")
                    .d("showPopup: fcm token: ${Common.currentAccount!!.fcmToken}")
                LoginManager.getInstance().logOut()
                viewModel.logout(Common.currentAccount!!.fcmToken!!)
                FirebaseMessaging.getInstance().deleteToken()
                //Remove data from share ref
                requireContext().getSharedPreferences("PREFERENCE", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .remove(Constant.USER_INFO).apply()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }.setNegativeButton("Cancel", null)
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
    }
}