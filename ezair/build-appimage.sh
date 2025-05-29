#!/bin/bash
# EzAir AppImage Builder
# Creates a portable AppImage for EzAir JavaFX application

set -e  # Exit on any error

echo "=== EzAir AppImage Builder ==="
echo "Building AppImage for EzAir JavaFX application..."

# Configuration
APP_NAME="EzAir"
APP_VERSION="1.0"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="$SCRIPT_DIR/target"
APPDIR="$TARGET_DIR/EzAir.AppDir"
APPIMAGETOOL="/tmp/appimagetool-x86_64.AppImage"

# Clean and create AppDir structure
echo "Creating AppImage directory structure..."
rm -rf "$APPDIR"
mkdir -p "$APPDIR"
mkdir -p "$APPDIR/usr/bin"
mkdir -p "$APPDIR/usr/lib"
mkdir -p "$APPDIR/usr/share/applications"
mkdir -p "$APPDIR/usr/share/pixmaps"

# Build the fat JAR if it doesn't exist
if [ ! -f "$TARGET_DIR/ezair-executable.jar" ]; then
    echo "Building application JAR..."
    mvn clean package -q
fi

# Copy the application JAR
echo "Copying application files..."
cp "$TARGET_DIR/ezair-executable.jar" "$APPDIR/usr/lib/"

# Create the main executable script
echo "Creating main executable..."
cat > "$APPDIR/usr/bin/ezair" << 'EOF'
#!/bin/bash
# EzAir AppImage Entry Point

# Find the directory containing this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Navigate to the lib directory relative to the script
APP_LIB_DIR="$SCRIPT_DIR/../lib"
JAR_FILE="$APP_LIB_DIR/ezair-executable.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: Application JAR not found at $JAR_FILE"
    echo "Script dir: $SCRIPT_DIR"
    echo "Looking for JAR at: $JAR_FILE"
    ls -la "$APP_LIB_DIR" 2>/dev/null || echo "Directory $APP_LIB_DIR not found"
    exit 1
fi

# Check Java installation
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher to run EzAir"
    echo ""
    echo "To install Java on Ubuntu/Debian:"
    echo "  sudo apt update && sudo apt install openjdk-11-jdk"
    echo ""
    echo "To install Java on CentOS/RHEL/Fedora:"
    echo "  sudo dnf install java-11-openjdk-devel"
    exit 1
fi

echo "Starting EzAir..."

# Try to find JavaFX modules in different locations
JAVAFX_MODULES=""
if [ -d "/usr/share/openjfx/lib" ]; then
    JAVAFX_MODULES="/usr/share/openjfx/lib"
elif [ -d "/usr/share/maven-repo/org/openjfx" ]; then
    # Look for JavaFX jars in system maven repo
    JAVAFX_PATH="/usr/share/maven-repo/org/openjfx"
    JAVAFX_MODULES="$JAVAFX_PATH/javafx-controls/11/javafx-controls-11.jar:$JAVAFX_PATH/javafx-fxml/11/javafx-fxml-11.jar:$JAVAFX_PATH/javafx-base/11/javafx-base-11.jar:$JAVAFX_PATH/javafx-graphics/11/javafx-graphics-11.jar"
fi

# Common JVM options
COMMON_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Europe/Bucharest"
COMMON_OPTS="$COMMON_OPTS --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
COMMON_OPTS="$COMMON_OPTS --add-opens java.base/java.lang=ALL-UNNAMED"
COMMON_OPTS="$COMMON_OPTS --add-opens java.desktop/sun.awt=ALL-UNNAMED"
COMMON_OPTS="$COMMON_OPTS -Djava.library.path=/usr/lib/x86_64-linux-gnu/jni"

# Launch with system JavaFX (hardware rendering first)
if [ -n "$JAVAFX_MODULES" ]; then
    exec java $COMMON_OPTS \
        -Djavafx.platform=linux \
        -Djava.awt.headless=false \
        --enable-native-access=javafx.graphics \
        --module-path "$JAVAFX_MODULES" \
        --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
        -cp "$JAR_FILE" \
        com.proiect.App "$@"
