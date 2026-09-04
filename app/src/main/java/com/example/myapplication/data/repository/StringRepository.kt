package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.StringDao
import com.example.myapplication.data.entity.StringEntity
import kotlinx.coroutines.flow.Flow

/**
 * 文字列データにアクセスするリポジトリクラス
 *
 * リポジトリパターンを実装し、ViewModelとDAO間の中間層として機能します。
 * データアクセスの詳細をViewModelから隠蔽し、ビジネスロジックの
 * 分離とテスタビリティの向上を実現します。
 *
 * リポジトリパターンの利点:
 * - データソースの抽象化（データベース、ネットワーク、キャッシュなど）
 * - ViewModelからのデータアクセス詳細の隠蔽
 * - 複数のデータソースの統合
 * - ユニットテストでのモック作成の容易さ
 * - ビジネスロジックの集約
 *
 * アーキテクチャ上の位置:
 * ViewModel → Repository → DAO → Database
 *
 * 現在の実装:
 * - StringDaoへの単純な委譲
 * - 将来的にはキャッシュ機能やネットワーク処理の追加が可能
 *
 * @property stringDao StringEntityへのデータアクセスを提供するDAO
 */
interface StringRepository {
    fun getAllStrings(): Flow<List<StringEntity>>

    suspend fun getFirstString(): StringEntity?

    suspend fun insertString(string: StringEntity)

    suspend fun updateString(string: StringEntity)

    suspend fun deleteString(string: StringEntity)

    suspend fun deleteStringById(id: Int): Int

    suspend fun deleteAllStrings()
}

class RoomStringRepository(
    private val stringDao: StringDao,
) : StringRepository {
    /**
     * すべての文字列エンティティを取得する
     *
     * データベースに保存されているすべての文字列を取得します。
     * Flowを返すことで、データの変更をリアルタイムで監視できます。
     *
     * 使用場面:
     * - 文字列一覧の表示
     * - データの変更監視
     * - リアクティブUI の更新
     *
     * パフォーマンス考慮:
     * - Flowはコールドストリームのため、collect開始時にデータ取得開始
     * - データベースの変更時のみ新しい値を emit
     * - バックグラウンドスレッドで実行され、UIスレッドをブロックしない
     *
     * @return すべてのStringEntityのリストを含むFlow
     */
    override fun getAllStrings(): Flow<List<StringEntity>> = stringDao.getAllStrings()

    /**
     * 最初の文字列エンティティを取得する（「Android」文字列）
     *
     * データベースから1件のStringEntityを取得します。
     * 現在のアプリでは「Hello Android!」メッセージの表示に使用されます。
     *
     * 実装詳細:
     * - suspend関数のため、コルーチン内で呼び出す必要がある
     * - データが存在しない場合はnullを返す
     * - ViewModelで適切なエラーハンドリングが実装されている
     *
     * エラーハンドリング:
     * - データが存在しない場合: null を返す
     * - データベースエラー: 例外がスローされる（ViewModelで catch）
     *
     * @return 最初のStringEntity、存在しない場合はnull
     */
    override suspend fun getFirstString(): StringEntity? = stringDao.getFirstString()

    /**
     * 文字列エンティティを挿入する
     *
     * 新しいStringEntityをデータベースに保存します。
     * アプリの初期化時やデータの追加時に使用されます。
     *
     * 使用場面:
     * - アプリ初期化時の「Android」文字列挿入
     * - 新しい文字列データの追加
     * - データベースのシード処理
     *
     * トランザクション:
     * - Roomが自動的にトランザクションを管理
     * - 失敗時は自動ロールバック
     * - データの整合性を保証
     *
     * @param string 挿入するStringEntityオブジェクト
     *               例: StringEntity(value = "Android")
     */
    override suspend fun insertString(string: StringEntity) = stringDao.insertString(string)

    /**
     * 文字列エンティティを更新する
     *
     * 既存のStringEntityの内容をデータベース内で更新します。
     * 主キー（id）を使用して更新対象のレコードを特定し、valueフィールドを変更します。
     *
     * 使用場面:
     * - ユーザーが文字列を編集した場合
     * - 設定値の変更
     * - データの修正や訂正
     *
     * ビジネスロジック:
     * - 現在はDAOへの単純な委譲
     * - 将来的にはバリデーションやキャッシュ無効化の追加が可能
     * - 更新前後のデータ変換処理の追加も可能
     *
     * @param string 更新するStringEntityオブジェクト
     *               有効なidと新しいvalueを含む必要がある
     */
    override suspend fun updateString(string: StringEntity) = stringDao.updateString(string)

    /**
     * 特定の文字列エンティティを削除する
     *
     * 指定されたStringEntityをデータベースから削除します。
     * エンティティオブジェクトを使用した削除操作です。
     *
     * 使用場面:
     * - 特定のデータの削除
     * - ユーザーによる削除操作
     * - データの整理やクリーンアップ
     *
     * 実装詳細:
     * - @Deleteアノテーションを使用したRoom操作
     * - 主キー（id）による削除対象の特定
     * - トランザクションの自動管理
     *
     * @param string 削除するStringEntityオブジェクト
     */
    override suspend fun deleteString(string: StringEntity) = stringDao.deleteString(string)

    /**
     * IDで特定の文字列エンティティを削除する
     *
     * 指定されたIDの文字列エンティティをデータベースから削除します。
     * IDのみで削除できるため、効率的な削除操作が可能です。
     *
     * 使用場面:
     * - IDのみが分かっている場合の削除
     * - UIからの簡単な削除操作
     * - 一覧画面からの削除ボタン実装
     *
     * 戻り値の活用:
     * - 削除成功/失敗の判定
     * - UIへのフィードバック表示
     * - エラーハンドリングの実装
     *
     * @param id 削除対象の文字列エンティティのID
     * @return 削除されたレコード数（1: 成功、0: 対象なし）
     */
    override suspend fun deleteStringById(id: Int): Int = stringDao.deleteStringById(id)

    /**
     * すべての文字列エンティティを削除する
     *
     * データベース内のすべてのStringEntityを削除します。
     * データのリセットやクリーンアップ時に使用されます。
     *
     * 使用場面:
     * - データベースの完全リセット
     * - テスト時のデータクリーンアップ
     * - アプリの初期化処理
     *
     * 注意事項:
     * - この操作は元に戻すことができません
     * - 実行前にユーザーへの確認が推奨されます
     * - データのバックアップを考慮してください
     */
    override suspend fun deleteAllStrings() = stringDao.deleteAllStrings()
}
