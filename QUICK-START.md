1. Start your MySQL Server
2. Create a database named `employee_management`:
   ```sql
   CREATE DATABASE employee_management;
   ```
   
## Database Configuration

Edit the `src/main/java/database/DatabaseConnection.java` file to match your MySQL settings:

```java
private static final String URL = "jdbc:mysql://localhost:3306/employee_management";
private static final String USER = "root"; // Change to your MySQL username
private static final String PASSWORD = ""; // Change to your MySQL password
```

## Running the Application

### Method 1: Using IntelliJ IDEA (Recommended)

1. Open the project in IntelliJ IDEA
2. Navigate to `src/main/java/ui/MainFrame.java`
3. Right-click and select "Run 'MainFrame.main()'"

### Method 2: Using the Run Script

- On Linux/Mac:
  ```
  ./run.sh
  ```

- On Windows:
  ```
  run.bat
  ```
