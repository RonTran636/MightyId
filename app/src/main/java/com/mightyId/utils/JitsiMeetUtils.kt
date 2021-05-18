package com.mightyId.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mightyId.activities.call.MeetingActivity
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_ID
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_TYPE
import com.mightyId.models.RequestCall
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

object JitsiMeetUtils {
    fun establishConnection(jitsiMeetUrl:String) {
        val serverURL: URL = try {
            URL("https://$jitsiMeetUrl/")
        } catch (e: MalformedURLException) {
            Timber.tag("JitsiMeetUtils").e("establishConnection: $e")
            throw MalformedURLException(e.message)
        } catch (e: Exception){
            Timber.tag("JitsiMeetUtils").e("establishConnection: $e")
            throw RuntimeException(e.message)
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            .setWelcomePageEnabled(false)
            .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)
    }

    fun configurationMeeting(userData: RequestCall, meetingType: String, meetingID: String)
    : JitsiMeetConferenceOptions {
        val bundle = Bundle()
        bundle.putString(Constant.JITSI_DISPLAY_NAME, userData.callerName)
        bundle.putString(Constant.JITSI_EMAIL, userData.callerEmail)
        bundle.putString(Constant.JITSI_PHOTO_URL, userData.callerPhotoURL)
        val jitsiUser = JitsiMeetUserInfo(bundle)
        val jitsiConnection = JitsiMeetConferenceOptions.Builder()
            .setUserInfo(jitsiUser)
            .setRoom(meetingID)
            //Feature Flags:
            .setFeatureFlag("conference-timer.enabled",true)
            .setFeatureFlag("chat.enabled",false)
            .setFeatureFlag("meeting-name.enabled",false)
            .setFeatureFlag("meeting-password.enabled",false)
            .setFeatureFlag("recording.enabled",false)
            .setFeatureFlag("live-streaming.enabled",false)
            .setFeatureFlag("recording.enabled",false)
            .setFeatureFlag("raise-hand.enabled",false)
            .setFeatureFlag("video-share.enabled",false)

        if (meetingType == "audio") {
            jitsiConnection.setVideoMuted(true)
        }
        return jitsiConnection.build()
    }

    fun configurationMeeting(userData: RequestCall): JitsiMeetConferenceOptions {
        val bundle = Bundle()
        bundle.putString(Constant.JITSI_DISPLAY_NAME, userData.callerName)
        bundle.putString(Constant.JITSI_EMAIL, userData.callerEmail)
        bundle.putString(Constant.JITSI_PHOTO_URL, userData.callerPhotoURL)
        val jitsiUser = JitsiMeetUserInfo(bundle)
        val jitsiConnection = JitsiMeetConferenceOptions.Builder()
            .setUserInfo(jitsiUser)
            .setRoom(userData.meetingId)
            //Feature Flags:
            .setFeatureFlag("conference-timer.enabled",true)
            .setFeatureFlag("chat.enabled",false)
            .setFeatureFlag("kick-out.enabled",false)
            .setFeatureFlag("meeting-name.enabled",false)
            .setFeatureFlag("meeting-password.enabled",false)
            .setFeatureFlag("recording.enabled",false)
            .setFeatureFlag("live-streaming.enabled",false)
            .setFeatureFlag("recording.enabled",false)
            .setFeatureFlag("raise-hand.enabled",false)
            .setFeatureFlag("video-share.enabled",false)

        if (userData.meetingType == "audio") {
            jitsiConnection.setVideoMuted(true)
        }
        return jitsiConnection.build()
    }

    fun launch(context: Context, options: JitsiMeetConferenceOptions,callId:String,topicId:String, topicType:String){
        Timber.tag("JitsiMeetUtils").d("launch: topicId: $topicId")
        val intent = Intent(context, MeetingActivity::class.java)
        intent.putExtra("JitsiMeetConferenceOptions",options)
        intent.putExtra("call_id",callId)
        intent.putExtra(TOPIC_ID,topicId)
        intent.putExtra(TOPIC_TYPE,topicType)
        intent.action = "org.jitsi.meet.CONFERENCE"
        context.startActivity(intent)
    }
}