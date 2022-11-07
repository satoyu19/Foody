package com.example.foody.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.foody.data.database.entities.FavoritesEntity
import com.example.foody.data.database.entities.RecipesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipesDao {

    //Flowが返るものはオブザーバブル クエリ。クエリで参照されるテーブルが変更されるたびに新しい値を出力する読み取りオペレーション
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insetRecipes(recipesEntity: RecipesEntity)

    /** FlowとRoomを連携するため、戻り値をFlowとする。一連をLiveDataに変えても変わらない。**/
    @Query("SELECT * FROM recipes_table ORDER BY id ASC")
    fun readRecipes(): Flow<List<RecipesEntity>>

    //お気にいりレシピの挿入
    @Insert(onConflict = OnConflictStrategy.REPLACE)    //重複した物を挿入酢用とした場合、それを置き換える
    suspend fun insertFavoriteRecipe(favoritesEntity: FavoritesEntity)

    @Query("SELECT * FROM favorite_recipes_table ORDER BY id ASC")
    fun readFavoriteRecipes(): Flow<List<FavoritesEntity>>

    @Delete
    suspend fun deleteFavoriteRecipe(favoritesEntity: FavoritesEntity)

    @Query("DELETE FROM favorite_recipes_table")
    suspend fun deleteAllFavoriteRecipes()
}