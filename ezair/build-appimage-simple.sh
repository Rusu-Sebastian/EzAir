#!/bin/bash
echo "=== EzAir AppImage Builder ==="
echo "Building AppImage for EzAir JavaFX application..."

APP_VERSION="1.0"
SCRIPT_DIR="$(pwd)"
TARGET_DIR="$SCRIPT_DIR/target"
APPDIR="$TARGET_DIR/EzAir.AppDir"
APPIMAGETOOL="/tmp/appimagetool-x86_64.AppImage"

echo "Creating AppImage directory structure..."
rm -rf "$APPDIR"
mkdir -p "$APPDIR/usr/bin"
mkdir -p "$APPDIR/usr/lib"
mkdir -p "$APPDIR/usr/share/applications"
mkdir -p "$APPDIR/usr/share/pixmaps"

echo "Building application JAR if needed..."
if [ ! -f "$TARGET_DIR/ezair-executable.jar" ]; then
    mvn clean package -q
fi

echo "Copying application files..."
cp "$TARGET_DIR/ezair-executable.jar" "$APPDIR/usr/lib/"
