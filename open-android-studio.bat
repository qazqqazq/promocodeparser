@echo off
REM Launches Android Studio with an ASCII Gradle home, so jlink/AAPT2 do not
REM choke on the Cyrillic user path. Use THIS to open the project from now on.
set "GRADLE_USER_HOME=C:\Users\Public\gradle-home"
start "" "C:\Program Files\Android\Android Studio\bin\studio64.exe" "C:\Users\Public\promohub\android"
