package com.example.foody.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

//ConnectivityManager →　ネットワーク接続の状態に関するクエリに応答するクラス。また、ネットワーク接続が変更されたときにアプリケーションに通知する。
//@ExperimentalCoroutinesApi　→　まだ実験段階のAPIで将来変更や廃止される可能性があることを示す、以前は必要だったか、現在は不要？
class NetworkListener : ConnectivityManager.NetworkCallback() {

    private val isNetworkAvailable = MutableStateFlow(false)

    fun checkNetworkAvailability(context: Context): MutableStateFlow<Boolean> {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(this)

        val network = connectivityManager.activeNetwork //現在アクティブなデフォルト データ ネットワークに対応するオブジェクト
        if (network == null) {  //デフォルト ネットワークがない場合、またはデフォルト ネットワークがブロックされている場合
            isNetworkAvailable.value = false
            return isNetworkAvailable
        }

        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network)   //指定された Network の NetworkCapabilities(そのネットワークに関する情報)、または null
        if (networkCapabilities == null) {
            isNetworkAvailable.value = false
            return isNetworkAvailable
        }

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                isNetworkAvailable.value = true
                isNetworkAvailable
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                isNetworkAvailable.value = true
                isNetworkAvailable
            }
            else -> {
                isNetworkAvailable.value = false
                isNetworkAvailable
            }
        }
    }

    //使用できる新しいネットワークを宣言したときに呼び出される。
    override fun onAvailable(network: Network) {
        super.onAvailable(network)

        isNetworkAvailable.value = true
    }

    //ネットワークが切断されたとき、またはこのリクエストまたはコールバックを満たさなくなったときに呼び出される。
    override fun onLost(network: Network) {
        super.onLost(network)

        isNetworkAvailable.value = false
    }
}