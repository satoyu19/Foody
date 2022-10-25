package com.example.foody.di

import com.example.foody.util.Constants.Companion.BASE_URL
import com.example.foody.data.network.FoodRecipesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient{
        return OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)      //新規接続時のデフォルトの読み込みタイムアウト
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory{
        return GsonConverterFactory.create()
    }

    @Singleton
    @Provides
    fun provideRetrofitInstance(okHttpClient: OkHttpClient, gsonConverterFactory: GsonConverterFactory): Retrofit{  //gsonConverterFactoryはprovideConverterFactory()から依存性注入
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)       //リクエストに使用されるHTTPクライアント
            .addConverterFactory(gsonConverterFactory)      //オブジェクトのシリアライズ、デシリアライズのためのコンバータファクトリ
            .build()
    }

    /**　上のモジュールを使用して、FoodRecipesApiを必要とするクラスに依存関係注入する**/
    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): FoodRecipesApi {  //retrofitはprovideRetrofitInstanceから依存性注入
        return retrofit.create(FoodRecipesApi::class.java)
    }

}