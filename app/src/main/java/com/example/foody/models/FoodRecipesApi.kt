package com.example.foody.models

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap
import java.net.ResponseCache

interface FoodRecipesApi {

    @GET("/recipes/complexSearch")
    suspend fun getRecipes(
        @QueryMap queries: Map<String, String>
    ): Response<FoodRecipe>
}