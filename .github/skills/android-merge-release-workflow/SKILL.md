---
name: android-merge-release-workflow
description: Android アプリの PR をバージョン、CI、レビュー状態まで確認してマージし、必要に応じてタグ・リリースを作成してローカル main を同期する手順
---

# Android PR マージ・リリースワークフロー

## 目的

この Skill は、改修用ブランチの PR を安全に確認して `main` にマージし、アプリのバージョンが更新されている場合だけ GitHub Release とタグを作成し、最後にローカル `main` を最新状態へ同期するために使用する。

この Skill はマージ操作を依頼されたときに使用する。ユーザーから明示的な依頼がない限り、PR のマージ、push、タグ作成、リリース作成は行わない。

## 前提と安全確認

1. リポジトリのルートで作業する。
2. `git status --short`、`git branch --show-current`、`git log -3 --oneline` で状態を確認する。
3. ユーザーの未コミット変更を破棄しない。変更がある場合は、今回のマージ処理と競合しないことを確認する。
4. PR の番号、base ブランチ、head ブランチ、リポジトリを特定する。
5. GitHub の PR が `OPEN` であり、base が対象リポジトリの `main` であることを確認する。
6. head ブランチの意図しない変更、機密情報、生成物、巨大ファイルが差分に含まれていないことを確認する。

GitHub CLI の例:

```powershell
gh pr view <PR番号> --json number,state,baseRefName,headRefName,title,url
gh pr diff <PR番号> --stat
gh pr diff <PR番号>
```

## バージョンの確認

アプリのバージョンの正本は `app/build.gradle.kts` の `versionName` と `versionCode` である。PR の base と head の両方を比較する。

```powershell
git show origin/main:app/build.gradle.kts | Select-String "versionCode|versionName"
git show <head-ref>:app/build.gradle.kts | Select-String "versionCode|versionName"
```

確認する内容:

- `versionName` が `MAJOR.MINOR.PATCH` の SemVer 形式である。
- 機能追加は MINOR、後方互換性のある修正は PATCH、互換性を壊す変更は MAJOR になっている。
- バージョン変更が必要な改修なのに `versionName` が更新されていない、または変更規模に対して過大・過小でない。
- `versionCode` が base より大きい整数で、再利用されていない。
- `versionName` と `versionCode` の変更が、README の Version バッジ、バージョン履歴、関連ドキュメントと整合している。
- ドキュメントだけの変更では、アプリのバージョンを不要に上げていない。

判断に迷う変更規模やリリース番号は、勝手に決めずユーザーに確認する。CI が通っていても、不適切なバージョニングはマージしない。

## CI・レビュー・競合の確認

```powershell
gh pr checks <PR番号>
gh pr view <PR番号> --json reviewDecision,mergeStateStatus,statusCheckRollup
```

次のすべてを満たす場合だけマージする。

- 必須 CI がすべて成功している。
- 必須レビューが承認済みである（リポジトリのルールで必要な場合）。
- PR が `MERGEABLE` または同等の競合なし状態である。
- 失敗、保留、キャンセルされた必須チェックがない。
- 差分とバージョン更新の内容がレビュー可能である。

チェックが `pending` の間は待機する。失敗した場合は原因を確認し、修正が必要ならマージを止めて改修用ブランチへ戻る。CI を無視するための強制マージは行わない。

## PR のマージ

マージ方法は **Create a merge commit** を使用する。PR の変更を単一のマージコミットとして `main` に取り込み、元の作業ブランチは不要になったら削除する。

```powershell
gh pr merge <PR番号> --merge --delete-branch
```

マージ後、PR が `MERGED` になったことと、マージコミットを確認する。

```powershell
gh pr view <PR番号> --json state,mergedAt,mergeCommit,url
```

## バージョン更新時のタグとリリース

PR の head に含まれる `versionName` が base から更新されている場合だけ、マージ後の `main` のコミットを対象にリリースを作成する。バージョン更新がない場合は、タグもリリースページも作成しない。

1. タグ名は `versionName` に `v` を付けた形式（例: `versionName = "0.8.0"` → `v0.8.0`）にする。
2. 同じタグまたは同じバージョンの Release が既に存在しないことを確認する。
3. リリースノートには変更内容、互換性に関わる注意、検証内容を簡潔に記載する。
4. リリースはマージ後の `main` の先頭コミットを対象にする。

```powershell
$version = "<versionName>"
$tag = "v$version"
gh release view $tag
gh release create $tag --target main --title $tag --generate-notes
```

`gh release view` が「存在しない」ことを確認する前に、既存タグを上書きしない。SemVer が pre-release（例: `1.0.0-rc.1`）の場合は、リポジトリの運用に合わせて `--prerelease` を付ける。

## ローカル main の同期

マージと必要なリリース作成が完了したら、ローカルの作業ツリーを確認して `main` に移動し、リモートの最新を fast-forward で取り込む。

```powershell
git status --short
git switch main
git pull --ff-only origin main
git status --short
git log -3 --oneline
```

未コミット変更がある場合は、勝手に stash、reset、削除をせず、同期を止めて扱いを確認する。`--ff-only` が失敗した場合も、履歴を書き換えず状況を報告する。

## 完了条件

- 対象 PR が期待する base にマージされている。
- 必須 CI、レビュー、競合状態をマージ前に確認している。
- SemVer と `versionCode` が改修内容に対して妥当である。
- `versionName` が更新された場合、対応する `v<versionName>` のタグと GitHub Release がマージ後の `main` に作成されている。
- バージョン更新がない場合、不要なタグと Release を作成していない。
- ローカルブランチが `main` で、`origin/main` の最新コミットを取り込んでいる。
- 作業ツリーが意図せず汚れていない。
