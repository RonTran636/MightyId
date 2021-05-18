package com.mightyId.utils

class Constant {
    companion object {
        const val USER_INFO = "Users"
        const val TOPIC = "topic"
        const val TAG = "tag"
        const val INVITE_USER_INFO = "Invited user"
        const val FIRST_RUN = "first run"
        const val CHANNEL_CALL_ID = "Calls"
        const val CAMERA_REQUEST_CODE = 100
        const val WRITE_EXTERNAL_REQUEST_CODE = 1001
        const val CHANNEL_FRIEND_ID = "Friends"
        const val MEETING_ID = "Conference service"
        const val MEETING_IN_PROGRESS = 1005
        const val MESSAGE_REQUEST_CODE = 1006
        const val NAVIGATE_FROM = "Navigate from"
        const val FRAGMENT_SIGN_UP ="FragmentSignUp"
        const val FRAGMENT_FORGOT_PASSWORD = "FragmentForgotPassword"
        const val CHAT_ROOM_INFO = "MessageInfo"
        const val TOPIC_INFO = "TopicInfo"
        const val OUTPUT_PATH = "images_folder"

        //Common information use
        const val ON_CHAT : String ="onChat"
        const val IS_CALLING = "isCalling"

        //Privacy mode:
        const val PRIVACY_PRIVATE = "private"
        const val PRIVACY_PUBLIC = "public"

        //URL for product
        const val MEET_URL = "https://r.workid.ca/"
        const val TERM_AND_PRIVACY="https://workid.ca/policy"
        const val PRODUCTION_BASE_URL = "https://workid.ca"
        const val PRODUCTION_SOCKET_URL = "https://s.workid.ca"

        //URL for debugging
        const val DEBUGGING_BASE_URL = "https://api.dev.workid.ca/"
        const val DEBUGGING_SOCKET_URL = "https://s.dev.workid.ca"

        //Type of FCM messages:
        const val MESSAGE_TYPE = "messageType"
        const val CALL_REQUEST = "RequestCall"
        const val CALL_RESPONSE = "ResponseCall"
        const val EXISTING_CALL_REQUEST = "RequestJoinCall"
        const val EXISTING_CALL_RESPONSE = "ResponseJoinCall"
        const val ADD_FRIEND = "AddFriendRequest"
        const val CHAT_REQUEST = "RequestChat"
        const val ADD_FRIEND_ACCEPTED = "AcceptFriendRequest"
        const val MEMBER_IN_TOPIC = "MemberInCall"
        const val ASSIGN_TASK = "CreateTodo"
        const val CALL_ACCEPTED_ELSE_WHERE = "ClosePopupCall"

        //Request call
        const val REMOTE_MSG_CALLER_INFO = "data"
        const val REMOTE_MSG_CALLER_NAME = "callerName"
        const val REMOTE_MSG_CALLER_EMAIL = "callerEmail"
        const val REMOTE_MSG_MEETING_TYPE = "meetingType"
        const val REMOTE_MSG_GROUP_CALL = "listGroupCall"

        //Response Call
        const val REMOTE_RESPONSE_ACCEPTED = "answered"
        const val REMOTE_RESPONSE_REJECTED = "declined"
        const val REMOTE_RESPONSE_MISSED = "missed"
        const val REMOTE_NOTIFICATION_ACCEPTED = "notification_accepted"
        const val REMOTE_NOTIFICATION_REJECTED = "notification_rejected"
        const val REMOTE_NOTIFICATION_ACTION_CALLBACK = "callback"
        const val REMOTE_NOTIFICATION_CANCEL = "cancel notification"

        //Response Existed Call
        const val REMOTE_MSG_EXISTING_CALL_CANCEL= "CancelRequestJoinCall"
        const val REMOTE_RESPONSE_ALLOWED = "allowed"
        const val REMOTE_RESPONSE_DECLINED = "declined"

        //Request Add Friend
        const val REMOTE_MSG_ADD_FRIEND_ACCEPTED = "ADD_FRIEND_ACCEPTED"
        const val REMOTE_MSG_ADD_FRIEND_REJECTED = "ADD_FRIEND_REJECTED"

        //JitsiMeetUserInfo
        const val JITSI_DISPLAY_NAME = "displayName"
        const val JITSI_EMAIL = "email"
        const val JITSI_PHOTO_URL = "avatar"

        //Notification
        const val NOTIFICATION_TAG = "Notification"
        const val NOTIFICATION_REQUEST_CALL_ID = 1001
        const val NOTIFICATION_MESSAGE_ID = 200
        const val NOTIFICATION_REQUEST_ADD_FRIEND_ID = 1005
        const val NOTIFICATION_MISSED_CALL_ID = 1006
        const val NOTIFICATION_REQUEST_EXIST_CALL = 1002
        const val NOTIFICATION_REQUEST_CALL_DURATION :Int = 60000
        val NOTIFICATION_VIBRATE_PATTERN = longArrayOf(0, 100, 1000, 300, 200, 100, 500, 200, 100)
        //Notification Channel
        const val CHANNEL_NAME_CHAT = "Chat messages"
        const val CHANNEL_NAME_INCOMING_CALL = "Incoming calls"

        //Server Constants
        const val USER_EXISTED = "Existed"

        //Friend status
        const val FRIEND_STATUS = "Friend requests"
        const val ACTION_CANCEL_FRIEND_REQUEST = "Cancel request"
        const val ACTION_ACCEPT_FRIEND_REQUEST = "Accept"
        const val ACTION_DECLINE_FRIEND_REQUEST = "Decline"
        const val FRIEND_STATUS_NEUTRAL = 0
        const val FRIEND_STATUS_WAITING_FOR_RESPONSE = 1
        const val FRIEND_STATUS_PENDING = 2
        const val FRIEND_STATUS_ACCEPTED = 3

        //
        const val PENDING_FRIEND_LIST = "pending"

        //Message Constant
        const val MESSAGE_TOPIC_ID="topic_id"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val DIRECT_REPLY = "direct_reply"
        const val MESSAGE_WEB_URL = "messageUrl"
        const val NEW_MESSAGE = "newMessage"

        //Pin Constants
        const val PIN_TYPE_MESSAGE = "message"
        const val PIN_TYPE_TOPIC = "topic"
        const val PINT_TYPE_PERSON = "person"
        const val HEADER = "header"

        //Store Media
        const val PAGE_NUMBER = "PageNumber"
    }
}