#!/bin/bash

# Ensure the build directory exists
mkdir -p build

echo "Compiling project..."

# Compile the source files in the correct order
javac -d build -cp "lib/*:." \
  src/main/java/database/*.java \
  src/main/java/models/*.java \
  src/main/java/dao/*.java \
  src/main/java/ui/CustomJDateChooser.java \
  src/main/java/ui/MainFrame.java \
  src/main/java/ui/EmployeePanel.java \
  src/main/java/ui/AttendancePanel.java \
  src/main/java/ui/SalaryPanel.java \
  src/main/java/ui/SalaryReportPanel.java

if [ $? -eq 0 ]; then
  echo "Compilation successful. Running application..."
  # Run the application
  java -cp "build:lib/*" ui.MainFrame
else
  echo "Compilation failed."
fi 