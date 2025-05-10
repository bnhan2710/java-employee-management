@echo off

REM Ensure the build directory exists
if not exist build mkdir build

echo Compiling project...

REM Compile the source files in the correct order
javac -verbose -d build -cp "lib/*;." ^
  src/main/java/database/*.java ^
  src/main/java/models/*.java ^
  src/main/java/dao/*.java ^
  src/main/java/ui/CustomJDateChooser.java ^
  src/main/java/ui/MainFrame.java ^
  src/main/java/ui/EmployeePanel.java ^
  src/main/java/ui/AttendancePanel.java ^
  src/main/java/ui/SalaryPanel.java ^
  src/main/java/ui/SalaryReportPanel.java

if %ERRORLEVEL% EQU 0 (
  echo Compilation successful. Running application...
  REM Run the application
  java -cp "build;lib/*" ui.MainFrame
) else (
  echo Compilation failed.
  echo Error code: %ERRORLEVEL%
)

pause 