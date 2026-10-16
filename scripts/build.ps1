param([switch]$ConnectedTests)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot
if (Test-Path 'A:\Android\jbr\bin\java.exe') {
    $env:JAVA_HOME = 'A:\Android\jbr'
}
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to JDK 25 (Android Studio jbr).' }
$env:GRADLE_USER_HOME = Join-Path $projectRoot '.gradle-home'
$env:ANDROID_USER_HOME = Join-Path $projectRoot '.android'
$env:TEMP = Join-Path $projectRoot '.build-temp'
$env:TMP = $env:TEMP
New-Item -ItemType Directory -Force $env:TEMP,$env:GRADLE_USER_HOME,$env:ANDROID_USER_HOME | Out-Null
$jvmTemp = $env:TEMP.Replace('\','/')
$jvmProfile = (Join-Path $projectRoot '.build-home').Replace('\','/')
$env:JAVA_TOOL_OPTIONS = "-Djava.io.tmpdir=$jvmTemp -Duser.home=$jvmProfile"
$tasks = @(':app:assembleDebug', ':app:testDebugUnitTest', ':app:assembleDebugAndroidTest', ':app:lintDebug')
if ($ConnectedTests) { $tasks += ':app:connectedDebugAndroidTest' }
& .\gradlew.bat @tasks --console=plain
if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
Write-Host 'APK: app/build/outputs/apk/debug/app-debug.apk'

