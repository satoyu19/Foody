package com.example.foody.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RecipesEntity::class], version = 1, exportSchema = false)
@TypeConverters(RecipesTypeConverter::class)    //定義したコンバータ クラスを Room が認識できる様にする
abstract class RecipesDatabase:  RoomDatabase() {

    abstract fun recipesDao(): RecipesDao
}