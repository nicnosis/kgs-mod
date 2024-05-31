#!/bin/bash

# Path to the JD-GUI JAR file
JD_GUI_JAR="/Applications/jd-gui-1.6.6.jar"

# Check if the JAR file exists
if [ ! -f "$JD_GUI_JAR" ]; then
    echo "JD-GUI JAR file not found: $JD_GUI_JAR"
    exit 1
fi

# Loop through all .class files in the current directory and its subdirectories
find ./ -type f -name '*.class' | while read -r class_file; do
    # Extract the directory and filename without extension
    dir_name=$(dirname "$class_file")
    base_name=$(basename "$class_file" .class)

    # Run JD-GUI to decompile the .class file
    java -jar "$JD_GUI_JAR" -nofern -nogui "$class_file" "$dir_name/$base_name.java"
done


# Save this script as convert.sh in the directory containing your .class files. Make it executable using:
# chmod +x convert.sh


# Then run it from the command line:
# ./convert.sh