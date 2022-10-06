package com.budgettracker.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.budgettracker.data.local.AppDatabase
import com.budgettracker.data.local.datastore.UIModeDataStore
import com.budgettracker.data.local.datastore.UIModeImpl
import com.budgettracker.services.exportcsv.ExportCsvService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun providePreferenceManager(@ApplicationContext context: Context): UIModeImpl {
        return UIModeDataStore(context)
    }

    @Singleton
    @Provides
    fun provideNoteDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "transaction.db")
            .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideExportCSV(@ApplicationContext context: Context): ExportCsvService {
        return ExportCsvService(appContext = context)
    }
}
