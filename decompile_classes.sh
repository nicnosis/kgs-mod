#!/bin/bash

# Specify the base directory where the .class files are located
# Change this path to match your directory structure
BASE_DIR="$(pwd)"

# Specify the output directory where the decompiled .java files will be saved
# Change this path to match your desired output directory
OUTPUT_DIR="$BASE_DIR/decompiled"

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Find all .class files in the base directory and its subdirectories
find "$BASE_DIR" -type f -name '*.class' | while read -r class_file; do
    # Print the name of the file being decompiled
    echo "Decompiling $class_file"
    
    # Use CFR to decompile each .class file and save the output to the specified directory
    cfr-decompiler "$class_file" --outputdir "$OUTPUT_DIR"
done
