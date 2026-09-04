---
name: Androidアプリ初期設定テンプレ
about: GitHub Copilot Coding Agent への指示文
---
# 概要
- このリポジトリは `basic-android-app` を元に作成されたAndroidアプリです。下記の手順に従って、このリポジトリ専用の初期設定を行ってください。

# 内容

- アプリのバージョンはセマンティックバージョニングで管理し、初期バージョンを `0.1.0`、`versionCode` を `1` に設定してください
- `CHANGELOG.md` が存在する場合は内容を初期化し、この初期設定を記録してください
- リポジトリ名とアプリ名に合わせてプロジェクトを変更してください
  - `README.md`、GitHub Actions、バッジ、クローン手順などの `basic-android-app` を新しいリポジトリ名に置き換えてください
  - `app/build.gradle.kts` の `namespace` と `applicationId` を新しいパッケージ名に変更してください
  - Kotlinソース、テストコード、`AndroidManifest.xml` のパッケージ宣言・参照を新しいパッケージ名に変更してください
  - `app/src/main/res/values/strings.xml` のアプリ名と表示文言を更新してください
  - 必要に応じてテーマ名、データベース名、ランチャーアイコン、バックアップ設定を新しいアプリに合わせて更新してください
- `docs` ディレクトリ内にテンプレート由来のMarkdownファイルがある場合は削除し、READMEの参照も更新してください
- 初期設定完了後、このテンプレートファイル `initial-setup.md` 自体を削除してください

# 備考
- 解説は基本的に日本語で書いてください
- PRの説明に次のコメントを埋め込んでください。`<!-- 日本語でレビューして下さい -->`
- 改修対象ファイルや新規追加するファイルの末尾には必ず改行を入れてください
- `assembleDebug`、`testDebugUnitTest`、`lintDebug`、`ktlintCheck`、`detekt` を実行して、変更後のプロジェクトが正常にビルド・検証できることを確認してください
