#!/bin/bash

reset

folderDevelopment="Project"
folderRelease="Release"

# Get into the development directory
cd $folderDevelopment

# Check if is JavaFX
isJavaFX=false
MODULEPATH=""
if ls lib/javafx* 1> /dev/null 2>&1; then
    isJavaFX=true
    if [[ $OSTYPE == 'linux-gnu' ]]; then
        MODULEPATH=./lib/javafx-linux/lib
    fi
    if [[ $OSTYPE == 'darwin'* ]] && [[ $(arch) == 'i386' ]]; then
        MODULEPATH=./lib/javafx-osx-intel/lib
    fi
    if [[ $OSTYPE == 'darwin'* ]] && [[ $(arch) == 'arm64' ]]; then
        MODULEPATH=./lib/javafx-osx-arm/lib
    fi
fi

# Check if is Hibernate
isHibernate=false
HIBERNATEX=""
HIBERNATEWIN=""
if [ -n "$(find . -maxdepth 1 -type f -name 'hibernate.properties' -print -quit)" ]; then
    isHibernate=true
    HIBERNATEX="--add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED"
    HIBERNATEWIN="--add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --enable-preview -XX:+ShowCodeDetailsInExceptionMessages"
fi

# Remove any existing .class files from the bin directory
rm -rf ./bin

# Create the bin directory if it doesn't exist
mkdir -p ./bin

# Copy the assets directory to the bin directory
if [ -d ./assets ]; then
    cp -r ./assets ./bin
fi
if [ -d ./icons ]; then
    cp -r ./icons ./bin
fi

# Generate the CLASSPATH by iterating over JAR files in the lib directory and its subdirectories
lib_dir="lib"
jar_files=()

# Find all JAR files in the lib directory and its subdirectories
if [ "$isJavaFX" != true ]; then
    while IFS= read -r -d '' jar_file; do
    jar_files+=("$jar_file")
    done < <(find "$lib_dir" -name "*.jar" -type f -print0)
else
    while IFS= read -r -d '' jar_file; do
    if [[ "$jar_file" != *"javafx"* ]]; then
        jar_files+=("$jar_file")
    fi
    done < <(find "$lib_dir" -name "*.jar" -type f -print0)
fi

# Join the JAR files into the class_path
class_path=$(IFS=:; echo "${jar_files[*]}")

# Remove the leading ':' from the class_path
CLASSPATH=${class_path#:}

# Unir els fitxers JAR en el class_path
class_path_win=$(IFS=";"; printf "%s" "${jar_files[*]}")
class_path_win=$(echo "$class_path_win" | sed 's|lib/|.\\lib\\|g')

# Eliminar el ':' inicial del class_path
CLASSPATHWIN=${class_path_win#:}

# Compile the Java source files and place the .class files in the bin directory
if [ "$isJavaFX" != true ]; then
    javac -d ./bin/ ./src/*.java -cp $CLASSPATH
else
    javac -d ./bin/ ./src/*.java --module-path $MODULEPATH --add-modules javafx.controls,javafx.fxml
fi

# Create the Project.jar file with the specified manifest file and the contents of the bin directory
jar cfm ./Project.jar ./Manifest.txt -C bin .

# Remove any .class files from the bin directory
rm -rf ./bin

# Get out of the development directory
cd ..

# Move the Project.jar file to the release directory
rm -rf ./$folderRelease
mkdir -p ./$folderRelease
mv ./$folderDevelopment/Project.jar ./$folderRelease/Project.jar
cp -r ./$folderDevelopment/lib ./$folderRelease/lib

# Copy icons if they exist
if [ -d ./$folderDevelopment/icons ] && [ "$isJavaFX" = true ]; then
    cp -r ./$folderDevelopment/icons ./$folderRelease/icons
fi

# Copy .properties if they exist
if [ -n "$(find ./$folderDevelopment -maxdepth 1 -type f -name '*.properties' -print -quit)" ]; then
    cp -r ./$folderDevelopment/*.properties ./$folderRelease/
fi

# Copy .xml if they exist (for Hibernate)
if [ -n "$(find ./$folderDevelopment -maxdepth 1 -type f -name '*.xml' -print -quit)" ]; then
    cp -r ./$folderDevelopment/*.xml ./$folderRelease/
fi

# Create the 'run.sh' and 'run.ps1' files
if [ "$isJavaFX" != true ]; then
cat > ./$folderRelease/run.sh << EOF
#!/bin/bash
java $HIBERNATEX -cp "Project.jar:$CLASSPATH" Main
EOF
cat > ./$folderRelease/run.ps1 << EOF
java $HIBERNATEWIN -cp "Project.jar;$CLASSPATHWIN" Main
EOF
else
cat > ./$folderRelease/run.sh << EOF
#!/bin/bash
MODULEPATH=""
ICON=""
if ls lib/javafx* 1> /dev/null 2>&1; then
    isJavaFX=true
    if [[ \$OSTYPE == 'linux-gnu' ]]; then
        MODULEPATH=./lib/javafx-linux/lib
    fi
    if [[ \$OSTYPE == 'darwin'* ]] && [[ \$(arch) == 'i386' ]]; then
        MODULEPATH=./lib/javafx-osx-intel/lib
        ICON=-Xdock:icon=icons/iconOSX.png
    fi
    if [[ \$OSTYPE == 'darwin'* ]] && [[ \$(arch) == 'arm64' ]]; then
        MODULEPATH=./lib/javafx-osx-arm/lib
        ICON=-Xdock:icon=icons/iconOSX.png
    fi
fi
java $HIBERNATEX \$ICON --module-path \$MODULEPATH --add-modules javafx.controls,javafx.fxml -cp "Project.jar:$CLASSPATH" Main
EOF
cat > ./$folderRelease/run.ps1 << EOF
java $HIBERNATEWIN --module-path "./lib/javafx-windows/lib" --add-modules javafx.controls,javafx.fxml -cp "Project.jar;$CLASSPATHWIN" Main
EOF
fi

# Fem l'arxiu executable
chmod +x ./$folderRelease/run.sh

# Run the Project.jar file
cd ./$folderRelease
./run.sh
cd ..