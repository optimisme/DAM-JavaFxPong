#!/bin/bash

reset

# Remove any existing Project.jar file
rm -rf ./release

# Remove any existing .class files from the bin directory
rm -rf ./bin

# Create the bin directory if it doesn't exist
mkdir -p ./bin

# Copy the assets directory to the bin directory
cp -r ./assets ./bin

# Generate the CLASSPATH by iterating over JAR files in the lib directory and its subdirectories
lib_dir="lib"
jar_files=()

# Find all JAR files in the lib directory and its subdirectories
while IFS= read -r -d '' jar_file; do
  if [[ "$jar_file" != *"javafx"* ]]; then
    jar_files+=("$jar_file")
  fi
done < <(find "$lib_dir" -name "*.jar" -type f -print0)

# Join the JAR files into the class_path
class_path=$(IFS=:; echo "${jar_files[*]}")

# Remove the leading ':' from the class_path
export CLASSPATH=${class_path#:}

if [[ $OSTYPE == 'linux-gnu' ]]; then
    export MODULEPATH=./lib/javafx-linux/lib
    export ICON=""
fi

if [[ $OSTYPE == 'darwin'* ]] && [[ $(arch) == 'i386' ]]; then
    export MODULEPATH=./lib/javafx-osx-intel/lib
    export ICON=-Xdock:icon=./assets/icon.png
fi

if [[ $OSTYPE == 'darwin'* ]] && [[ $(arch) == 'arm64' ]]; then
    export MODULEPATH=./lib/javafx-osx-arm/lib
    export ICON=-Xdock:icon=./assets/icon.png
fi

# Compile the Java source files and place the .class files in the bin directory
javac -d ./bin/ ./src/*.java --module-path $MODULEPATH --add-modules javafx.controls,javafx.fxml

# Create the Project.jar file with the specified manifest file and the contents of the bin directory
jar cfm release/Project.jar ./src/Manifest.txt -C bin . 
cp -r ./lib ./release

# Remove any .class files from the bin directory
rm -rf ./bin

# Run the Project.jar file
cd release
java $ICON --module-path $MODULEPATH --add-modules javafx.controls,javafx.fxml -cp Project.jar:$CLASSPATH Main
cd ..
