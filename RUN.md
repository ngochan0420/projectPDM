
## 1. How to run (IntelliJ IDEA)

1. **Create the database** in MySQL Workbench:
   ```sql
   CREATE DATABASE loandb;
   ```
   Then open `database/schema.sql`, add `USE loandb;` as the first line, and run it.
   Do the same with `database/sample_data.sql`.
   Check: `SELECT COUNT(*) FROM loandb.Application;` should return 21.
2. **Set your MySQL password** in `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/loandb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
   db.user=root
   db.password=your_password
   ```
3. **Open the project**: IntelliJ → *File → Open* → select the `loan-app` folder (the one with `pom.xml`). Maven downloads the MySQL driver and FlatLaf automatically.
4. Open `src/main/java/com/pdm/loan/Main.java` and click **Run**.
