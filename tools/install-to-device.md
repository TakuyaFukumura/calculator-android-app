# `install-to-device.bat` の使い方

`install-to-device.bat` は、接続されたAndroid端末または起動中のエミュレーターへ、デバッグ版APKをビルドしてインストールし、アプリを起動するためのWindows用スクリプトです。

## 前提条件

- Windows環境であること
- Java 17が利用できること
- Android SDKのPlatform-Tools（`adb.exe`）がインストールされていること
- リポジトリの依存関係を取得できること
- Android端末の場合は、USBデバッグを有効にしていること
- 実行時にADBで認識される端末が1台だけであること

Android SDKの場所は、リポジトリ直下の `local.properties` にある `sdk.dir` を優先して使用します。`sdk.dir` がない場合は、既定の `%LOCALAPPDATA%\Android\Sdk` を使用します。

## 実行手順

1. Android端末をUSB接続するか、エミュレーターを起動します。
2. 端末に「USBデバッグを許可しますか？」と表示された場合は許可します。
3. PowerShellまたはコマンドプロンプトで、リポジトリのルートへ移動します。

   ```powershell
   cd C:\path\to\basic-android-app
   ```

4. スクリプトを実行します。

   ```powershell
   .\tools\install-to-device.bat
   ```

スクリプトは次の処理を自動で行います。

1. ADBサーバーを起動します。
2. 接続状態が `device` の端末を確認します。
3. `gradlew.bat installDebug` でデバッグ版APKをビルドしてインストールします。
4. `com.example.myapplication` を起動します。

処理が成功すると、`インストールと起動が完了しました。` と表示されます。終了するには、表示されたプロンプトで任意のキーを押します。

## トラブルシューティング

### `adb.exe が見つかりません`

Android SDKのPlatform-Toolsがインストールされていることを確認し、`local.properties` の `sdk.dir` が実際のSDKの場所を指しているか確認してください。

```properties
sdk.dir=C:\\Users\\ユーザー名\\AppData\\Local\\Android\\Sdk
```

### `ADBデバイスが見つかりません`

端末のUSB接続、USBデバッグの有効化、端末側の接続許可を確認してください。エミュレーターを使う場合は、完全に起動してから再実行してください。

### `複数のADBデバイスが接続されています`

使用しない端末やエミュレーターを切断・停止し、1台だけが接続された状態で再実行してください。

接続状況は次のコマンドで確認できます。

```powershell
adb devices
```

### `installDebug に失敗しました`

ネットワーク接続、Java 17、Android SDKの不足コンポーネントを確認してください。詳細なエラーは、スクリプトが実行するGradleの出力を確認してください。

### 日本語が文字化けして構文エラーになる

`install-to-device.ps1` はWindows PowerShellでも読み込めるUTF-8（BOM付き）で保存してください。ファイルを別のエディターで保存し直す場合も、文字コードを変更しないでください。
