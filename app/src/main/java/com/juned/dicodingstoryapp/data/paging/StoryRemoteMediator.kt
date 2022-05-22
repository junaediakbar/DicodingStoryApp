package com.juned.dicodingstoryapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.juned.dicodingstoryapp.data.api.ApiService
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.data.database.RemoteKeyEntity
import com.juned.dicodingstoryapp.data.database.StoryDatabase
import com.juned.dicodingstoryapp.helper.wrapEspressoIdlingResource


@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val auth: String
) : RemoteMediator<Int, StoryItem>() {
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryItem>
    ): MediatorResult {
        wrapEspressoIdlingResource {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKey?.next?.minus(1) ?: INITIAL_PAGE
                }

                LoadType.PREPEND -> {
                    val remoteKey = getRemoteKeyForFirstItem(state)
                    remoteKey?.prev
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                }

                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)
                    remoteKey?.next
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                }
            }

            try {
                val response = apiService
                    .getAllStoriesPaged(auth, page, state.config.pageSize)
                    .listStory

                val endOfPagination = response.isEmpty()

                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        database.getRemoteKeyDao().deleteAllRemoteKeys()
                        database.getStoryDao().deleteAllStories()
                    }

                    val keys = response.map {
                        RemoteKeyEntity(
                            id = it.id,
                            prev = if (page == 1) null else page - 1,
                            next = if (endOfPagination) null else page + 1
                        )
                    }

                    database.getRemoteKeyDao().insertRemoteKeys(keys)
                    database.getStoryDao().insertStories(response)
                }

                return MediatorResult.Success(endOfPaginationReached = endOfPagination)
            } catch (ex: Exception) {
                return MediatorResult.Error(ex)
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryItem>) =
        state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { story ->
            database.getRemoteKeyDao().getRemoteKeyById(story.id)
        }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryItem>) =
        state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { story ->
            database.getRemoteKeyDao().getRemoteKeyById(story.id)
        }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryItem>) =
        state.anchorPosition?.let {
            state.closestItemToPosition(it)?.id?.let { id ->
                database.getRemoteKeyDao().getRemoteKeyById(id)
            }
        }

    companion object {
        const val INITIAL_PAGE = 1
    }
}