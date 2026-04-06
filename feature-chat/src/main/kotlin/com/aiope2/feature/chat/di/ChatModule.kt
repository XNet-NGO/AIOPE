package com.aiope2.feature.chat.di

import android.content.Context
import androidx.room.Room
import com.aiope2.feature.chat.db.ChatDao
import com.aiope2.feature.chat.db.ChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext ctx: Context): ChatDatabase =
    Room.databaseBuilder(ctx, ChatDatabase::class.java, "aiope2-chat.db").build()

  @Provides
  fun provideDao(db: ChatDatabase): ChatDao = db.chatDao()
}
