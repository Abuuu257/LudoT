@echo off
set BASE=%~dp0
set OUT=%BASE%\out
java -cp "%OUT%" com.apiit.ludot.main.Main %*
