package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.entity.StringEntity
import kotlinx.coroutines.flow.Flow

/**
 * 文字列データにアクセスするためのDAO（Data Access Object）
 *
 * Roomデータベースライブラリを使用して、StringEntityテーブルへの
 * データアクセス操作を定義するインターフェースです。
 *
 * DAOパターンの利点:
 * - データベースアクセス操作の抽象化
 * - SQLクエリのコンパイル時検証
 * - 型安全なデータベース操作
 * - ユニットテストでのモック作成が容易
 *
 * 実装される操作:
 * - データの読み取り（Query）
 * - データの挿入（Insert）
 * - データの更新（Update）
 * - データの削除（Delete）
 *
 * 非同期処理:
 * - Flow: リアクティブなデータ監視
 * - suspend: コルーチンによる非同期実行
 */
@Dao
interface StringDao {
    /**
     * すべての文字列エンティティを取得する
     *
     * データベースの "strings" テーブルからすべてのレコードを取得します。
     * Flowを使用することで、データの変更を自動的に監視し、
     * UIに対してリアクティブな更新を提供します。
     *
     * 特徴:
     * - データが変更されると自動的に新しい値が通知される
     * - UIスレッドをブロックしない非同期処理
     * - リアルタイムでのデータ同期
     *
     * @return すべてのStringEntityのリストを含むFlow
     *         データが空の場合は空のリストが返される
     */
    @Query("SELECT * FROM strings")
    fun getAllStrings(): Flow<List<StringEntity>>

    /**
     * 最初の文字列エンティティを取得する（Android文字列用）
     *
     * データベースから1件のレコードのみを取得します。
     * 現在のアプリでは「Android」文字列を表示するために使用されています。
     *
     * LIMIT句を使用することで:
     * - データベースアクセスの効率化
     * - 必要最小限のデータ取得
     * - パフォーマンスの向上
     *
     * @return 最初のStringEntity、データが存在しない場合はnull
     */
    @Query("SELECT * FROM strings LIMIT 1")
    suspend fun getFirstString(): StringEntity?

    /**
     * 文字列エンティティを挿入する
     *
     * 新しいStringEntityをデータベースに追加します。
     * 主キー（id）は自動生成されるため、呼び出し側では
     * idを指定する必要はありません。
     *
     * トランザクション:
     * - Roomが自動的にトランザクションを管理
     * - 失敗時は自動的にロールバック
     * - データの整合性を保証
     *
     * @param string 挿入するStringEntityオブジェクト
     *               通常は StringEntity(value = "文字列") の形式で作成
     */
    @Insert
    suspend fun insertString(string: StringEntity)

    @Query("SELECT COUNT(*) FROM strings")
    suspend fun countStrings(): Int

    /**
     * 文字列エンティティを更新する
     *
     * 既存のStringEntityをデータベース内で更新します。
     * 主キー（id）を使用して更新対象のレコードを特定します。
     *
     * 使用場面:
     * - 既存の文字列データの変更
     * - ユーザーが入力した新しい値での更新
     * - 設定値の変更
     *
     * 注意事項:
     * - 更新対象のエンティティは有効なidを持つ必要がある
     * - 存在しないidでの更新は無視される
     *
     * @param string 更新するStringEntityオブジェクト
     *               例: StringEntity(id = 1, value = "新しい文字列")
     */
    @Update
    suspend fun updateString(string: StringEntity)

    /**
     * 特定の文字列エンティティを削除する
     *
     * 指定されたStringEntityをデータベースから削除します。
     * 主キー（id）を使用して削除対象のレコードを特定します。
     *
     * 使用場面:
     * - 特定のデータの削除
     * - ユーザーによる個別データの削除操作
     * - 不要になったデータの整理
     *
     * 注意事項:
     * - 削除対象のエンティティは有効なidを持つ必要がある
     * - 存在しないidでの削除は無視される
     * - この操作は元に戻すことができません
     *
     * @param string 削除するStringEntityオブジェクト
     *               例: StringEntity(id = 1, value = "削除対象")
     */
    @Delete
    suspend fun deleteString(string: StringEntity)

    /**
     * IDで特定の文字列エンティティを削除する
     *
     * 指定されたIDの文字列エンティティをデータベースから削除します。
     * IDのみで削除できるため、エンティティオブジェクト全体を保持する必要がありません。
     *
     * 使用場面:
     * - IDのみが分かっている場合の削除操作
     * - 効率的なデータ削除
     * - UIからの削除操作での簡単な実装
     *
     * @param id 削除対象の文字列エンティティのID
     * @return 削除されたレコード数（1: 成功、0: 対象なし）
     */
    @Query("DELETE FROM strings WHERE id = :id")
    suspend fun deleteStringById(id: Int): Int

    /**
     * すべての文字列エンティティを削除する
     *
     * データベースの "strings" テーブルからすべてのレコードを削除します。
     * 主にテスト時やデータのリセット時に使用されます。
     *
     * 用途例:
     * - アプリの初期化処理
     * - テストデータのクリーンアップ
     * - データベースのリセット機能
     *
     * 注意: この操作は元に戻すことができません
     */
    @Query("DELETE FROM strings")
    suspend fun deleteAllStrings()
}
