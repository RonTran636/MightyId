package com.mightyId.activities.onboarding

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.activities.login.LoginActivity
import com.mightyId.databinding.ActivityOnboardingBinding
import com.mightyId.utils.Common
import com.mightyId.utils.Constant.Companion.FIRST_RUN
import com.mightyId.adapters.SliderAdapter
import com.mightyId.utils.connectionLostSnackBar

class OnBoardingActivity : BaseActivity(), View.OnClickListener {

    private lateinit var viewPagerAdapter: SliderAdapter
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var completedOnBoarding: SharedPreferences
    private val onBoardSize = 3  // Number of onBoarding images
    private val dots = arrayOfNulls<ImageView>(onBoardSize)
    private val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT)
    private var proceed = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        completedOnBoarding = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        viewPagerAdapter = SliderAdapter(this)
        binding.onBoardViewPager.adapter = viewPagerAdapter
        //Initialize onBoarding image's counter
        for (i in 0 until onBoardSize) {
            dots[i] = ImageView(this)
        }
        addDot(0)
        layoutParams.setMargins(0, 0, 4, 0)

        binding.onBoardViewPager.addOnPageChangeListener(listener)
        binding.buttonExplore.setOnClickListener(this)
        binding.buttonSkip.setOnClickListener(this)
        proceed.observe(this){
            if (it==true){
                moveToHomeActivity()
            }
        }
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, theme)))
        }
        Common.isConnected.observe(this){
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
    }

    private val listener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {

        }

        override fun onPageSelected(position: Int) {
            addDot(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
        }
    }

    private fun addDot(position: Int) {
        binding.layoutDots.removeAllViews()
        for (i in 0 until onBoardSize) {
            dots[i]!!.setNonSelectedDot()
            binding.layoutDots.addView(dots[i], layoutParams)
        }
        dots[position]!!.setSelectedDot()
    }

    private fun ImageView.setNonSelectedDot() {
        this.setImageDrawable(
            ContextCompat.getDrawable(
                this@OnBoardingActivity,
                R.drawable.non_selected_dot
            )
        )
    }

    private fun ImageView.setSelectedDot() {
        this.setImageDrawable(
            ContextCompat.getDrawable(
                this@OnBoardingActivity,
                R.drawable.selected_dot
            )
        )
    }

    private fun moveToHomeActivity() {
        val editor: SharedPreferences.Editor = completedOnBoarding.edit()
        editor.putBoolean(FIRST_RUN, false)
        editor.apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkPermission() {
        if ((checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            || (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        ) {
            val listener = object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()){
                            proceed.value = true
                        }else{
                            checkPermission()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?,
                ) {
                    AlertDialog.Builder(this@OnBoardingActivity)
                        .setIcon(R.drawable.notification_icon)
                        .setTitle("Camera & audio permission")
                        .setMessage("In order to use this app, both camera and audio permission are needed. Please grant permission for the best experience.")
                        .setPositiveButton("Later") { _, _ -> proceed.value = true }
                        .show()
                }
            }
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                )
                .withListener(listener)
                .onSameThread()
                .check()
        } else {
            proceed.value = true
        }
    }

    override fun onClick(v: View) {
        when (v) {
            binding.buttonSkip -> checkPermission()
            binding.buttonExplore -> {
                if (binding.onBoardViewPager.currentItem < onBoardSize - 1) {
                    binding.onBoardViewPager.currentItem++
                } else {
                    checkPermission()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (binding.onBoardViewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.onBoardViewPager.currentItem--
        }
    }
}