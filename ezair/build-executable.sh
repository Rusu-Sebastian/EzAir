#!/bin/bash
# EzAir Build Script - Creates a native-like executable for Linux

set -e  # Exit on any error

echo "=== EzAir Executable Builder ==="
echo "Building Linux executable for EzAir JavaFX application..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="$SCRIPT_DIR/target"
DIST_DIR="$TARGET_DIR/ezair-linux-dist"

# Clean and create distribution directory
echo "Creating distribution directory..."
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# Check if fat JAR exists
if [ ! -f "$TARGET_DIR/ezair-executable.jar" ]; then
    echo "Fat JAR not found. Building project first..."
    mvn clean package -q
fi

# Copy the fat JAR to distribution directory
echo "Copying application JAR..."
cp "$TARGET_DIR/ezair-executable.jar" "$DIST_DIR/"

# Create the main launcher script
echo "Creating launcher script..."
cat > "$DIST_DIR/ezair" << 'EOF'
#!/bin/bash
# EzAir Application Launcher

# Get the directory where this script is located
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$APP_DIR/ezair-executable.jar"

# Check if Java is installed
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

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Error: Java 11 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

# Check if the JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: Application JAR file not found at $JAR_FILE"
    exit 1
fi

# Set up JavaFX module path and launch the application
echo "Starting EzAir..."

# Try to find JavaFX installation paths
JAVAFX_PATHS=(
    "/usr/share/maven-repo/org/openjfx"
    "/usr/share/openjfx/lib"
    "/usr/lib/jvm/java-11-openjdk*/lib/javafx"
    "/usr/lib/jvm/default-java/lib/javafx"
    "/opt/javafx/lib"
    "$HOME/.javafx/lib"
)

MODULE_PATH_ARGS=""
for path in "${JAVAFX_PATHS[@]}"; do
    if [ -d "$path" ]; then
        # For Maven repo structure, use all jars
        if [[ "$path" == *"maven-repo"* ]]; then
            MODULE_PATH_ARGS="--module-path $path/javafx-base/11:$path/javafx-controls/11:$path/javafx-fxml/11:$path/javafx-graphics/11"
            break
        # For lib structure, look for javafx.base.jar
        elif [ -f "$path"/javafx.base.jar ]; then
            MODULE_PATH_ARGS="--module-path $path"
            break
        fi
    fi
done

# If JavaFX modules are not found, try to run without module path (fat JAR should contain them)
if [ -z "$MODULE_PATH_ARGS" ]; then
    echo "JavaFX modules not found in system, using embedded JavaFX from fat JAR..."
    # Attempt to run with just the JAR file (relying on embedded JavaFX modules)
    exec java \
        -Dfile.encoding=UTF-8 \
        -Duser.timezone="Europe/Bucharest" \
        -jar "$JAR_FILE" "$@"
else
    echo "Using system JavaFX installation..."
    exec java \
        $MODULE_PATH_ARGS \
        --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
        --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED \
        --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
        --add-opens java.base/java.lang=ALL-UNNAMED \
        --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
        -Dfile.encoding=UTF-8 \
        -Duser.timezone="Europe/Bucharest" \
        -jar "$JAR_FILE" "$@"
fi
EOF

# Make launcher executable
chmod +x "$DIST_DIR/ezair"

# Create a desktop entry file for system integration
echo "Creating desktop entry..."
cat > "$DIST_DIR/ezair.desktop" << EOF
[Desktop Entry]
Name=EzAir
Comment=EzAir Flight Management System
Exec=$DIST_DIR/ezair
Icon=$DIST_DIR/ezair-icon.png
Terminal=false
Type=Application
Categories=Office;Utility;
StartupNotify=true
EOF

# Create an install script
echo "Creating install script..."
cat > "$DIST_DIR/install.sh" << 'EOF'
#!/bin/bash
# EzAir Installation Script

set -e

APP_NAME="ezair"
INSTALL_DIR="/opt/$APP_NAME"
BIN_DIR="/usr/local/bin"
DESKTOP_DIR="/usr/share/applications"

echo "=== EzAir Installation ==="
echo "This script will install EzAir to $INSTALL_DIR"
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script as root (use sudo):"
    echo "sudo ./install.sh"
    exit 1
fi

# Create installation directory
echo "Creating installation directory..."
mkdir -p "$INSTALL_DIR"

# Copy files
echo "Copying application files..."
cp ezair-executable.jar "$INSTALL_DIR/"
cp ezair.desktop "$INSTALL_DIR/"

# Create system-wide launcher
echo "Creating system launcher..."
cat > "$BIN_DIR/ezair" << LAUNCHER_EOF
#!/bin/bash
exec "$INSTALL_DIR/ezair-executable.jar" "\$@"
LAUNCHER_EOF

# Alternative launcher that uses the directory-based approach
cat > "$BIN_DIR/ezair-gui" << LAUNCHER_EOF2
#!/bin/bash
cd "$INSTALL_DIR"

# Try to find JavaFX installation paths
JAVAFX_PATHS=(
    "/usr/share/maven-repo/org/openjfx"
    "/usr/share/openjfx/lib"
    "/usr/lib/jvm/java-11-openjdk*/lib/javafx"
    "/usr/lib/jvm/default-java/lib/javafx"
    "/opt/javafx/lib"
    "\$HOME/.javafx/lib"
)

