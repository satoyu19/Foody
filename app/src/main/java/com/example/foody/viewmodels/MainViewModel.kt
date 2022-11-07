package com.example.foody.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.*
import com.example.foody.data.Repository
import com.example.foody.data.database.entities.FavoritesEntity
import com.example.foody.data.database.entities.RecipesEntity
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
        //ローカルに保存されているレシピ一覧、お気に入りレシピ一覧
    val readRecipes: LiveData<List<RecipesEntity>> = repository.local.readRecipes().asLiveData()
    val readFavoriteRecipes: LiveData<List<FavoritesEntity>> = repository.local.readFavoriteRecipes().asLiveData()

        //リモートで取得したレシピ一覧をローカルに保存
    private fun insertRecipes(recipesEntity: RecipesEntity) = viewModelScope.launch {
            repository.local.insertRecipes(recipesEntity)
    }

    //お気に入りレシピの挿入
    fun insertFavoriteRecipes(favoritesEntity: FavoritesEntity) = viewModelScope.launch {
        repository.local.insertFavoriteRecipes(favoritesEntity)
    }
    //お気に入りレシピの削除
    fun deleteFavoriteRecipes(favoritesEntity: FavoritesEntity) = viewModelScope.launch {
        repository.local.deleteFavoriteRecipe(favoritesEntity)
    }
    //お気に入りレシピを全て削除
    fun deleteAllFavoriteRecipes() = viewModelScope.launch {
        repository.local.deleteAllFavoriteRecipes()
    }

    /** RETROFIT */
    var recipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()
    var searchRecipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()

        //リモートからレシピ一覧を取得(非同期)
    fun getRecipes(queries: Map<String, String>) = viewModelScope.launch {
        getRecipesSafeCall(queries)
    }

    fun searchRecipes(searchQuery: Map<String, String>) = viewModelScope.launch {
        searchRecipesSafeCall(searchQuery)
    }

        //検索によるAPI通信
    private suspend fun searchRecipesSafeCall(searchQuery: Map<String, String>) {
            if (hasInternetConnection()){
                try {
                    /** ①エラーが発生する可能性があるのはどこ？
                     * Response型で包めば、HTTPのstatus Code的に失敗だとしても例外が発生しないらしいから、②ではない？
                     * (ネットワークエラーなどの場合、例外になるっぽい)　**/
                    val response = repository.remote.searchRecipes(searchQuery)    //②ここ？
                    searchRecipesResponse.value = handleFoodRecipesResponse(response)

                }catch (e: Exception){
                    searchRecipesResponse.value = NetworkResult.Error("Recipes not found")
                }
            }else {
                searchRecipesResponse.value = NetworkResult.Error ("No Internet Connection")
            }
    }

    //API通信によるレシピ取得
    private suspend fun getRecipesSafeCall(queries: Map<String, String>) {
            //呼び出してすぐはロードとする
        recipesResponse.value = NetworkResult.Loading()
        //ネットワーク状況の確認
        if (hasInternetConnection()){
            try {
                /** ①エラーが発生する可能性があるのはどこ？
                 * Response型で包めば、HTTPのstatus Code的に失敗だとしても例外が発生しないらしいから、②ではない？
                 * (ネットワークエラーなどの場合、例外になるっぽい)　**/
                val response = repository.remote.getRecipes(queries)    //②ここ？
                recipesResponse.value = handleFoodRecipesResponse(response)

                val foodRecipe = recipesResponse.value!!.data
                if (foodRecipe != null){    //データがある場合、オフラインキャッシュ(データベースに登録しておく)
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
        val recipesEntity = RecipesEntity(foodRecipe)   //挿入データ
        insertRecipes(recipesEntity)    /** データベース挿入*/
    }

    //非同期処理に成功した場合はResponseがsuccessとなる？↓
    // public static <T> Response<T> success(@Nullable T body) { return success(body,~)
    private fun handleFoodRecipesResponse(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {
        when{
            response.message().toString().contains("timeout") -> {      //408 Request Timeout ←　左記の様な場合？(Timeoutがレスポンスの文字列に含まれている)
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited")
            }
            response.body()!!.results.isNullOrEmpty() -> {
                Log.e("Error", "handle")
                return NetworkResult.Error("Recipes not found")
            }
            response.isSuccessful -> {      //code()が[200..300]の範囲にあるとき、true を返す。
                val foodRecipes = response.body()   //レスポンスのボディをデシリアライズしたもの(バイトコードからオブジェクトに復元)
                return NetworkResult.Success(foodRecipes!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }

    /** ネットワーク接続確認、インターネット接続が利用可能な場合はtrueを返す **/
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