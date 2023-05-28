# PowerShell script to build the project

Clear-Host

$folderDevelopment = "Project"
$folderRelease = "Release"

# Get into the development directory
Set-Location $folderDevelopment

# Check if is JavaFX
$isJavaFX = $false
if (Test-Path -Path "lib/javafx*" -ErrorAction SilentlyContinue) {
    $isJavaFX = $true
}

# Check if is Hibernate
$isHibernate=$false
$HIBERNATEX=""
$HIBERNATEWIN=""
if (Test-Path ".\hibernate.properties") {
    $isHibernate=$true
    $HIBERNATEX="--add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED"
    $HIBERNATEWIN="--add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --enable-preview -XX:+ShowCodeDetailsInExceptionMessages"
}

# Remove any existing .class files from the bin directory
if (Test-Path -Path "./bin") {
    Remove-Item -Recurse -Force -Path "./bin"
}

# Create the bin directory if it doesn't exist
New-Item -ItemType Directory -Force -Path ./bin | Out-Null

# Copy the assets directory to the bin directory
if (Test-Path -Path "./assets") {
    Copy-Item -Recurse -Force ./assets ./bin/assets
}

# Copy the icons if they exist
if (Test-Path -Path "./icons") {
    Copy-Item -Recurse -Force ./icons ./bin/icons
}

# Generate the CLASSPATH by iterating over JAR files in the lib directory and its subdirectories
$lib_dir = (Resolve-Path "lib").Path
$jar_files = @()
if (-not $isJavaFX) {
    $jar_files = Get-ChildItem -Path $lib_dir -Filter "*.jar" -Recurse | ForEach-Object { ".\lib\" + $_.FullName.Replace($lib_dir + '\', '') }
} else {
    $jar_files = Get-ChildItem -Path $lib_dir -Filter "*.jar" -Recurse | ForEach-Object {
        if (-not $_.Name.Contains("javafx")) {
            ".\lib\" + $_.FullName.Replace($lib_dir + '\', '')
        }
    }
}

# Enclose paths with quotes if they contain spaces
$CLASSPATH = ($jar_files | ForEach-Object { 
    if($_ -match '\s') {
        "`"" + $_ + "`""
    } else {
        $_
    } 
}) -join ';'

# Generate the CLASSPATH for UNIX
$jar_files = @()
if (-not $isJavaFX) {
    $jar_files = Get-ChildItem -Path $lib_dir -Filter "*.jar" -Recurse | ForEach-Object { "lib/" + $_.FullName.Replace($lib_dir + '\', '') }
} else {
    $jar_files = Get-ChildItem -Path $lib_dir -Filter "*.jar" -Recurse | ForEach-Object {
        if (-not $_.Name.Contains("javafx")) {
            "lib/" + $_.FullName.Replace($lib_dir + '\', '')
        }
    }
}

# Enclose paths with quotes if they contain spaces
$CLASSPATHX = ($jar_files | ForEach-Object { 
    if($_ -match '\s') {
        "`"" + $_ + "`""
    } else {
        $_
    } 
}) -join ':'

# Compile the Java source files and place the .class files in the bin directory
if (-not $isJavaFX) {
    javac -d ./bin/ ./src/*.java -cp $CLASSPATH
} else {
    javac -d ./bin/ ./src/*.java -cp $CLASSPATH --module-path ./lib/javafx-windows/lib --add-modules javafx.controls,javafx.fxml
}

# Create the Project.jar file with the specified manifest file and the contents of the bin directory
if (Get-Command jar -ErrorAction SilentlyContinue) {
    # jar command is available, use it
    jar cfm ./Project.jar ./Manifest.txt -C bin .
} else {
    # jar command is not available, try to find it
    $jarExePath = Get-ChildItem -Path C:\ -Recurse -Filter "jar.exe" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
    if ($jarExePath) {
        & $jarExePath cfm ./Project.jar ./Manifest.txt -C bin .
    } else {
        Write-Host "Jar command not found."
    }
}

# Remove any .class files from the bin directory
Remove-Item -Recurse -Force ./bin

# Get out of the development directory
Set-Location ..

# Move the Project.jar file to the release directory
if (Test-Path -Path "./$folderRelease") {
    Remove-Item -Recurse -Force ./$folderRelease
}
New-Item -ItemType Directory -Force -Path ./$folderRelease | Out-Null
Move-Item ./$folderDevelopment/Project.jar ./$folderRelease/Project.jar
Copy-Item -Recurse -Force "./$folderDevelopment/lib" "./$folderRelease/lib"

if ((Test-Path -Path ".\$folderDevelopment\icons") -and $isJavaFX) {
    Copy-Item -Path ".\$folderDevelopment\icons" -Destination ".\$folderRelease\icons" -Recurse
}

# Copy .properties if they exist
if (Test-Path -Path ".\$folderDevelopment\*.properties" -PathType Leaf) {
    Copy-Item -Path ".\$folderDevelopment\*.properties" -Destination ".\$folderRelease\" -Force
}

# Copy .xml if they exist (for hibernate)
if (Test-Path -Path ".\$folderDevelopment\*.xml" -PathType Leaf) {
    Copy-Item -Path ".\$folderDevelopment\*.xml" -Destination ".\$folderRelease\" -Force
}

# Create the 'run.sh' and 'run.ps1' files
if (-not $isJavaFX) {
@"
#!/bin/bash
java $HIBERNATEX -cp "Project.jar;$CLASSPATHX" Main
"@ | Out-File -FilePath ".\$folderRelease\run.sh" -Encoding UTF8
@"
java $HIBERNATEWIN -cp "Project.jar;$CLASSPATH" Main
"@ | Out-File -FilePath ".\$folderRelease\run.ps1" -Encoding UTF8
} else {
@"
#!/bin/bash
MODULEPATH=""
ICON=""
if ls lib/javafx* 1> /dev/null 2>&1; then
    isJavaFX=true
    if [[ `$OSTYPE == 'linux-gnu' ]]; then
        MODULEPATH=./lib/javafx-linux/lib
    fi
    if [[ `$OSTYPE == 'darwin'* ]] && [[ `$(arch) == 'i386' ]]; then
        MODULEPATH=./lib/javafx-osx-intel/lib
        ICON=-Xdock:icon=icons/iconOSX.png
    fi
    if [[ `$OSTYPE == 'darwin'* ]] && [[ `$(arch) == 'arm64' ]]; then
        MODULEPATH=./lib/javafx-osx-arm/lib
        ICON=-Xdock:icon=icons/iconOSX.png
    fi
fi
java $HIBERNATEX `$ICON --module-path `$MODULEPATH --add-modules javafx.controls,javafx.fxml -cp "Project.jar:$CLASSPATHX" Main
"@ | Out-File -FilePath ".\$folderRelease\run.sh" -Encoding UTF8
@"
java $HIBERNATEWIN --module-path "./lib/javafx-windows/lib" --add-modules javafx.controls,javafx.fxml -cp "Project.jar;$CLASSPATH" Main
"@ | Out-File -FilePath ".\$folderRelease\run.ps1" -Encoding UTF8
}

# Run the Project.jar file
Set-Location ./$folderRelease
./run.ps1
Set-Location ..