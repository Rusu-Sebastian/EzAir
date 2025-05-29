#!/bin/bash
# EzAir CLI Utility
# This script provides command-line utility functions for EzAir

set -e  # Exit on any error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/target/ezair-executable.jar"
DIST_DIR="$SCRIPT_DIR/target/ezair-linux-dist"

function print_help() {
    echo "EzAir CLI Utility"
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  run            Run the EzAir application"
    echo "  version        Show version information"
    echo "  test           Test if the application can run"
    echo "  verify         Verify JAR integrity"
    echo "  extract        Extract JAR files to inspect contents"
    echo "  install        Install system-wide"
    echo "  help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 run         # Run the application"
    echo "  $0 version     # Show version information"
    echo "  $0 verify      # Verify JAR integrity"
}

function check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        echo "Error: JAR file not found at $JAR_FILE"
        echo "Please build the project first using './build-executable.sh'"
        exit 1
    fi
}

function run_app() {
    check_jar
    echo "Starting EzAir application..."
    if [ -d "$DIST_DIR" ]; then
        cd "$DIST_DIR" && ./ezair
    else
        echo "Distribution directory not found. Running directly from JAR..."
        # Try to find JavaFX installation paths
        JAVAFX_PATHS=(
            "/usr/share/maven-repo/org/openjfx"
            "/usr/share/openjfx/lib"
            "/usr/lib/jvm/java-11-openjdk*/lib/javafx"
            "/usr/lib/jvm/default-java/lib/javafx"
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

        if [ -z "$MODULE_PATH_ARGS" ]; then
            echo "JavaFX modules not found in system, using embedded JavaFX from fat JAR..."
            java -jar "$JAR_FILE"
        else
            echo "Using system JavaFX installation..."
            java $MODULE_PATH_ARGS \
                --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
                --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED \
                --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
                --add-opens java.base/java.lang=ALL-UNNAMED \
                --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
                -Dfile.encoding=UTF-8 \
                -jar "$JAR_FILE"
        fi
    fi
}

function show_version() {
    check_jar
    echo "EzAir Application"
    echo "Version: 1.0"
    echo "Build Date: $(date -r "$JAR_FILE")"
    echo "JAR File: $JAR_FILE"
    echo "JAR Size: $(du -h "$JAR_FILE" | cut -f1)"
    echo ""
    echo "Java Version:"
    java -version
    echo ""
    if [ -d "/usr/share/maven-repo/org/openjfx" ] || [ -d "/usr/share/openjfx/lib" ]; then
        echo "JavaFX: Installed"
    else
        echo "JavaFX: Not found in system paths"
    fi
}

function test_app() {
    check_jar
    echo "Testing JAR file..."
    
    # Check module info class
    if jar tf "$JAR_FILE" | grep -q "module-info.class"; then
        echo "✓ Module info found"
    else
        echo "✗ Module info not found"
    fi
    
    # Check main class
    if jar tf "$JAR_FILE" | grep -q "com/proiect/App.class"; then
        echo "✓ Main class found"
    else
        echo "✗ Main class not found"
    fi
    
    # Check JavaFX libraries
    if jar tf "$JAR_FILE" | grep -q "javafx"; then
        echo "✓ JavaFX libraries included"
    else
        echo "✗ JavaFX libraries not included"
    fi
    
    # Check MySQL connector
    if jar tf "$JAR_FILE" | grep -q "mysql"; then
        echo "✓ MySQL connector included"
    else
        echo "✗ MySQL connector not included"
    fi
    
    echo ""
    echo "Checking for JavaFX installation..."
    JAVAFX_FOUND=false
    for path in "/usr/share/maven-repo/org/openjfx" "/usr/share/openjfx/lib"; do
        if [ -d "$path" ]; then
            echo "✓ JavaFX found at: $path"
            JAVAFX_FOUND=true
            break
        fi
    done
    
    if [ "$JAVAFX_FOUND" = false ]; then
        echo "✗ JavaFX not found in standard system paths"
    fi
    
    echo ""
    echo "Java environment check:"
    java -version 2>&1
}

function verify_jar() {
    check_jar
    echo "Verifying JAR integrity..."
    if jar tf "$JAR_FILE" > /dev/null 2>&1; then
        echo "✓ JAR file is valid"
    else
        echo "✗ JAR file is corrupt"
    fi
    
    echo ""
    echo "JAR Manifest Contents:"
    if unzip -q -c "$JAR_FILE" META-INF/MANIFEST.MF 2>/dev/null; then
        echo "✓ Manifest found"
    else
        echo "✗ No manifest found"
    fi
    
    echo ""
    echo "Checking required classes:"
    for class in "module-info.class" "com/proiect/App.class"; do
        if jar tf "$JAR_FILE" | grep -q "$class"; then
            echo "✓ $class found"
        else
            echo "✗ $class not found"
        fi
    done
}

function extract_jar() {
    check_jar
    EXTRACT_DIR="$SCRIPT_DIR/target/extracted-jar"
    echo "Extracting JAR to $EXTRACT_DIR..."
    rm -rf "$EXTRACT_DIR"
    mkdir -p "$EXTRACT_DIR"
    cd "$EXTRACT_DIR"
    jar xf "$JAR_FILE"
    echo "✓ JAR extracted successfully"
    echo ""
    echo "JAR Contents Summary:"
    find . -type d -maxdepth 2 | sort
    echo ""
    echo "To inspect the extracted files, go to: $EXTRACT_DIR"
}

function install_app() {
    if [ -d "$DIST_DIR" ] && [ -f "$DIST_DIR/install.sh" ]; then
        echo "Installing EzAir system-wide..."
        cd "$DIST_DIR" && sudo ./install.sh
    else
        echo "Distribution package not found. Please run build-executable.sh first."
        exit 1
    fi
}

# Main script logic
case "$1" in
    run)
        run_app
        ;;
    version)
        show_version
        ;;
    test)
        test_app
        ;;
    verify)
        verify_jar
        ;;
    extract)
        extract_jar
        ;;
    install)
        install_app
        ;;
    help|--help|-h|"")
        print_help
        ;;
    *)
        echo "Unknown command: $1"
        print_help
        exit 1
        ;;
esac

exit 0
