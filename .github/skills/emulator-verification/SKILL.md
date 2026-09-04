---
name: android-emulator-verification
description: このリポジトリの Android アプリをエミュレーターで起動し、基本的な UI/CRUD 動作を確認する手順
---

# Android エミュレーター動作確認

## 対象アプリ

- パッケージ名: `com.example.myapplication`
- 起動 Activity: `.MainActivity`
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- 最小 SDK: 24、Target SDK: 36
- 初期表示: Room の初期データ `world` を使った `Hello world!`

## 前提確認

1. Java 17 が利用できることを確認する。
2. SDK の場所を確認する。Windows では `local.properties` の `sdk.dir` を優先し、通常は次の場所にある。
   `C:\Users\<ユーザー名>\AppData\Local\Android\Sdk`
3. `platform-tools\adb.exe` と `emulator\emulator.exe` が存在することを確認する。
4. 仮想化アクセラレーションを確認する。
   `emulator.exe -accel-check` で WHPX または同等のアクセラレーションが利用可能であることを確認する。

## エミュレーター起動

Windows PowerShell の例:

```powershell
$sdk = "C:\Users\<ユーザー名>\AppData\Local\Android\Sdk"
$adb = "$sdk\platform-tools\adb.exe"
$emulator = "$sdk\emulator\emulator.exe"

& $emulator -list-avds
Start-Process $emulator -ArgumentList "-avd", "Medium_Phone_API_36.0", "-no-boot-anim"
& $adb wait-for-device
```

起動完了を確認する。`adb devices` が `device` になり、`sys.boot_completed` が `1` になるまで待つ。初回起動は数分かかる場合がある。

```powershell
& $adb devices
& $adb shell getprop sys.boot_completed
```

エミュレーターが重い場合は、AVD の Graphics を `Hardware` または `Automatic` にし、RAM を 2～4 GB に設定する。`Pixel Launcher isn't responding` が起動中だけ表示される場合は、まず 1～2 分待つ。継続する場合はエミュレーターを再起動し、必要に応じて Cold Boot を試す。`Wipe Data` はエミュレーター内のデータを消去するため、最後の手段にする。

## ビルドとインストール

リポジトリルートで実行する。初回ビルドは時間がかかるため、十分なタイムアウトを設定し、実行中にキャンセルしない。

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

`installDebug` が成功しない場合は、次を確認する。

- `adb devices` に対象エミュレーターが 1 台以上あり、状態が `device` である。
- SDK Platform 36 と必要な Build Tools がインストールされている。
- 別の adb が起動している場合は、同じ SDK の `adb.exe` を使う。
- Gradle の依存関係取得にネットワーク接続が必要である。

## アプリ起動

```powershell
& $adb shell am force-stop com.example.myapplication
& $adb shell monkey -p com.example.myapplication 1
```

前面 Activity を確認する。

```powershell
& $adb shell dumpsys activity activities | Select-String "mResumedActivity|topResumedActivity"
```

`com.example.myapplication/.MainActivity` が表示されれば起動確認完了。

## 手動確認シナリオ

次の順に確認する。

1. 起動直後に `Hello world!` と文字列一覧が表示される。
2. 「文字列を入力」に `Android` を入力し、「追加」を押す。一覧が 1 件増え、追加メッセージが表示される。
3. 追加した項目の「編集」を押し、値を変更して「保存」を押す。一覧と挨拶が更新される。
4. 項目の「削除」を押す。対象が一覧から消え、挨拶が残りの先頭文字列に更新される。
5. 複数件ある状態で「すべて削除」を押す。一覧が空になり、挨拶が `Hello world!` に戻る。
6. アプリを再起動する。Room のデータが保持され、削除したデータが勝手に復元されないことを確認する。
7. 画面回転またはエミュレーター再作成後も、アプリがクラッシュせず UI が表示されることを確認する。

## 変更後の標準確認

UI やデータ層を変更した場合は、次を実行する。

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat installDebug
```

インストルメンテーションテストを変更した場合は、起動済みエミュレーターに対して次も実行する。

```powershell
.\gradlew.bat connectedAndroidTest
```

## 障害切り分け

- アプリが起動しない: `adb logcat` で `FATAL EXCEPTION` を確認し、前面 Activity とインストール結果を確認する。
- `device offline`: エミュレーターが完全起動するまで待ち、`adb kill-server` と `adb start-server` の後に再接続する。
- Pixel Launcher が応答しない: アプリ本体のクラッシュとは限らない。起動完了を待ち、GPU 設定、WHPX、PC の空きメモリを確認する。
- 画面が極端に重い: 不要なアプリを閉じ、AVD の解像度/RAM を下げ、Hardware/Automatic Graphics を使用する。PC の物理メモリが 16 GB 程度でも、他のアプリを多数起動していると遅くなる。
- UI の文言が期待と違う: 実装の初期値は `world`。古いドキュメントにある `Android` と混同しない。
