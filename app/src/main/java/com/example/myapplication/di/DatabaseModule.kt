package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.dao.StringDao
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repository.RoomStringRepository
import com.example.myapplication.data.repository.StringRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * ApplicationScopeは、
 * HiltのDIでアプリケーション全体で共有する依存関係（例：CoroutineScope）を識別するためのQualifierです。
 * 同じ型の依存関係が複数ある場合、@ApplicationScopeを付与することで、どの依存関係を注入するか明示できます。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * データベース関連の依存関係を提供するHiltモジュール
 *
 * このモジュールは以下のデータベース関連依存関係を提供します：
 * - AppDatabaseインスタンス（Roomデータベース）
 * - StringDao（Roomが生成するDAO実装）
 * - StringRepository（リポジトリパターンの実装）
 *
 * すべての依存関係はシングルトンとして提供され、以下を保証します：
 * - アプリ全体で単一のデータベースインスタンス
 * - 効率的なリソース利用
 * - データの一貫性
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * アプリケーションスコープのCoroutineScopeをシングルトンで提供
     *
     * アプリ全体で利用可能なCoroutineScopeをシングルトンとして生成します。
     * データベース操作やその他のバックグラウンド処理に利用できます：
     * - SupervisorJobを使用し、子コルーチンの失敗が他に影響しないようにします
     * - データベースやネットワーク処理に最適化されたIOディスパッチャを使用します
     * - シングルトンスコープで効率的なリソース利用を実現します
     *
     * @return バックグラウンド処理用のアプリケーションスコープCoroutineScope
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * メインのアプリケーションデータベースインスタンスを提供
     *
     * 適切な設定でRoomデータベースインスタンスを生成します：
     * - メモリリーク防止のためアプリケーションコンテキストを使用
     * - アプリ全体で共有するためシングルトンスコープ
     * - 初期データセットアップ用のコールバックにスコープを注入
     *
     * @param context データベース作成用のアプリケーションコンテキスト
     * @param scope データベース初期化用のアプリケーションスコープCoroutineScope
     * @return アプリ全体で利用するAppDatabaseインスタンス
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): AppDatabase = AppDatabase.getDatabase(context, scope)

    /**
     * StringDaoの実装を提供
     *
     * Roomが自動生成するDAO実装をデータベースインスタンスから取得して返します。
     *
     * @param database Roomデータベースインスタンス
     * @return Roomが生成したStringDao実装
     */
    @Provides
    fun provideStringDao(database: AppDatabase): StringDao = database.stringDao()

    /**
     * StringRepositoryインスタンスを提供
     *
     * ViewModelとDAOの仲介役となるリポジトリを生成します。
     * リポジトリパターンを実装し、データアクセスを抽象化します。
     *
     * @param stringDao Roomが生成したStringDao実装
     * @return データアクセス用のStringRepository
     */
    @Provides
    fun provideStringRepository(stringDao: StringDao): StringRepository = RoomStringRepository(stringDao)

    @Provides
    @Singleton
    fun provideErrorLogger(): ErrorLogger = AndroidErrorLogger()
}
