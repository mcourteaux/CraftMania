@echo off
setlocal

:: Converted using ChatGPT (untested)

:: Build classpath (use semicolons on Windows)
set CP=lib\lwjgl-2.9.3\jar\lwjgl.jar;lib\lwjgl-2.9.3\jar\lwjgl_util.jar;lib\slick-util.jar;src

echo Compiling...
javac -classpath "%CP%" src\org\craftmania\**\*.java

echo Running...
java -classpath "%CP%" -Djava.library.path=lib\lwjgl-2.9.3\native\windows org.craftmania.CraftMania


