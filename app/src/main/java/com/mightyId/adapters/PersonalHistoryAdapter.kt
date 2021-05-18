package com.mightyId.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderPersonalHistoryCallBinding
import com.mightyId.utils.Constant
import com.mightyId.utils.TimeUtils
import com.mightyId.models.CallHistoryItems
import java.time.LocalTime

class PersonalHistoryAdapter(
    private var historyList: MutableList<CallHistoryItems>,
) : RecyclerView.Adapter<PersonalHistoryAdapter.ViewHolder>() {

    fun update(newList: MutableList<CallHistoryItems>) {
        historyList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_personal_history_call, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        historyList[position].let { holder.setData(it, position) }
    }

    override fun getItemCount(): Int = historyList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = HolderPersonalHistoryCallBinding.bind(itemView)

        fun setData(callHistory: CallHistoryItems, position: Int) {
            if (position == 0) {
                binding.separateLine.visibility = View.GONE
            }
            binding.historyPersonalType.text = callHistory.meetingType
            binding.historyPersonalTime.text =
                TimeUtils.displayTimeStatus(itemView.context, callHistory.timeCall)
            displayDuration(callHistory)
            displayCallStatus(callHistory)
        }

        private fun displayDuration(callHistory: CallHistoryItems) {
            if (callHistory.duration == null || callHistory.duration == "0") {
                callHistory.duration = "00:00:00"
            }
            val temp = LocalTime.parse(callHistory.duration)
            val hours = temp.hour
            val minutes = temp.minute
            val seconds = temp.second
            var duration =
                itemView.context.getString(R.string.call_duration, hours, minutes, seconds)
            if (hours == 0) {
                duration = duration.replace("0hrs", "")
                if (minutes == 0) {
                    duration = duration.replace("0mins", "")
                    if (seconds==0){
                        duration = ""
                    }
                }
            }
            binding.historyPersonalDuration.text = duration
        }

        private fun displayCallStatus(callHistory: CallHistoryItems) {
            if (callHistory.isRequestCall == true) {
                binding.historyPersonalTextStatus.text = "Outgoing call "
                binding.historyPersonalStatus.backgroundTintList =
                    ColorStateList.valueOf(binding.root.resources.getColor(R.color.accent_red,
                        binding.root.context.theme))
                binding.historyPersonalStatus.setImageResource(R.drawable.ic_baseline_arrow_back)
                binding.historyPersonalStatus.rotation = 135F
            } else {
                if (callHistory.callStatus == Constant.REMOTE_RESPONSE_MISSED) {
                    binding.historyPersonalTextStatus.text = "Missed call "
                    binding.historyPersonalStatus.backgroundTintList =
                        ColorStateList.valueOf(binding.root.resources.getColor(R.color.accent_red,
                            binding.root.context.theme))
                    binding.historyPersonalStatus.setImageResource(R.drawable.ic_call_missed)
                    binding.historyPersonalStatus.rotation = 0F
                } else {
                    binding.historyPersonalTextStatus.text = "Incoming call "
                    binding.historyPersonalStatus.backgroundTintList =
                        ColorStateList.valueOf(binding.root.resources.getColor(R.color.default_green,
                            binding.root.context.theme))
                    binding.historyPersonalStatus.setImageResource(R.drawable.ic_baseline_arrow_back)
                    binding.historyPersonalStatus.rotation = -45F
                }
            }
        }
    }


}