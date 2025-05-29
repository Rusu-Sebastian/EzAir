# EzAir AppImage Installation Guide

This guide will help you install and run the EzAir flight management application on Linux using the AppImage format.

## What is EzAir?

EzAir is a JavaFX-based flight management application that allows users to:
- Browse and book flights
- Manage user accounts
- View booking history
- Receive notifications about flight changes

## System Requirements

- Linux-based operating system (Ubuntu, Mint, Fedora, etc.)
- Java 11 or higher
- OpenJFX (JavaFX) libraries

## Installation

### Step 1: Install Required Dependencies

Before running EzAir, you need to install Java and the JavaFX libraries:

**Ubuntu/Debian/Mint:**
```bash
sudo apt update
sudo apt install openjdk-11-jdk openjfx libopenjfx-jni
```

**Fedora/RHEL:**
```bash
sudo dnf install java-11-openjdk java-11-openjdk-devel openjfx
```

**Arch Linux:**
```bash
sudo pacman -S jdk11-openjdk java-openjfx
```

### Step 2: Make the AppImage Executable

1. Download the EzAir-1.0-x86_64.AppImage file
2. Open a terminal and navigate to the download location
3. Make the AppImage executable:

```bash
chmod +x EzAir-1.0-x86_64.AppImage
```

### Step 3: Run the Application

Simply double-click the AppImage file in your file manager, or run it from the terminal:

```bash
./EzAir-1.0-x86_64.AppImage
```

## Optional: Integrating with your Desktop

### Creating a Desktop Shortcut

1. Move the AppImage to a permanent location (e.g., ~/Applications):

```bash
mkdir -p ~/Applications
mv EzAir-1.0-x86_64.AppImage ~/Applications/
```

2. Create a desktop entry file:

```bash
cat > ~/.local/share/applications/ezair.desktop << EOF
[Desktop Entry]
Type=Application
Name=EzAir
Comment=EzAir Flight Management System
Exec=~/Applications/EzAir-1.0-x86_64.AppImage
Icon=java
Categories=Office;Utility;
Terminal=false
StartupNotify=true
EOF
```

## Troubleshooting

### JavaFX Libraries Not Found

If you see an error about missing JavaFX runtime components, ensure you have installed the OpenJFX package for your distribution as described in Step 1.

### Graphics Rendering Issues

The application uses the system's JavaFX libraries with hardware rendering. If you experience graphics issues, you may need to install additional graphics drivers for your system.

### Application Won't Start

If the application fails to start:

1. Try running it from the terminal to see error messages
2. Verify Java is installed correctly: `java -version`
3. Check that JavaFX is installed: `ls -la /usr/share/openjfx/lib`

## Uninstallation

To uninstall EzAir:

1. Delete the AppImage file
2. If you created a desktop shortcut, remove it:

```bash
rm ~/.local/share/applications/ezair.desktop
```

## Additional Resources

For more information or support, please refer to the project documentation or contact the development team.
