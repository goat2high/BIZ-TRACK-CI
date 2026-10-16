package com.example.biztrackci.di

import com.example.biztrackci.data.repository.BizTrackRepositoryImpl
import com.example.biztrackci.domain.repository.BizTrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBizTrackRepository(
        impl: BizTrackRepositoryImpl
    ): BizTrackRepository
}
