---
name: android-modification-workflow
description: このリポジトリの Android アプリを改修し、作業ブランチ・Semantic Versioning・適切なコミット粒度で安全に完了する手順
---

# Android アプリ改修ワークフロー

## 目的

この Skill は、機能追加、バグ修正、UI変更、依存関係更新などを、`main` を汚さずに実施し、検証可能なコミットとして残すために使用する。

## 作業開始

1. リポジトリのルートで作業する。
2. `git status --short` と `git branch --show-current` で未コミット変更と現在のブランチを確認する。
3. ユーザーの変更を勝手に破棄しない。既存の未コミット変更が今回の作業と競合する場合は、作業を止めて確認する。
4. 最新の `main` を基準にする。ローカル `main` が古い場合は、ユーザーの指示に従って更新してから作業する。
5. `main` から目的が分かる kebab-case の作業ブランチを作成して移動する。

```powershell
git switch main
git pull --ff-only origin main
git switch -c <change-type>-<short-description>
```

ブランチ例:

- `feature/custom-greeting`
- `fix/empty-string-validation`
- `refactor/repository-layer`
- `chore/update-compose`

すでにユーザーが作業中のブランチにいる場合は、勝手に新しいブランチを作らず、そのブランチを使用する。

## 改修前の把握

変更範囲に応じて次のファイルを確認する。

- `app/src/main/java/com/example/myapplication/MainActivity.kt`: Compose UI と画面イベント
- `app/src/main/java/com/example/myapplication/ui/viewmodel/MainViewModel.kt`: UI状態と非同期処理
- `app/src/main/java/com/example/myapplication/data/`: Room の Entity、DAO、Database、Repository
- `app/src/main/java/com/example/myapplication/di/`: Hilt の依存関係設定
- `app/build.gradle.kts`: `applicationId`、`versionCode`、`versionName`、依存関係
- `gradle/libs.versions.toml`: プラグインとライブラリのバージョン
- `README.md` と `docs/`: ユーザー向け説明と実装仕様

既存のパターン、状態管理、エラーハンドリング、テスト構成を再利用し、無関係なファイルは変更しない。

## Semantic Versioning

アプリの公開バージョンは `MAJOR.MINOR.PATCH` 形式で管理する。現在の正本は `app/build.gradle.kts` の `versionName` である。

| 変更内容 | 更新 |
| --- | --- |
| 既存機能と互換性のあるバグ修正、表示修正、内部改善 | PATCH: `0.7.0` → `0.7.1` |
| 後方互換性を保った新機能、画面・操作の追加 | MINOR: `0.7.0` → `0.8.0` |
| 既存利用者の移行が必要な仕様変更、互換性を壊す変更 | MAJOR: `0.7.0` → `1.0.0` |

`0.x` では、まだ安定版でないため互換性を壊す変更を MINOR として扱うこともできる。ただし、既存利用者への影響が大きい変更は `1.0.0` 到達前でも明示的に記録する。

バージョンを変更するときは、次を同じ変更として更新する。

1. `app/build.gradle.kts` の `versionName`
2. `app/build.gradle.kts` の `versionCode`
3. `README.md` の Version バッジ
4. `README.md` のバージョン履歴（履歴がある場合）
5. 変更に関係する `docs/` のバージョン記載

`versionCode` は Android の単調増加する整数であり、SemVer の値そのものではない。既存値より必ず 1 以上大きくし、同じ `versionName` で再利用しない。バージョンを上げない純粋なドキュメント変更では、アプリの `versionName` と `versionCode` を変更しない。

## 実装と検証

1. 変更を小さく分け、各段階でコンパイル可能な状態を保つ。
2. UI変更では、必要に応じて `android-emulator-verification` Skill を使い、エミュレーターで起動と操作を確認する。
3. データ層変更では、永続化、初期データ、空データ、エラー時の表示を確認する。
4. 改修後は変更範囲に応じて次を実行する。長時間実行になるため、十分なタイムアウトを設定し、実行中にキャンセルしない。

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

インストルメンテーションテストや実機相当の動作を変更した場合は、起動済みエミュレーターで次も実行する。

```powershell
.\gradlew.bat connectedAndroidTest
```

失敗した場合は、変更が原因のエラーを修正してからコミットする。無関係な既存問題は、内容を記録して隠さない。

## コミット方針

コミットはレビューしやすい意味のある単位に分ける。無関係な変更を同じコミットに混ぜない。

- 実装とテストを同時に変更する場合は、動作単位で 1 コミットにする。
- バージョン更新と README/履歴更新は、改修のリリース単位として実装コミットに含める。
- 大きな改修では、データ層、ViewModel、UI、テストなど、独立してレビュー可能な単位に分ける。
- 自動整形や大量の無関係な差分をコミットしない。
- コミット前に `git diff --check`、`git diff --stat`、`git status --short` を確認する。

コミットメッセージは変更の目的を短く表す。例:

```powershell
git add app/src/main/... app/src/test/... app/build.gradle.kts README.md
git commit -m "Add custom greeting support" -m "Co-authored-by: Copilot App <223556219+Copilot@users.noreply.github.com>"
```

ユーザーから明示的に依頼されない限り、コミットを push、PR 作成、マージまで自動で進めない。

## 完了条件

- `main` ではなく作業ブランチにいる。
- 実装、テスト、ドキュメント、バージョン更新が変更内容に対して整合している。
- SemVer の更新理由が明確で、`versionCode` も増加している。
- 必要なビルド・テスト・lint・エミュレーター確認が成功している。
- `git status` と差分を確認し、コミットが適切な粒度で作成されている。
