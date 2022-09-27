package com.example.foody.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.*
import com.example.foody.data.Repository
import com.example.foody.data.database.RecipesEntity
import com.example.foody.models.FoodRecipe
import com.example.foody.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (private val repository: Repository, application: Application): AndroidViewModel(application) {

    /** ROOM DATABASE*/

    val readRecipes: LiveData<List<RecipesEntity>> = repository.local.readDatabase().asLiveData()

    private fun insertRecipes(recipesEntity: RecipesEntity) = viewModelScope.launch {
        repository.local.insertRecipes(recipesEntity)
    }

    /** RETROFIT */
    var recipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()

    fun getRecipes(queries: Map<String, String>) = viewModelScope.launch {
        getRecipesSafeCall(queries)
    }

    private suspend fun getRecipesSafeCall(queries: Map<String, String>) {
        recipesResponse.value = NetworkResult.Loading()
        //ネットワーク状況の確認
        if (hasInternetConnection()){
            try {
                val response = repository.remote.getRecipes(queries)
                recipesResponse.value = handleFoodRecipesResponse(response)

                val foodRecipe = recipesResponse.value!!.data
                if (foodRecipe != null){    //データがある場合、オフラインキャッシュ
                    offlineCacheRecipes(foodRecipe)
                }
            }catch (e: Exception){
                recipesResponse.value = NetworkResult.Error("Recipes not found")
            }
        }else {
            recipesResponse.value = NetworkResult.Error ("No Internet Connection")
        }
    }

    private fun offlineCacheRecipes(foodRecipe: FoodRecipe) {
        val recipesEntity = RecipesEntity(foodRecipe)
        insertRecipes(recipesEntity)    /** データベース挿入*/
    }

    //todo: body()の中身がどうなっているのかログで確認する
    private fun handleFoodRecipesResponse(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {
        when{
            response.message().toString().contains("timeout") -> {      //408 Request Timeout ←　左記の様な場合？(Timeoutがレスポンスの文字列に含まれている)
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited")
            }
            response.body()!!.results.isNullOrEmpty() -> {      //body() →　レスポンスのデシリアライズ(バイトコードからオブジェクトに復元)
                Log.e("Error", "handle")
                return NetworkResult.Error("Recipes not found")
            }
            response.isSuccessful -> {      //code()が[200..300]の範囲にあるとき、true を返す。
                val foodRecipes = response.body()
                return NetworkResult.Success(foodRecipes!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    //ネットワーク接続確認、インターネット接続が利用可能な場合はtrueを返す
    private fun hasInternetConnection(): Boolean {
        //Context.CONNECTIVITY_SERVICE →　getSystemService(String)と共に使用し、ネットワーク接続の管理を行うためのandroid.net.ConnectivityManagerを取得することができます。
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //アプリの現在のデフォルト ネットワークへの参照を取得
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        //ネットワークへの参照を指定することで、アプリはそのネットワークに関する情報をクエリできる
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        //ネットワークに特定のトランスポートがあるかどうかを確認
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities. TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}