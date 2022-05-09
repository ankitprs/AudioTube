package tech.apps.music.database.network

import retrofit2.Response

data class SimpleResponse<T>(
    val status: Status,
    val data: Response<T>?,
    val exception: Exception?
) {
    companion object {
        fun <T> success(data: Response<T>): SimpleResponse<T> {
            return SimpleResponse(
                Status.Success,
                data,
                null
            )
        }
        fun <T> failure(exception: Exception): SimpleResponse<T> {
            return SimpleResponse(
                Status.Failure,
                null,
                exception
            )
        }
    }

    sealed class Status {
        object Success : Status()
        object Failure : Status()
    }

    private val failed: Boolean
        get() = this.status == Status.Failure

    val isSuccessful: Boolean
        get() = !failed && this.data?.isSuccessful == true

    val body: T
        get() = this.data!!.body()!!
}