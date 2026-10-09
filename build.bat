@echo off

set BASE=%~dp0
set SRC=%BASE%\src
set TEST=%BASE%\test
set OUT=%BASE%\out
set TEST_OUT=%BASE%\test-out
set LIB=%BASE%\lib
set JUNIT_JAR=%LIB%\junit-platform-console-standalone.jar

echo === Cleaning output directories ===
if exist "%OUT%" rmdir /s /q "%OUT%"
if exist "%TEST_OUT%" rmdir /s /q "%TEST_OUT%"
mkdir "%OUT%"
mkdir "%TEST_OUT%"

echo === Compiling main sources ===
for /r "%SRC%" %%f in (*.java) do (
    echo %%f >> src_files.txt
)
javac -cp "%OUT%" -d "%OUT%" @src_files.txt
set JAVAC_ERR=%ERRORLEVEL%
if exist src_files.txt del src_files.txt
if %JAVAC_ERR% neq 0 (
    echo ERROR: Main compilation failed!
    exit /b 1
)
echo Main sources compiled successfully.

echo === Compiling test sources ===
for /r "%TEST%" %%f in (*.java) do (
    echo %%f >> test_files.txt
)
javac -cp "%OUT%;%JUNIT_JAR%" -d "%TEST_OUT%" @test_files.txt
set JAVAC_TEST_ERR=%ERRORLEVEL%
if exist test_files.txt del test_files.txt
if %JAVAC_TEST_ERR% neq 0 (
    echo ERROR: Test compilation failed!
    exit /b 1
)
echo Test sources compiled successfully.

echo === Running Tests ===
java -jar "%JUNIT_JAR%" execute ^
    --classpath "%OUT%;%TEST_OUT%" ^
    --scan-classpath ^
    --reports-dir "%BASE%\test-results"

echo === Build complete ===
