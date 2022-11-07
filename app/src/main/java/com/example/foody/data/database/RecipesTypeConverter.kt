package com.example.foody.data.database

import androidx.room.TypeConverter
import com.example.foody.models.FoodRecipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.foody.models.Result

/** Roomが対応していない型を保存可能な方に変換するコンバーター*/
class RecipesTypeConverter {

    var gson = Gson()

    //FoodRecipe型はString型に変換して挿入する
    @TypeConverter   //型コンバーターである
    fun foodRecipeToString(foodRecipe: FoodRecipe): String{
        //オブジェクト　→　gson
        return gson.toJson(foodRecipe)
    }

    //FoodRecipe型に戻して取得する
    @TypeConverter
    fun stringToFoodRecipe(data: String): FoodRecipe {
        // 変換する型情報を用意する
        val listType = object: TypeToken<FoodRecipe>() {}.type
        //gson →　コレクションオブジェクト
        return gson.fromJson(data, listType)
    }

    //Result型はString型に変換して挿入する
    @TypeConverter
    fun resultToString(result: Result): String {
        return gson.toJson(result)
    }

    //result型に戻して取得する
    @TypeConverter
    fun stringToResult(data: String): Result {
        val listType = object: TypeToken<Result>(){}.type
        return gson.fromJson(data, listType)
    }
}