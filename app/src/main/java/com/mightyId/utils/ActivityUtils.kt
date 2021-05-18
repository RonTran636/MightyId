package com.mightyId.utils

import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.snackbar.Snackbar
import com.mightyId.R

fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
        }
    }
}

fun Activity.turnScreenOffAndKeyguardOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    } else {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.hideKeyboard() {
    val inputMethodManager =
        this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var currentView = this.currentFocus
    if (currentView == null) currentView = View(this)
    inputMethodManager.hideSoftInputFromWindow(currentView.windowToken, 0)
    currentView.clearFocus()
}

fun Activity.clearFocusOnOutsideClick() {
    currentFocus?.apply {
        if (this is EditText) {
            clearFocus()
        }
        hideKeyboard()
    }
}

fun View.showKeyboard() {
    this.requestFocus()
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(
        InputMethodManager.SHOW_FORCED,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
//    activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
}

/**Setting the color of status bar*/
@Suppress("DEPRECATION")
fun Activity.setLightStatusBar(isLightStatusBar: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val systemUiAppearance = if (isLightStatusBar) {
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        } else {
            0
        }
        window.insetsController?.setSystemBarsAppearance(systemUiAppearance,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
    } else {
        val systemUiVisibilityFlags = if (isLightStatusBar) {
            window.decorView.systemUiVisibility or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = systemUiVisibilityFlags
    }
}

fun NotificationManager.buildChannel(channelId:String, channelName:CharSequence, importance:Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, channelName, importance)
        createNotificationChannel(channel)
    }
}

fun Context.inDevelop(){
    AlertDialog.Builder(this)
        .setMessage("Feature are developing \uD83D\uDE48")
        .show()
}

fun ContentResolver.getFileName(fileUri: Uri): String {
    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}

fun View.snackbar(message: String,action:String?,event: View.OnClickListener?) {
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_LONG
    ).also {
        it.setAction(action,event)
            .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, context.theme)))
    }.show()
}

fun View.connectionLostSnackBar(): Snackbar{
    return Snackbar.make(
        this,
        context.getString(R.string.connection_lost),
        Snackbar.LENGTH_INDEFINITE
    )
}

internal fun wrapContent(): ViewGroup.LayoutParams {
    return ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
}

internal fun matchParent(): ViewGroup.LayoutParams {
    return ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
}


fun BadgeDrawable.displayBadge(badgeNumber: Int) {
    if (badgeNumber > 0) {
        this.apply {
            isVisible = true
            number = badgeNumber
        }
    } else {
        this.apply {
            isVisible = false
            clearNumber()
        }
    }
}

/**
 * Call this method (in onActivityCreated or later)
 * to make the dialog near-full screen.
 */
fun DialogFragment.setFullScreen() {
    dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}