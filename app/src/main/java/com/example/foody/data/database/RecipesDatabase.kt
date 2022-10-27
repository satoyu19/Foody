package com.example.foody.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.foody.data.database.entities.RecipesEntity

@Database(entities = [RecipesEntity::class], version = 1, exportSchema = false)
@TypeConverters(RecipesTypeConverter::class)    //定義したコンバータ クラスを Room が認識できる様にする
abstract class RecipesDatabase:  RoomDatabase() {

    abstract fun recipesDao(): RecipesDao
}