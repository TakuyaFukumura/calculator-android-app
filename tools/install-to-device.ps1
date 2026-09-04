$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$localProperties = Join-Path $repoRoot "local.properties"
$sdkDir = $null

if (Test-Path $localProperties) {
    $sdkLine = Get-Content $localProperties |
        Where-Object { $_ -match "^sdk\.dir=" } |
        Select-Object -First 1
    if ($sdkLine) {
        $sdkDir = ($sdkLine -replace "^sdk\.dir=", "").Replace("\:", ":").Replace("\\", "\")
    }
}

if (-not $sdkDir) {
    $sdkDir = Join-Path $env:LOCALAPPDATA "Android\Sdk"
}

$adb = Join-Path $sdkDir "platform-tools\adb.exe"
$gradle = Join-Path $repoRoot "gradlew.bat"

if (-not (Test-Path $adb)) {
    throw "adb.exe が見つかりません: $adb"
}
if (-not (Test-Path $gradle)) {
    throw "gradlew.bat が見つかりません: $gradle"
}

Push-Location $repoRoot
try {
    & $adb start-server | Out-Host
    $devices = @(& $adb devices | Select-String "`tdevice$")
    if ($devices.Count -eq 0) {
        throw "ADBデバイスが見つかりません。USB接続とUSBデバッグ許可を確認してください。"
    }
    if ($devices.Count -gt 1) {
        throw "複数のADBデバイスが接続されています。1台だけ接続してください。"
    }

    & $gradle installDebug
    if ($LASTEXITCODE -ne 0) {
        throw "installDebug に失敗しました。"
    }

    & $adb shell am force-stop com.example.myapplication
    & $adb shell monkey -p com.example.myapplication 1
    if ($LASTEXITCODE -ne 0) {
        throw "アプリの起動に失敗しました。"
    }

    Write-Host "インストールと起動が完了しました。" -ForegroundColor Green
}
finally {
    Pop-Location
}
