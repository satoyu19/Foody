package com.example.foody.data.database

import androidx.room.TypeConverter
import com.example.foody.models.FoodRecipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Roomが対応していない型を保存可能な方に変換するコンバーター*/
class RecipesTypeConverter {

    var gson = Gson()

    @TypeConverter   //型コンバーターである
    fun foodRecipeToString(foodRecipe: FoodRecipe): String{
        //オブジェクト　→　gson
        return gson.toJson(foodRecipe)
    }

    @TypeConverter
    fun stringToFoodRecipe(data: String): FoodRecipe {
        // 変換する型情報を用意する
        val listType = object: TypeToken<FoodRecipe>() {}.type
        //gson →　コレクションオブジェクト
        return gson.fromJson(data, listType)
    }
}