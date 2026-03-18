@echo off
cd /d %~dp0

REM ---- Use your JDK 21 ----
set "JAVAC=C:\Program Files\Java\jdk-21\bin\javac.exe"
set "JAVA=C:\Program Files\Java\jdk-21\bin\java.exe"

if not exist "%JAVAC%" (
    echo JDK not found. Please check the path in play.bat
    pause
    exit /b
)

REM ---- Compile latest code ----
"%JAVAC%" -d build\classes -sourcepath src\main\java src\main\java\wv\monstermaze\main\Game.java

REM ---- Run the game ----
"%JAVA%" -cp build\classes wv.monstermaze.main.Game

pause