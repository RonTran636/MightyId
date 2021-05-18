package com.mightyId.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.widget.ImageButton
import android.widget.ImageView
import androidx.camera.core.ImageProxy
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.mightyId.R
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter
import io.reactivex.rxjava3.core.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


fun getProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
}

/**
 * Display HTML Content
 */
fun getHTMLContent(url: String): Observable<Document> {
    return Observable.fromCallable {
        try {
            return@fromCallable Jsoup.connect(url).timeout(0).get()
        } catch (e: IOException) {
            Timber.tag("Error").e("getHTMLContent: ")
            throw RuntimeException(e)
        }

    }
}

/**
 *  convert image proxy to bitmap
 */
fun ImageProxy.imageProxyToBitmap(): Bitmap {
    val planeProxy = this.planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun ImageView.loadImage(url: String?, progressDrawable: CircularProgressDrawable?) {
    val option = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.ic_avatar_default)

    Glide.with(this.context)
        .setDefaultRequestOptions(option)
        .load(url)
        .into(this)
}

fun ImageView.loadImage(bitmap: Bitmap, progressDrawable: CircularProgressDrawable?) {
    val option = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.drawable.ic_avatar_default)

    Glide.with(this.context)
        .setDefaultRequestOptions(option)
        .load(bitmap)
        .into(this)
}

fun ImageView.loadImageWithRoundBorder(url: String?, progressDrawable: CircularProgressDrawable?) {
    val option = RequestOptions()
        .transform(RoundedCorners(45))
        .placeholder(progressDrawable)
        .error(R.drawable.ic_avatar_default)

    Glide.with(this.context)
        .setDefaultRequestOptions(option)
        .load(url)
        .circleCrop()
        .into(this)
}

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    val newBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
    newBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream)
    Timber.tag("bitmapToByteArray").d("bitmapToByteArray: ${stream.toByteArray()}")
    return stream.toByteArray()
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun decodeSampledBitmapFromResource(
    res: Resources,
    resId: Int,
    reqWidth: Int,
    reqHeight: Int,
): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, this)
        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false
        BitmapFactory.decodeResource(res, resId, this)
    }
}

fun Bitmap.resizeBitmap(maxSize: Int): Bitmap? {
    var width = this.width
    var height = this.height
    val x: Double
    if (width >= height && width > maxSize) {
        x = (width / height).toDouble()
        width = maxSize
        height = (maxSize / x).toInt()
    } else if (height >= width && height > maxSize) {
        x = (height / width).toDouble()
        height = maxSize
        width = (maxSize / x).toInt()
    }
    return Bitmap.createScaledBitmap(this, width, height, false)
}

fun getImageUri(context: Context, inImage: Bitmap): Uri? {
//    val bytes = ByteArrayOutputStream()
//    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//    val path = Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
//    // Store image in dcim
    val file =
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/DCIM/",
            "image" + Date().time.toString() + ".jpg")
    return Uri.fromFile(file)
}

fun Context.convertUriToBitmap(uri: Uri): Bitmap {
    return Glide.with(this)
        .asBitmap()
        .load(uri)
        .submit()
        .get()
}

fun LottieAnimationView.setLottieImage(rawSource: Int) {
    this.setAnimation(rawSource)
    this.playAnimation()
}

fun Context.loadImageToNotificationAvatar(url: String?): Bitmap? {
    return if (url.isNullOrBlank()) {
        Glide.with(applicationContext)
            .asBitmap()
            .load(R.drawable.ic_avatar_default)
            .circleCrop()
            .submit()
            .get()
    } else {
        Glide.with(applicationContext)
            .asBitmap()
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_default)
            .load(url)
            .submit(200, 200)
            .get()
    }
}

fun ImageButton.simulateClick(delay: Long = 50L) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

fun ImageView.displayTodoIcon(todoStatus: String) {
    when (todoStatus) {
        ChatRoomAdapter.TODO_PENDING -> {
            this.apply {
                setImageResource(android.R.color.transparent)
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.todo_pending,
                        this.context.theme))
            }
        }
        ChatRoomAdapter.TODO_CONFIRM -> {
            this.apply {
                setImageResource(android.R.color.transparent)
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.green,
                        this.context.theme))
            }
        }
        ChatRoomAdapter.TODO_REJECT -> {
            this.apply {
                setImageResource(android.R.color.transparent)
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.accent_red,
                        this.context.theme))
            }
        }
        ChatRoomAdapter.TODO_COMPLETE -> {
            this.apply {
                setImageResource(R.drawable.ic_baseline_check_12)
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.green,
                        this.context.theme))
            }
        }
        ChatRoomAdapter.TODO_FAILED -> {
            this.apply {
                setImageResource(R.drawable.ic_baseline_clear_12)
                backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.accent_red,
                        this.context.theme))
            }
        }
    }
}


