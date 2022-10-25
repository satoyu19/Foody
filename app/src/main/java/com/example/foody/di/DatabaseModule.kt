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

    //Room.databaseBuilder →　public static <T extends RoomDatabase> RoomDatabase.Builder<T> databaseBuilder(
    //                        @NonNull Context context, @NonNull Class<T> klass, @NonNull String name)
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(context,
        RecipesDatabase::class.java,
        DATABASE_NAME).build()

    /** provideDatabaseを利用して、依存注入を行う**/
    @Singleton
    @Provides
    fun provideDao(database: RecipesDatabase) = database.recipesDao()
}