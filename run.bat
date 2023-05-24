@echo off

REM Remove any existing Project.jar file
if exist .\release rmdir /s /q .\release

REM Remove any existing .class files from the bin directory
if exist .\bin rmdir /s /q .\bin

REM Create the bin directory if it doesn't exist
if not exist .\bin mkdir .\bin

REM Copy the assets directory to the bin directory
xcopy /E /I .\assets .\bin\assets

REM Generate the CLASSPATH by iterating over JAR files in the lib directory and its subdirectories
SETLOCAL ENABLEDELAYEDEXPANSION
SET lib_dir=lib
SET class_path=;
FOR /R %lib_dir% %%G IN (*.jar) DO (
   if not "%%~nxG"=="javafx.jar" (
       SET class_path=!class_path!;%%G
   )
)
SET CLASSPATH=%class_path:~1%

SET MODULEPATH=.\lib\javafx-windows\lib
SET ICON=

REM Compile the Java source files and place the .class files in the bin directory
javac -d .\bin\ .\src\*.java --module-path %MODULEPATH% --add-modules javafx.controls,javafx.fxml

REM Create the Project.jar file with the specified manifest file and the contents of the bin directory
jar cfm .\release\Project.jar .\Manifest.txt -C .\bin\ .

REM Copy the lib directory to the release directory
xcopy /E /I .\lib .\release\lib

REM Remove any .class files from the bin directory
if exist .\bin rmdir /s /q .\bin

REM Run the Project.jar file
cd .\release
java %ICON% --module-path %MODULEPATH% --add-modules javafx.controls,javafx.fxml -cp Project.jar;%CLASSPATH% Main
cd ..
