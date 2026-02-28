#!/bin/bash

# Compile the Java files
echo "Compiling Java files..."
javac -cp "lib/sqlite-jdbc-3.45.1.0.jar" -d out src/*.java

# Run the application
echo "Starting Database & File Manager..."
java -cp "out:lib/sqlite-jdbc-3.45.1.0.jar" DatabaseApp
