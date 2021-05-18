package com.mightyId.callback

import androidx.databinding.ViewDataBinding
import com.mightyId.models.Account

interface ItemClickListener {
    fun onItemLongClick(account: Account, binding: ViewDataBinding)
    fun onItemClick(account: Account, binding: ViewDataBinding)
}