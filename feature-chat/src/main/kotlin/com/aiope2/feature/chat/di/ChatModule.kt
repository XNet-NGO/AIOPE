package com.aiope2.feature.chat.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aiope2.feature.chat.db.ChatDao
import com.aiope2.feature.chat.db.ChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE messages ADD COLUMN imagePaths TEXT NOT NULL DEFAULT ''")
  }
}

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {
  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext ctx: Context): ChatDatabase =
    Room.databaseBuilder(ctx, ChatDatabase::class.java, "aiope2-chat.db")
      .addMigrations(MIGRATION_1_2)
      .build()

  @Provides
  fun provideDao(db: ChatDatabase): ChatDao = db.chatDao()
}
