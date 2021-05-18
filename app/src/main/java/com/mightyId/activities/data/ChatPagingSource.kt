package com.mightyId.activities.data
//
//import androidx.paging.ExperimentalPagingApi
//import androidx.paging.PagingState
//import androidx.paging.rxjava3.RxPagingSource
//import com.mightyId.apiCentral.ServiceCentral
//import com.mightyId.models.MessageItem
//import io.reactivex.rxjava3.core.Single
//import io.reactivex.rxjava3.schedulers.Schedulers
//
//const val STARTING_INDEX = 1
//
//@ExperimentalPagingApi
//class ChatPagingSource(
//    private val serviceCentral: ServiceCentral,
//    private val topicId:String,
//    private val lastMessageId: Int
//): RxPagingSource<Int, MessageItem>(){
//
//    override fun getRefreshKey(state: PagingState<Int, MessageItem>): Int? {
//        TODO("Not yet implemented")
//    }
//
//    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, MessageItem>> {
//       return serviceCentral.getMessage(topicId,lastMessageId)
//           .subscribeOn(Schedulers.io())
//           .map {
//               LoadResult.Page(
//                   data = it.result,
//                   prevKey = if (params.key == null) null else params.key   ,
//                   nextKey = if (it.lastMessageId == 0) null else it.lastMessageId
//               )
//           }
//           .onErrorReturn {
//                LoadResult.Error(it)
//           }
//    }
//}