MODULE_PATH_ARGS=""
for path in "\${JAVAFX_PATHS[@]}"; do
    if [ -d "\$path" ]; then
        # For Maven repo structure, use all jars
        if [[ "\$path" == *"maven-repo"* ]]; then
            MODULE_PATH_ARGS="--module-path \$path/javafx-base/11:\$path/javafx-controls/11:\$path/javafx-fxml/11:\$path/javafx-graphics/11"
            break
        # For lib structure, look for javafx.base.jar
        elif [ -f "\$path"/javafx.base.jar ]; then
            MODULE_PATH_ARGS="--module-path \$path"
            break
        fi
    fi
done

# If JavaFX modules are not found, try to run without module path
if [ -z "\$MODULE_PATH_ARGS" ]; then
    echo "JavaFX modules not found in system."
    echo "Attempting to run with embedded JavaFX (fat JAR)..."
    java -jar "$INSTALL_DIR/ezair-executable.jar" "\$@"
else
    echo "Using system JavaFX installation..."
    java \\
        \$MODULE_PATH_ARGS \\
        --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \\
        --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED \\
        --add-opens java.base/java.lang.reflect=ALL-UNNAMED \\
        --add-opens java.base/java.lang=ALL-UNNAMED \\
        --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \\
        -Dfile.encoding=UTF-8 \\
        -Duser.timezone="Europe/Bucharest" \\
        -jar "$INSTALL_DIR/ezair-executable.jar" "\$@"
fi
LAUNCHER_EOF2

chmod +x "$BIN_DIR/ezair"
chmod +x "$BIN_DIR/ezair-gui"

# Install desktop entry
if [ -d "$DESKTOP_DIR" ]; then
    echo "Installing desktop entry..."
    sed "s|Exec=.*|Exec=$BIN_DIR/ezair-gui|" ezair.desktop > "$DESKTOP_DIR/ezair.desktop"
    chmod 644 "$DESKTOP_DIR/ezair.desktop"
fi

echo ""
echo "=== Installation Complete ==="
echo "EzAir has been installed successfully!"
echo ""
echo "You can now run EzAir in the following ways:"
echo "1. From command line: ezair-gui"
echo "2. From applications menu: Look for 'EzAir'"
echo "3. From file manager: Double-click the desktop file"
echo ""
echo "To uninstall, run: sudo rm -rf $INSTALL_DIR $BIN_DIR/ezair $BIN_DIR/ezair-gui $DESKTOP_DIR/ezair.desktop"
EOF

chmod +x "$DIST_DIR/install.sh"

# Create an uninstall script
echo "Creating uninstall script..."
cat > "$DIST_DIR/uninstall.sh" << 'EOF'
#!/bin/bash
# EzAir Uninstallation Script

INSTALL_DIR="/opt/ezair"
BIN_DIR="/usr/local/bin"
DESKTOP_DIR="/usr/share/applications"

echo "=== EzAir Uninstallation ==="

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script as root (use sudo):"
    echo "sudo ./uninstall.sh"
    exit 1
fi

echo "Removing EzAir files..."
rm -rf "$INSTALL_DIR"
rm -f "$BIN_DIR/ezair" "$BIN_DIR/ezair-gui"
rm -f "$DESKTOP_DIR/ezair.desktop"

echo "EzAir has been uninstalled successfully!"
EOF

chmod +x "$DIST_DIR/uninstall.sh"

# Create README for the distribution
echo "Creating documentation..."
cat > "$DIST_DIR/README.txt" << 'EOF'
EzAir Linux Distribution
========================

This package contains the EzAir Flight Management System for Linux.

REQUIREMENTS:
- Java 11 or higher
- Linux with GUI support (X11/Wayland)

QUICK START:
1. Run: ./ezair
   This will start the application directly from this directory.

SYSTEM INSTALLATION:
1. Run: sudo ./install.sh
   This will install EzAir system-wide.

2. After installation, you can run EzAir by:
   - Typing 'ezair-gui' in terminal
   - Finding "EzAir" in your applications menu

MANUAL EXECUTION:
If you prefer to run without installation:
java --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
     --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED \
     --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
     -jar ezair-executable.jar

TROUBLESHOOTING:
- If you get "JavaFX runtime components are missing" error, install OpenJFX:
  Ubuntu/Debian: sudo apt update && sudo apt install openjfx
  CentOS/RHEL/Fedora: sudo dnf install java-openjfx

- If the application doesn't start, check that Java 11+ is installed:
  java -version

- If you see Java errors related to modules or JavaFX, try:
  1. Install OpenJFX as mentioned above
  2. Verify that your Java installation is compatible with JavaFX
  3. Run with the full JavaFX module path:
     java --module-path /usr/share/openjfx/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -jar ezair-executable.jar

UNINSTALLATION:
If you installed system-wide, run: sudo ./uninstall.sh

For support, please contact the development team.
EOF

# Create a simple version info file
cat > "$DIST_DIR/VERSION" << EOF
EzAir Flight Management System
Version: 1.0
Build Date: $(date)
Java Target: 11+
Platform: Linux
EOF

# Create the final package
echo "Creating distribution archive..."
cd "$TARGET_DIR"
ARCHIVE_NAME="ezair-linux-x64-v1.0.tar.gz"
tar -czf "$ARCHIVE_NAME" -C . "ezair-linux-dist"

echo ""
echo "=== Build Complete ==="
echo "Linux executable package created successfully!"
echo ""
echo "Distribution directory: $DIST_DIR"
echo "Archive created: $TARGET_DIR/$ARCHIVE_NAME"
echo ""
echo "To test the application:"
echo "  cd $DIST_DIR"
echo "  ./ezair"
echo ""
echo "To install system-wide:"
echo "  cd $DIST_DIR"
echo "  sudo ./install.sh"
echo ""
echo "Archive contents:"
ls -la "$DIST_DIR"