else
    echo "Error: JavaFX modules not found on system."
    echo "Please install OpenJFX: sudo apt install openjfx libopenjfx-jni"
    exit 1
fi
EOF
    fi
else
    # Running from extracted AppDir
    APP_LIB_DIR="$(dirname "$0")/../lib"
fi

JAR_FILE="$APP_LIB_DIR/ezair-executable.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: Application JAR not found at $JAR_FILE"
    exit 1
fi

# Check Java installation
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher to run EzAir"
    echo ""
    echo "To install Java on Ubuntu/Debian:"
    echo "  sudo apt update && sudo apt install openjdk-11-jdk"
    echo ""
    echo "To install Java on CentOS/RHEL/Fedora:"
    echo "  sudo dnf install java-11-openjdk-devel"
    exit 1
fi

echo "Starting EzAir..."

# Launch with software rendering to avoid graphics issues
exec java \
    -Dprism.order=sw \
    -Dprism.verbose=false \
    -Djavafx.platform=linux \
    -Djava.awt.headless=false \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone="Europe/Bucharest" \
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.desktop/sun.awt=ALL-UNNAMED \
    -jar "$JAR_FILE" "$@"
EOF

chmod +x "$APPDIR/usr/bin/ezair"

# Create AppRun (the main entry point for AppImage)
echo "Creating AppRun..."
cat > "$APPDIR/AppRun" << 'EOF'
#!/bin/bash
# AppImage Entry Point

# Get the directory where this AppRun is located
HERE="$(dirname "$(readlink -f "${0}")")"

# Launch the application
exec "$HERE/usr/bin/ezair" "$@"
EOF

chmod +x "$APPDIR/AppRun"

# Create desktop entry
echo "Creating desktop entry..."
cat > "$APPDIR/ezair.desktop" << EOF
[Desktop Entry]
Type=Application
Name=EzAir
Comment=EzAir Flight Management System
Exec=ezair
Icon=ezair
Categories=Office;Utility;
Terminal=false
StartupNotify=true
EOF

# Copy desktop entry to the applications directory as well
cp "$APPDIR/ezair.desktop" "$APPDIR/usr/share/applications/"

# Create a simple icon
echo "Creating application icon..."
cat > "$APPDIR/ezair.svg" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<svg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
  <rect width="64" height="64" fill="#2196F3" rx="8"/>
  <text x="32" y="40" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" fill="white">E</text>
  <text x="32" y="20" font-family="Arial, sans-serif" font-size="8" text-anchor="middle" fill="white">✈</text>
</svg>
EOF

# Copy icon to pixmaps
cp "$APPDIR/ezair.svg" "$APPDIR/usr/share/pixmaps/"

# Create a version info file
cat > "$APPDIR/VERSION" << EOF
EzAir Flight Management System
Version: $APP_VERSION
Build Date: $(date)
Java Target: 11+
Platform: Linux x86_64
Package: AppImage
EOF

# Check if appimagetool exists
if [ ! -f "$APPIMAGETOOL" ]; then
    echo "Error: appimagetool not found at $APPIMAGETOOL"
    echo "Please download it first"
    exit 1
fi

# Build the AppImage
echo "Building AppImage..."
cd "$TARGET_DIR"

# Set environment variable to avoid desktop integration questions
export NO_APPSTREAM=1

# Build the AppImage
"$APPIMAGETOOL" "$APPDIR" "EzAir-$APP_VERSION-x86_64.AppImage"

# Make the AppImage executable
chmod +x "EzAir-$APP_VERSION-x86_64.AppImage"

echo ""
echo "=== AppImage Build Complete ==="
echo "AppImage created: $TARGET_DIR/EzAir-$APP_VERSION-x86_64.AppImage"
echo ""
echo "File size: $(du -h "$TARGET_DIR/EzAir-$APP_VERSION-x86_64.AppImage" | cut -f1)"
echo ""
echo "To test the AppImage:"
echo "  cd $TARGET_DIR"
echo "  ./EzAir-$APP_VERSION-x86_64.AppImage"
echo ""
echo "The AppImage is fully portable and can be distributed as-is!"
