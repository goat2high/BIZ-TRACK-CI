package com.example.biztrackci.di

import android.content.Context
import androidx.room.Room
import com.example.biztrackci.data.local.dao.ExpenseDao
import com.example.biztrackci.data.local.dao.ProductDao
import com.example.biztrackci.data.local.dao.SaleDao
import com.example.biztrackci.data.local.db.BizTrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBizTrackDatabase(
        @ApplicationContext context: Context
    ): BizTrackDatabase {
        lateinit var database: BizTrackDatabase
        val scope = CoroutineScope(SupervisorJob())
        database = Room.databaseBuilder(
            context,
            BizTrackDatabase::class.java,
            BizTrackDatabase.DATABASE_NAME
        )
            .addCallback(BizTrackDatabase.getCallback(scope) { database })
            .fallbackToDestructiveMigration()
            .build()
        return database
    }

    @Provides
    fun provideProductDao(database: BizTrackDatabase): ProductDao = database.productDao()

    @Provides
    fun provideSaleDao(database: BizTrackDatabase): SaleDao = database.saleDao()

    @Provides
    fun provideExpenseDao(database: BizTrackDatabase): ExpenseDao = database.expenseDao()
}
