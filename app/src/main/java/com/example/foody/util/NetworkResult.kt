package com.example.foody.util

    /** ネットワーク通信の結果 **/
sealed class NetworkResult<T>(val data: T? = null, val message: String? = null){    //ジェネリクスのNullが許容される書き方、<T: Any>にすると許容しない

    class Success<T>(data: T): NetworkResult<T>(data)
    class Error<T>(message: String?, data: T? = null): NetworkResult<T>(data, message)
    class Loading<T>: NetworkResult<T>()
}