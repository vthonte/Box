package com.google.ai.edge.gallery.feature.jarvis.di

import android.content.Context
import com.google.ai.edge.gallery.feature.jarvis.memory.JarvisDatabase
import com.google.ai.edge.gallery.feature.jarvis.memory.JarvisMemoryDao
import com.google.ai.edge.gallery.feature.jarvis.tools.AlarmTool
import com.google.ai.edge.gallery.feature.jarvis.tools.CallTool
import com.google.ai.edge.gallery.feature.jarvis.tools.FlashlightTool
import com.google.ai.edge.gallery.feature.jarvis.tools.OpenAppTool
import com.google.ai.edge.gallery.feature.jarvis.tools.OpenSettingsTool
import com.google.ai.edge.gallery.feature.jarvis.tools.ShareTool
import com.google.ai.edge.gallery.feature.jarvis.tools.Tool
import com.google.ai.edge.gallery.feature.jarvis.voice.SimpleWakeWordEngine
import com.google.ai.edge.gallery.feature.jarvis.voice.SystemTtsEngine
import com.google.ai.edge.gallery.feature.jarvis.voice.TtsEngine
import com.google.ai.edge.gallery.feature.jarvis.voice.WakeWordEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

/**
 * Hilt module for Jarvis feature components.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class JarvisModule {

    @Binds
    @Singleton
    abstract fun bindWakeWordEngine(impl: SimpleWakeWordEngine): WakeWordEngine

    @Binds
    @IntoSet
    abstract fun bindFlashlightTool(tool: FlashlightTool): Tool

    @Binds
    @IntoSet
    abstract fun bindOpenSettingsTool(tool: OpenSettingsTool): Tool

    @Binds
    @IntoSet
    abstract fun bindAlarmTool(tool: AlarmTool): Tool

    @Binds
    @IntoSet
    abstract fun bindCallTool(tool: CallTool): Tool

    @Binds
    @IntoSet
    abstract fun bindOpenAppTool(tool: OpenAppTool): Tool

    @Binds
    @IntoSet
    abstract fun bindShareTool(tool: ShareTool): Tool

    companion object {
        @Provides
        @Singleton
        fun provideJarvisDatabase(@ApplicationContext context: Context): JarvisDatabase {
            return JarvisDatabase.getInstance(context)
        }

        @Provides
        fun provideJarvisMemoryDao(db: JarvisDatabase): JarvisMemoryDao {
            return db.memoryDao()
        }
    }
}
