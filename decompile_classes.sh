#!/bin/bash

# Base directory
BASE_DIR="/Users/nicnosis/Code/_eclipse"

# Directory containing the .class files
CLASS_DIR="$BASE_DIR/gogo"

# Output directory for the .java files
OUTPUT_DIR="$CLASS_DIR/decompiled"

# Ensure the output directory exists
mkdir -p "$OUTPUT_DIR"

# Function to decompile a single .class file
decompile_class() {
    local class_file=$1
    local output_file=$2
    
    # Run jd-cli to decompile the .class file to a .java file
    java -jar /Applications/jd-cli.jar "$class_file" --output-dir "$OUTPUT_DIR"
}

# Export the function so it can be used with find
export -f decompile_class

# Find and decompile all .class files
find "$CLASS_DIR" -type f -name '*.class' -exec bash -c 'decompile_class "$0"' {} \;

echo "Decompilation complete. Check the $OUTPUT_DIR directory for .java files."