#!/bin/bash

# Set the base directory to the current directory or specify a different path
BASE_DIR=$(pwd)  # Use the current directory
# Uncomment and set the following line if you want to specify a different directory
# BASE_DIR="/Users/yourusername/Code/_eclipse"

# Set the path to the jd-gui jar file
JD_GUI_JAR="/Applications/jd-gui-1.6.6.jar"  # Specify the path to your jd-gui jar file

# Output directory for the decompiled Java files
OUTPUT_DIR="$BASE_DIR/decompiled_java"

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Find and decompile all .class files in the base directory
find "$BASE_DIR" -type f -name '*.class' | while read -r class_file; do
    # Decompile the .class file to .java using jd-gui
    java -jar "$JD_GUI_JAR" "$class_file" --output-dir "$OUTPUT_DIR"
done

echo "Decompilation complete. Decompiled files are located in $OUTPUT_DIR."