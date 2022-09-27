package com.example.foody.di

import android.content.Context
import androidx.room.Room
import com.example.foody.data.database.RecipesDatabase
import com.example.foody.util.Constants.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    //databaseの依存注入
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(context,
        RecipesDatabase::class.java,
        DATABASE_NAME).build()

    //database_daoの依存注入
    @Singleton
    @Provides
    fun provideDao(database: RecipesDatabase) = database.recipesDao()
}