package com.smartqueue.droidsql.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility helper to store and retrieve SQL keyword tutorial explanations and interactive examples.
 * Explains when, why, and how each keyword is used, in the specific detailed format requested.
 */
public class SQLTutorialHelper {

    public static class TutorialItem {
        public final String keyword;
        public final String category;
        public final String description;
        public final String syntax;
        public final String example;

        public TutorialItem(String keyword, String category, String description, String syntax, String example) {
            this.keyword = keyword;
            this.category = category;
            this.description = description;
            this.syntax = syntax;
            this.example = example;
        }
    }

    private static final List<TutorialItem> ITEMS = new ArrayList<>();

    static {
        initDQL();
        initDML();
        initDDL();
        initConstraintsAndJoins();
        initOperatorsAndLogic();
        initFunctions();
        initProgrammability();
    }

    public static List<TutorialItem> getItems() {
        return ITEMS;
    }

    private static void initDQL() {
        ITEMS.add(new TutorialItem(
            "SELECT",
            "Data Query (DQL)",
            "The `SELECT` statement is the most fundamental query in SQL. It is used to fetch (retrieve) records from one or more database tables and return them as a result set. You can query all columns, specific columns, calculated expressions, or apply functions directly on the selected data.\n\n#### Retrieve specific columns\n```sql\nSELECT name, email FROM users;\n```\n\n#### Retrieve all columns\n```sql\nSELECT * FROM users;\n```\n\n#### Perform math calculations and use aliases\n```sql\nSELECT name, salary, (salary * 12) AS annual_salary FROM employees;\n```\n\n### Why is `SELECT` Used?\n\n* To retrieve specific details or fields from a table based on need.\n* To view the entire dataset of a table using the wildcard operator `*`.\n* To fetch computed values, mathematical transformations, or formatted output strings.",
            "SELECT column1, column2 FROM table_name;",
            "SELECT name, email FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "FROM",
            "Data Query (DQL)",
            "The `FROM` clause is a mandatory component of standard `SELECT` and `DELETE` statements. It specifies the target source tables or views from which the database engine should retrieve or remove records. It sets the primary dataset context.\n\n#### Simple table selection\n```sql\nSELECT name FROM employees;\n```\n\n#### Multiple tables in a join query\n```sql\nSELECT orders.id, customers.name \nFROM orders \nJOIN customers ON orders.customer_id = customers.id;\n```\n\n### Why is `FROM` Used?\n\n* To specify the primary table containing the source records.\n* To link multiple tables together when writing JOIN queries.\n* To declare subquery results or virtual views as sources of data.",
            "SELECT columns FROM table_name;",
            "SELECT * FROM orders;"
        ));

        ITEMS.add(new TutorialItem(
            "WHERE",
            "Data Query (DQL)",
            "The `WHERE` clause filters the rows returned by a query, ensuring that only records satisfying specific conditions are retrieved, updated, or deleted. It operates row-by-row and is crucial for isolating target datasets.\n\n#### Filter numbers\n```sql\nSELECT * FROM users WHERE age >= 21;\n```\n\n#### Filter text matching and logical combinations\n```sql\nSELECT * FROM users WHERE country = 'USA' AND status = 'active';\n```\n\n#### Filter within updates to target specific rows\n```sql\nUPDATE users SET status = 'suspended' WHERE registration_date < '2025-01-01';\n```\n\n### Why is `WHERE` Used?\n\n* To filter rows dynamically based on column values.\n* To prevent bulk updates or deletions by target filtering.\n* To match text patterns, date boundaries, or range values.",
            "SELECT columns FROM table WHERE condition;",
            "SELECT * FROM users WHERE age >= 18;"
        ));

        ITEMS.add(new TutorialItem(
            "GROUP BY",
            "Data Query (DQL)",
            "The `GROUP BY` statement groups rows that have matching values in specified columns into aggregate summary rows (like finding the total count of customers per country). It is almost always used in combination with aggregate functions (like COUNT, SUM, AVG, MIN, MAX).\n\n#### Group by category and count rows\n```sql\nSELECT country, COUNT(*) AS total_users \nFROM users \nGROUP BY country;\n```\n\n#### Group by multiple columns with sums\n```sql\nSELECT department, job_title, SUM(salary) AS total_payroll \nFROM employees \nGROUP BY department, job_title;\n```\n\n### Why is `GROUP BY` Used?\n\n* To consolidate duplicate rows into distinct summary categories.\n* To compute aggregate summaries (totals, averages, counts) over categorized segments.\n* To run analytical reports on structured datasets.",
            "SELECT column, COUNT(*) FROM table GROUP BY column;",
            "SELECT status, COUNT(*) FROM orders GROUP BY status;"
        ));

        ITEMS.add(new TutorialItem(
            "HAVING",
            "Data Query (DQL)",
            "The `HAVING` clause is used to filter the grouped results produced by a `GROUP BY` clause. While the `WHERE` clause filters individual rows before grouping, the `HAVING` clause filters the aggregated summaries after the grouping takes place.\n\n#### Filter aggregated count\n```sql\nSELECT country, COUNT(*) AS total \nFROM users \nGROUP BY country \nHAVING COUNT(*) > 5;\n```\n\n#### Filter aggregate sums\n```sql\nSELECT department, AVG(salary) AS avg_sal \nFROM employees \nGROUP BY department \nHAVING AVG(salary) > 50000;\n```\n\n### Why is `HAVING` Used?\n\n* To apply filtering conditions directly on aggregated outputs (which `WHERE` cannot do).\n* To exclude categories or groups that do not meet statistical thresholds.\n* To perform complex post-aggregation audits.",
            "SELECT column, SUM(val) FROM table GROUP BY column HAVING SUM(val) > limit;",
            "SELECT status, SUM(amount) FROM orders GROUP BY status HAVING SUM(amount) > 1000;"
        ));

        ITEMS.add(new TutorialItem(
            "ORDER BY",
            "Data Query (DQL)",
            "The `ORDER BY` clause sorts the result set of a query in ascending (`ASC`) or descending (`DESC`) order. Sorting can be applied to numeric columns, text (alphabetical), dates, or custom expressions.\n\n#### Sort ascending (default)\n```sql\nSELECT * FROM users ORDER BY name ASC;\n```\n\n#### Sort descending\n```sql\nSELECT * FROM products ORDER BY price DESC;\n```\n\n#### Multi-column sorting priorities\n```sql\nSELECT * FROM employees ORDER BY department ASC, salary DESC;\n```\n\n### Why is `ORDER BY` Used?\n\n* To display outputs in a structured, readable order for users.\n* To sort records prior to applying limits (e.g., getting the top 5 values).\n* To sort by multiple priorities (e.g., sorting by department, then by employee name).",
            "SELECT columns FROM table ORDER BY column [ASC|DESC];",
            "SELECT * FROM users ORDER BY age DESC;"
        ));

        ITEMS.add(new TutorialItem(
            "LIMIT",
            "Data Query (DQL)",
            "The `LIMIT` clause restricts the maximum number of rows returned by a query. When coupled with the `OFFSET` clause, it allows skip-counting, which is critical for implementing pagination in applications.\n\n#### Limit raw rows\n```sql\nSELECT * FROM logs LIMIT 5;\n```\n\n#### Limit with offsets (skip first 10, return next 5)\n```sql\nSELECT * FROM users LIMIT 5 OFFSET 10;\n```\n\n### Why is `LIMIT` Used?\n\n* To optimize performance by avoiding fetching massive datasets.\n* To retrieve the 'Top N' rows (e.g., fetching only the top 10 rows).\n* To implement paginated browsing (loading pages of data dynamically).",
            "SELECT columns FROM table LIMIT count [OFFSET offset];",
            "SELECT * FROM users LIMIT 10 OFFSET 20;"
        ));

        ITEMS.add(new TutorialItem(
            "TOP",
            "Data Query (DQL)",
            "The `TOP` clause is used to limit the number or percentage of rows returned from a query result set. (Note: Primarily used in SQL Server and MS Access. SQLite and MySQL use `LIMIT` instead).\n\n#### Retrieve top N rows\n```sql\nSELECT TOP 5 * FROM products ORDER BY price DESC;\n```\n\n#### Retrieve top percentage of rows\n```sql\nSELECT TOP 10 PERCENT * FROM employees ORDER BY hire_date ASC;\n```\n\n### Why is `TOP` Used?\n\n* To restrict returned rows in Microsoft-based database systems.\n* To fetch high-ranking elements easily.\n* To retrieve a subset of results based on percentage values using TOP PERCENT.",
            "SELECT TOP number columns FROM table_name;",
            "SELECT TOP 5 * FROM products ORDER BY price DESC;"
        ));

        ITEMS.add(new TutorialItem(
            "ROWNUM",
            "Data Query (DQL)",
            "The `ROWNUM` pseudo-column returns a number indicating the order in which a row was selected. (Note: Primarily used in Oracle SQL to limit the size of query results).\n\n#### Simple row limiting\n```sql\nSELECT * FROM employees WHERE ROWNUM <= 10;\n```\n\n#### Nested pagination\n```sql\nSELECT * FROM (SELECT a.*, ROWNUM rnum FROM employees a) WHERE rnum BETWEEN 10 AND 20;\n```\n\n### Why is `ROWNUM` Used?\n\n* To limit query rows in Oracle databases (similar to SQLite's LIMIT).\n* To assign temporary row indexes dynamically to results.\n* To execute nested queries for complex pagination configurations.",
            "SELECT columns FROM table WHERE ROWNUM <= limit;",
            "SELECT * FROM employees WHERE ROWNUM <= 5;"
        ));

        ITEMS.add(new TutorialItem(
            "DISTINCT",
            "Data Query (DQL)",
            "The `DISTINCT` keyword is used in conjunction with `SELECT` to eliminate duplicate rows from the query output. It ensures that only unique values are returned.\n\n#### Retrieve unique values\n```sql\nSELECT DISTINCT city FROM customers;\n```\n\n#### Count unique entries\n```sql\nSELECT COUNT(DISTINCT country) AS unique_countries FROM users;\n```\n\n### Why is `DISTINCT` Used?\n\n* To extract clean, non-duplicate attributes from a column.\n* To count unique combinations of rows (e.g., counting unique countries).\n* To optimize reporting results by filtering out redundant entries.",
            "SELECT DISTINCT column FROM table_name;",
            "SELECT DISTINCT department FROM employees;"
        ));

        ITEMS.add(new TutorialItem(
            "AS",
            "Data Query (DQL)",
            "The `AS` keyword creates a temporary alias name for a column or a table. Aliases exist only for the duration of the query and make outputs more readable or query writing shorter.\n\n#### Alias for columns\n```sql\nSELECT first_name AS name, phone_number AS contact FROM users;\n```\n\n#### Alias for calculated fields\n```sql\nSELECT price, tax, (price + tax) AS total_cost FROM products;\n```\n\n#### Alias for tables in joins\n```sql\nSELECT e.name, d.dept_name \nFROM employees AS e \nJOIN departments AS d ON e.dept_id = d.id;\n```\n\n### Why is `AS` Used?\n\n* To rename columns in reports for end-user readability.\n* To assign names to computed columns, mathematical formulas, or aggregations.\n* To shorten table names when writing complex joins.",
            "SELECT column AS alias FROM table AS t_alias;",
            "SELECT salary * 12 AS annual_salary FROM employees;"
        ));

        ITEMS.add(new TutorialItem(
            "UNION",
            "Data Query (DQL)",
            "The `UNION` operator combines the result sets of two or more `SELECT` statements into a single, unified result set. It automatically removes all duplicate rows between the queries. (Note: All queries must return the same number of columns, with matching datatypes and order).\n\n#### Combine unique emails from two tables\n```sql\nSELECT email FROM customers \nUNION \nSELECT email FROM suppliers;\n```\n\n#### Merge query sets with matching structures\n```sql\nSELECT city, 'Buyer' AS role FROM customers \nUNION \nSELECT city, 'Seller' AS role FROM suppliers;\n```\n\n### Why is `UNION` Used?\n\n* To combine matching data from completely different tables into one list.\n* To perform distinct set union operations across datasets.\n* To build unified reports from historical and active archive tables.",
            "SELECT col FROM t1 UNION SELECT col FROM t2;",
            "SELECT email FROM clients UNION SELECT email FROM leads;"
        ));

        ITEMS.add(new TutorialItem(
            "UNION ALL",
            "Data Query (DQL)",
            "The `UNION ALL` operator merges the result sets of two or more `SELECT` statements, retaining all duplicate rows. It operates much faster than `UNION` because it does not perform the CPU-heavy sorting required to remove duplicates.\n\n#### Merge all logs including duplicates\n```sql\nSELECT log_date, msg FROM error_logs \nUNION ALL \nSELECT log_date, msg FROM system_logs;\n```\n\n#### Combine tables rapidly\n```sql\nSELECT city FROM offices \nUNION ALL \nSELECT city FROM branches;\n```\n\n### Why is `UNION ALL` Used?\n\n* To rapidly combine datasets when duplicates are expected or allowed.\n* To preserve absolute row counts across tables.\n* To compile all entries from multiple logging/transaction tables.",
            "SELECT col FROM t1 UNION ALL SELECT col FROM t2;",
            "SELECT city FROM offices UNION ALL SELECT city FROM branches;"
        ));

        ITEMS.add(new TutorialItem(
            "EXCEPT",
            "Data Query (DQL)",
            "The `EXCEPT` (or `MINUS` in some SQL dialects) operator returns all rows from the first query that are not present in the second query's result set. It computes the mathematical set difference.\n\n#### Find users without orders\n```sql\nSELECT id FROM users \nEXCEPT \nSELECT user_id FROM orders;\n```\n\n#### Compare table differences\n```sql\nSELECT name, salary FROM employees \nEXCEPT \nSELECT name, salary FROM archive_employees;\n```\n\n### Why is `EXCEPT` Used?\n\n* To isolate unique rows belonging exclusively to the first table.\n* To find missing associations (e.g., users who haven't placed orders).\n* To compare database snapshots for changes.",
            "SELECT col FROM t1 EXCEPT SELECT col FROM t2;",
            "SELECT id FROM users EXCEPT SELECT user_id FROM orders;"
        ));

        ITEMS.add(new TutorialItem(
            "INTERSECT",
            "Data Query (DQL)",
            "The `INTERSECT` operator returns only the rows that are present in the result sets of both queries. It computes the mathematical set intersection.\n\n#### Find active customers who are also suppliers\n```sql\nSELECT email FROM customers \nINTERSECT \nSELECT email FROM suppliers;\n```\n\n#### Filter shared values\n```sql\nSELECT city FROM clients \nINTERSECT \nSELECT city FROM partners;\n```\n\n### Why is `INTERSECT` Used?\n\n* To identify common shared records between two tables.\n* To run strict matching audits across systems.\n* To filter datasets based on overlapping identifiers.",
            "SELECT col FROM t1 INTERSECT SELECT col FROM t2;",
            "SELECT customer_id FROM vip_list INTERSECT SELECT customer_id FROM active_buyers;"
        ));

        ITEMS.add(new TutorialItem(
            "WITH",
            "Data Query (DQL)",
            "The `WITH` clause defines Common Table Expressions (CTEs). A CTE is a temporary named result set that exists solely for the execution duration of a larger query. It acts as an inline temporary view.\n\n#### Simple CTE query\n```sql\nWITH young_users AS (\n  SELECT * FROM users WHERE age < 25\n)\nSELECT name, email FROM young_users WHERE country = 'USA';\n```\n\n#### Multiple CTEs in a single query\n```sql\nWITH \n  dept_costs AS (SELECT dept_id, SUM(salary) AS total FROM employees GROUP BY dept_id),\n  max_dept AS (SELECT MAX(total) AS max_val FROM dept_costs)\nSELECT dept_id FROM dept_costs, max_dept WHERE total = max_val;\n```\n\n### Why is `WITH` Used?\n\n* To simplify highly nested, complex subqueries, improving code readability.\n* To modularize complex calculations and reuse them multiple times in a query.\n* To build recursive hierarchy operations (e.g., listing managers and direct reports).",
            "WITH cte_name AS (SELECT query) SELECT * FROM cte_name;",
            "WITH high_sal AS (SELECT * FROM employees WHERE salary > 5000) SELECT * FROM high_sal;"
        ));

    }

    private static void initDML() {
        ITEMS.add(new TutorialItem(
            "INSERT INTO",
            "Data Manipulation (DML)",
            "The `INSERT INTO` statement adds new rows of data to a table. You can insert explicit manual values, bulk insert rows, or select and copy rows from other tables.\n\n#### Insert standard row\n```sql\nINSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');\n```\n\n#### Bulk insert multiple rows\n```sql\nINSERT INTO users (name, age) VALUES ('Bob', 22), ('Charlie', 30);\n```\n\n#### Insert from SELECT query\n```sql\nINSERT INTO archive_users SELECT * FROM users WHERE status = 'inactive';\n```\n\n### Why is `INSERT INTO` Used?\n\n* To register new user inputs or transactional records.\n* To copy subsets of rows from one table to another dynamically.\n* To populate default datasets.",
            "INSERT INTO table_name (col1, col2) VALUES (val1, val2);",
            "INSERT INTO users (name, email) VALUES ('David', 'david@example.com');"
        ));

        ITEMS.add(new TutorialItem(
            "VALUES",
            "Data Manipulation (DML)",
            "The `VALUES` keyword defines the data rows to be written into columns when executing `INSERT INTO` or `REPLACE` commands.\n\n#### Single row constructor\n```sql\nINSERT INTO products (name, price) VALUES ('Book', 15.99);\n```\n\n#### Multi-row values list\n```sql\nINSERT INTO tags (name) VALUES ('SQL'), ('Database'), ('Mobile');\n```\n\n### Why is `VALUES` Used?\n\n* To provide constant value mapping arrays.\n* To insert multiple rows inside a single execution block.\n* To serve as row builders.",
            "INSERT INTO table VALUES (val1, val2), (val3, val4);",
            "INSERT INTO users (name, age) VALUES ('Sam', 22), ('Emma', 29);"
        ));

        ITEMS.add(new TutorialItem(
            "INTO",
            "Data Manipulation (DML)",
            "The `INTO` keyword acts as a destination marker. It specifies the target table for insertions (in `INSERT INTO`), defines target tables during database copy commands (in `SELECT INTO`), or maps values to variables in procedural blocks.\n\n#### Specify target in INSERT\n```sql\nINSERT INTO archive_logs VALUES ('Error cleared');\n```\n\n#### Copy table structure and data into a new table\n```sql\nSELECT * INTO backup_employees FROM employees;\n```\n\n### Why is `INTO` Used?\n\n* To explicitly designate the target table for data write operations.\n* To copy records directly into newly created tables.\n* To assign query results into variables in PL/SQL or T-SQL scripts.",
            "INSERT INTO table_name ... OR SELECT * INTO new_table FROM old_table;",
            "SELECT * INTO backup_users FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "UPDATE",
            "Data Manipulation (DML)",
            "The `UPDATE` statement modifies existing records in a table. It updates column values for rows matching specific filter conditions. (WARNING: Always use a `WHERE` clause with `UPDATE` to prevent updating all records in the table!).\n\n#### Update single column\n```sql\nUPDATE users SET status = 'active' WHERE id = 12;\n```\n\n#### Update multiple columns\n```sql\nUPDATE employees SET salary = 60000, dept = 'Sales' WHERE id = 45;\n```\n\n#### Perform calculation updates\n```sql\nUPDATE products SET price = price * 1.10 WHERE category = 'Electronics';\n```\n\n### Why is `UPDATE` Used?\n\n* To modify, correct, or refresh old column data.\n* To perform bulk data transitions conditionally (e.g., activating accounts).\n* To increment numeric fields based on specific events.",
            "UPDATE table_name SET col = val WHERE condition;",
            "UPDATE users SET status = 'active' WHERE age >= 18;"
        ));

        ITEMS.add(new TutorialItem(
            "SET",
            "Data Manipulation (DML)",
            "The `SET` keyword specifies the columns to be modified and their new values during an `UPDATE` statement. It pairs target columns with new expressions or constants.\n\n#### Assign new value to column\n```sql\nUPDATE users SET country = 'Canada' WHERE id = 8;\n```\n\n#### Assign multiple values to different columns\n```sql\nUPDATE employees SET role = 'Manager', salary = salary + 5000 WHERE id = 3;\n```\n\n### Why is `SET` Used?\n\n* To assign values to target fields in updates.\n* To list multiple column updates in a single statement.\n* To reset variables or change column values conditionally.",
            "UPDATE table SET col1 = val1, col2 = val2 WHERE condition;",
            "UPDATE employees SET salary = salary * 1.05 WHERE dept = 'IT';"
        ));

        ITEMS.add(new TutorialItem(
            "DELETE",
            "Data Manipulation (DML)",
            "The `DELETE` statement removes existing records from a table. It drops target rows matching the filter condition but preserves the table structure and schemas. (WARNING: Always use a `WHERE` clause to avoid clearing the entire table!).\n\n#### Delete matching rows\n```sql\nDELETE FROM users WHERE age < 18;\n```\n\n#### Delete expired data\n```sql\nDELETE FROM sessions WHERE expire_time < 1716000000;\n```\n\n### Why is `DELETE` Used?\n\n* To remove outdated, expired, or invalid records from tables.\n* To clean up temporary tables or session entries.\n* To purge records matching specific status criteria.",
            "DELETE FROM table_name WHERE condition;",
            "DELETE FROM sessions WHERE expire_time < 1716000000;"
        ));

        ITEMS.add(new TutorialItem(
            "TRUNCATE TABLE",
            "Data Manipulation (DML)",
            "The `TRUNCATE TABLE` command quickly deletes all records from a table. Unlike `DELETE`, it bypasses transactional logs and row triggers, making it extremely fast. (Note: SQLite does not support this natively; use `DELETE FROM table;` combined with `DELETE FROM sqlite_sequence;` to reset keys).\n\n#### Fast truncate table\n```sql\nTRUNCATE TABLE staging_data;\n```\n\n#### Standard SQLite equivalent (delete all and reset auto-increment)\n```sql\nDELETE FROM staging_data;\nDELETE FROM sqlite_sequence WHERE name = 'staging_data';\n```\n\n### Why is `TRUNCATE TABLE` Used?\n\n* To rapidly empty large logs or staging tables.\n* To reset autoincrement primary keys instantly.\n* To save database processing resources.",
            "TRUNCATE TABLE table_name;",
            "TRUNCATE TABLE temp_logs;"
        ));

        ITEMS.add(new TutorialItem(
            "REPLACE",
            "Data Manipulation (DML)",
            "The `REPLACE` (or `INSERT OR REPLACE`) command inserts a new row, or overwrites an existing row if a unique key or primary key conflict occurs. It acts as a standard SQL extension for simple upsert behavior.\n\n#### Insert or overwrite setting\n```sql\nREPLACE INTO settings (key, value) VALUES ('dark_mode', 'true');\n```\n\n#### Insert or overwrite conflict data\n```sql\nINSERT OR REPLACE INTO users (id, name, email) VALUES (5, 'New Name', 'new@example.com');\n```\n\n### Why is `REPLACE` Used?\n\n* To implement simple insert-or-update operations in a single query.\n* To handle unique constraint conflicts gracefully without throwing exceptions.\n* To overwrite old config settings or logs automatically.",
            "REPLACE INTO table_name (id, col) VALUES (1, 'new_value');",
            "REPLACE INTO configs (key, val) VALUES ('theme', 'dark');"
        ));

    }

    private static void initDDL() {
        ITEMS.add(new TutorialItem(
            "CREATE",
            "Data Definition (DDL)",
            "The `CREATE` statement establishes new database structures. You can create tables, indexes, views, triggers, and full databases.\n\n#### Create Table\n```sql\nCREATE TABLE members (\n  id INTEGER PRIMARY KEY,\n  name TEXT NOT NULL\n);\n```\n\n#### Create Index\n```sql\nCREATE INDEX idx_name ON members (name);\n```\n\n### Why is `CREATE` Used?\n\n* To initialize table schemas with specific column rules.\n* To build views and indexing tools.\n* To construct the core layout of database containers.",
            "CREATE TABLE|VIEW|INDEX|TRIGGER name (...);",
            "CREATE TABLE logs (id INT, message TEXT);"
        ));

        ITEMS.add(new TutorialItem(
            "DATABASE",
            "Data Definition (DDL)",
            "The `DATABASE` keyword identifies database container targets when executing initialization or drop commands.\n\n#### Create database\n```sql\nCREATE DATABASE sales_db;\n```\n\n#### Drop database\n```sql\nDROP DATABASE sales_db;\n```\n\n### Why is `DATABASE` Used?\n\n* To spin up isolated storage spaces.\n* To destroy obsolete database containers.\n* To execute database-wide operations.",
            "CREATE DATABASE db_name; OR DROP DATABASE db_name;",
            "CREATE DATABASE production_db;"
        ));

        ITEMS.add(new TutorialItem(
            "TABLE",
            "Data Definition (DDL)",
            "The `TABLE` keyword declares a physical relational table object target when executing schema definitions.\n\n#### Create Table\n```sql\nCREATE TABLE test (val INT);\n```\n\n#### Drop Table\n```sql\nDROP TABLE test;\n```\n\n### Why is `TABLE` Used?\n\n* To declare structure limits during table setup.\n* To alter existing table columns.\n* To drop tables.",
            "CREATE TABLE name (...); OR DROP TABLE name;",
            "CREATE TABLE products (sku TEXT PRIMARY KEY, price REAL);"
        ));

        ITEMS.add(new TutorialItem(
            "COLUMN",
            "Data Definition (DDL)",
            "The `COLUMN` keyword targets structural column operations when executing `ALTER TABLE` actions.\n\n#### Add column to table\n```sql\nALTER TABLE users ADD COLUMN age INT;\n```\n\n#### Drop column from table\n```sql\nALTER TABLE users DROP COLUMN age;\n```\n\n### Why is `COLUMN` Used?\n\n* To add new columns to active schemas.\n* To drop columns in databases that support column deletion.\n* To rename columns for documentation clarity.",
            "ALTER TABLE name ADD COLUMN column_name datatype;",
            "ALTER TABLE users ADD COLUMN age INTEGER;"
        ));

        ITEMS.add(new TutorialItem(
            "INDEX",
            "Data Definition (DDL)",
            "The `INDEX` keyword configures index structures to speed up database lookups on indexed fields.\n\n#### Create index on single column\n```sql\nCREATE INDEX idx_user_email ON users (email);\n```\n\n#### Create composite index on multiple columns\n```sql\nCREATE INDEX idx_user_country_age ON users (country, age);\n```\n\n#### Drop index\n```sql\nDROP INDEX idx_user_email;\n```\n\n### Why is `INDEX` Used?\n\n* To optimize query times from slow O(N) scans to fast O(log N) lookups.\n* To support unique composite constraints.\n* To remove index structures when writing operations slow down.",
            "CREATE INDEX index_name ON table (column);",
            "CREATE INDEX idx_user_email ON users (email);"
        ));

        ITEMS.add(new TutorialItem(
            "VIEW",
            "Data Definition (DDL)",
            "The `VIEW` keyword creates a virtual table referencing a stored `SELECT` query definition.\n\n#### Create basic view\n```sql\nCREATE VIEW active_users AS \nSELECT id, name FROM users WHERE status = 'active';\n```\n\n#### Create view joining tables\n```sql\nCREATE VIEW order_summaries AS \nSELECT o.id, c.name, o.amount \nFROM orders o \nJOIN customers c ON o.customer_id = c.id;\n```\n\n### Why is `VIEW` Used?\n\n* To encapsulate complex join statements.\n* To enforce query security by hiding sensitive columns.\n* To present consistent interface abstractions.",
            "CREATE VIEW view_name AS SELECT query;",
            "CREATE VIEW active_users AS SELECT id, name FROM users WHERE status = 'active';"
        ));

        ITEMS.add(new TutorialItem(
            "DROP",
            "Data Definition (DDL)",
            "The `DROP` statement permanently deletes tables, indexes, views, or triggers.\n\n#### Drop table if exists\n```sql\nDROP TABLE IF EXISTS old_logs;\n```\n\n#### Drop index\n```sql\nDROP INDEX idx_logs;\n```\n\n### Why is `DROP` Used?\n\n* To completely remove deprecated schemas.\n* To reclaim system storage instantly.\n* To delete triggers, indexes, or views to restructure them.",
            "DROP TABLE|VIEW|INDEX|TRIGGER name;",
            "DROP TABLE IF EXISTS old_logs;"
        ));

        ITEMS.add(new TutorialItem(
            "ALTER",
            "Data Definition (DDL)",
            "The `ALTER` statement modifies structures (adding/dropping columns, renaming tables).\n\n#### Rename table\n```sql\nALTER TABLE employees RENAME TO staff;\n```\n\n#### Rename column\n```sql\nALTER TABLE staff RENAME COLUMN phone_number TO contact_no;\n```\n\n### Why is `ALTER` Used?\n\n* To evolve database structures without losing existing rows.\n* To add, rename, or delete columns.\n* To attach or detach constraints.",
            "ALTER TABLE table_name ACTION;",
            "ALTER TABLE employees RENAME TO staff;"
        ));

        ITEMS.add(new TutorialItem(
            "RENAME",
            "Data Definition (DDL)",
            "The `RENAME` keyword changes names of tables or columns without modifying datatypes.\n\n#### Rename table\n```sql\nALTER TABLE users RENAME TO customers;\n```\n\n#### Rename column\n```sql\nALTER TABLE customers RENAME COLUMN user_name TO username;\n```\n\n### Why is `RENAME` Used?\n\n* To update table naming conventions.\n* To adjust column names for clarity.\n* To preserve data while refactoring schema objects.",
            "ALTER TABLE table RENAME TO new_name; OR ALTER TABLE table RENAME COLUMN old TO new;",
            "ALTER TABLE users RENAME COLUMN user_name TO username;"
        ));

        ITEMS.add(new TutorialItem(
            "ADD",
            "Data Definition (DDL)",
            "The `ADD` keyword appends columns or constraints to table definitions dynamically.\n\n#### Add column with default value\n```sql\nALTER TABLE customers ADD COLUMN balance REAL DEFAULT 0.0;\n```\n\n#### Add column constraints\n```sql\nALTER TABLE users ADD COLUMN phone_no TEXT UNIQUE;\n```\n\n### Why is `ADD` Used?\n\n* To inject new columns into live tables.\n* To append new constraints like foreign keys or uniques.\n* To dynamically update database schemas.",
            "ALTER TABLE table ADD COLUMN column_name datatype; OR ALTER TABLE table ADD CONSTRAINT name ...;",
            "ALTER TABLE customers ADD COLUMN balance REAL DEFAULT 0.0;"
        ));

        ITEMS.add(new TutorialItem(
            "CASCADE",
            "Data Definition (DDL)",
            "The `CASCADE` keyword propagates actions automatically (e.g. cascading drops to dependent views or deletes to child foreign keys).\n\n#### Cascade deletion in constraints\n```sql\nCREATE TABLE profiles (\n  id INT PRIMARY KEY,\n  user_id INT,\n  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE\n);\n```\n\n#### Cascade drops\n```sql\nDROP TABLE users CASCADE;\n```\n\n### Why is `CASCADE` Used?\n\n* To clean up child rows automatically when parent rows are deleted.\n* To drop tables and automatically clear all dependent objects.\n* To ensure referential database integrity.",
            "FOREIGN KEY (...) REFERENCES table (...) ON DELETE CASCADE; OR DROP TABLE name CASCADE;",
            "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE"
        ));

    }

    private static void initConstraintsAndJoins() {
        ITEMS.add(new TutorialItem(
            "PRIMARY KEY",
            "Constraints",
            "A `PRIMARY KEY` uniquely identifies each record in a table. It must contain unique, non-null values.\n\n#### Single primary key column\n```sql\nCREATE TABLE items (id INTEGER PRIMARY KEY, title TEXT);\n```\n\n#### Composite primary key\n```sql\nCREATE TABLE enrollments (\n  student_id INT,\n  course_id INT,\n  PRIMARY KEY (student_id, course_id)\n);\n```\n\n### Why is `PRIMARY KEY` Used?\n\n* To guarantee that each row has a unique identifier.\n* To index rows automatically for rapid primary lookups.\n* To establish the parent key for foreign relations.",
            "column_name datatype PRIMARY KEY;",
            "CREATE TABLE items (id INTEGER PRIMARY KEY, title TEXT);"
        ));

        ITEMS.add(new TutorialItem(
            "FOREIGN KEY",
            "Constraints",
            "A `FOREIGN KEY` links a column in a child table to the primary key of a parent table.\n\n#### Simple foreign key link\n```sql\nCREATE TABLE orders (\n  id INT PRIMARY KEY,\n  uid INT,\n  FOREIGN KEY(uid) REFERENCES users(id)\n);\n```\n\n#### Foreign key with cascade options\n```sql\nCREATE TABLE posts (\n  id INT PRIMARY KEY,\n  author_id INT,\n  FOREIGN KEY(author_id) REFERENCES users(id) ON DELETE CASCADE\n);\n```\n\n### Why is `FOREIGN KEY` Used?\n\n* To establish relationships between tables.\n* To prevent orphan records (ensuring parent keys exist).\n* To support cascading updates and deletes.",
            "FOREIGN KEY (local_col) REFERENCES foreign_table (foreign_col);",
            "CREATE TABLE orders (id INT, uid INT, FOREIGN KEY(uid) REFERENCES users(id));"
        ));

        ITEMS.add(new TutorialItem(
            "UNIQUE",
            "Constraints",
            "The `UNIQUE` constraint ensures that all values in a column or set of columns are different (no duplicates allowed).\n\n#### Unique column constraint\n```sql\nCREATE TABLE users (id INT, email TEXT UNIQUE;\n```\n\n#### Unique constraint on multiple columns\n```sql\nCREATE TABLE seats (\n  row_no INT,\n  seat_no INT,\n  UNIQUE (row_no, seat_no)\n);\n```\n\n### Why is `UNIQUE` Used?\n\n* To prevent duplicate credentials (e.g., duplicate usernames or emails).\n* To establish alternative lookup keys alongside primary keys.\n* To automatically generate indexing on constrained columns.",
            "column_name datatype UNIQUE;",
            "CREATE TABLE users (id INT, email TEXT UNIQUE);"
        ));

        ITEMS.add(new TutorialItem(
            "CHECK",
            "Constraints",
            "The `CHECK` constraint limits the value range that can be placed in a column.\n\n#### Simple check constraint\n```sql\nCREATE TABLE employees (id INT, age INT CHECK (age >= 18));\n```\n\n#### Check constraint on multiple columns\n```sql\nCREATE TABLE products (\n  price REAL,\n  discount REAL,\n  CHECK (price > 0 AND discount >= 0 AND discount < price)\n);\n```\n\n### Why is `CHECK` Used?\n\n* To validate values at the database level (e.g., ensuring age >= 18).\n* To enforce strict value ranges or patterns.\n* To block bad inputs before write operations.",
            "column_name datatype CHECK (condition);",
            "CREATE TABLE employees (id INT, age INT CHECK (age >= 18));"
        ));

        ITEMS.add(new TutorialItem(
            "DEFAULT",
            "Constraints",
            "The `DEFAULT` constraint provides a default value for a column if no value is specified when inserting a record.\n\n#### Default text\n```sql\nCREATE TABLE tasks (id INT, status TEXT DEFAULT 'pending');\n```\n\n#### Default math/time function\n```sql\nCREATE TABLE logs (msg TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);\n```\n\n### Why is `DEFAULT` Used?\n\n* To automatically populate fields (e.g., timestamps or status fields).\n* To simplify insert queries by allowing optional fields.\n* To ensure fallback values exist.",
            "column_name datatype DEFAULT default_value;",
            "CREATE TABLE task (id INT, status TEXT DEFAULT 'pending');"
        ));

        ITEMS.add(new TutorialItem(
            "NOT NULL",
            "Constraints",
            "The `NOT NULL` constraint enforces that a column cannot accept NULL values.\n\n#### Not null constraint\n```sql\nCREATE TABLE members (id INT, username TEXT NOT NULL);\n```\n\n#### Combining constraints\n```sql\nCREATE TABLE staff (id INT, email TEXT NOT NULL UNIQUE);\n```\n\n### Why is `NOT NULL` Used?\n\n* To make critical fields mandatory (e.g., passwords or titles).\n* To avoid errors when performing calculations on columns.\n* To keep data clean and complete.",
            "column_name datatype NOT NULL;",
            "CREATE TABLE members (id INT, username TEXT NOT NULL);"
        ));

        ITEMS.add(new TutorialItem(
            "REFERENCES",
            "Constraints",
            "The `REFERENCES` keyword defines the target table and columns when establishing a foreign key relationship constraint.\n\n#### Define foreign key target\n```sql\nFOREIGN KEY (user_id) REFERENCES users (id);\n```\n\n#### Target composite keys\n```sql\nFOREIGN KEY (class_id, sec_id) REFERENCES classes (id, sec_id);\n```\n\n### Why is `REFERENCES` Used?\n\n* To target parent tables in relationships.\n* To direct data verification for foreign key rows.\n* To declare dependency trees.",
            "FOREIGN KEY (col) REFERENCES parent_table (parent_col);",
            "FOREIGN KEY (user_id) REFERENCES users (id)"
        ));

        ITEMS.add(new TutorialItem(
            "ON",
            "Joins & Constraints",
            "The `ON` keyword defines join condition comparisons (`ON t1.id = t2.id`), specifies trigger event targets, or defines cascade events.\n\n#### ON in table joins\n```sql\nSELECT * FROM users u JOIN profiles p ON u.id = p.user_id;\n```\n\n#### ON in cascade constraints\n```sql\nFOREIGN KEY (uid) REFERENCES users (id) ON DELETE SET NULL;\n```\n\n#### ON in trigger events\n```sql\nCREATE TRIGGER log_user AFTER INSERT ON users BEGIN ... END;\n```\n\n### Why is `ON` Used?\n\n* To link tables on key fields during queries.\n* To specify triggers to run `ON table_name`.\n* To define cascade events (e.g., `ON DELETE CASCADE`).",
            "JOIN table ON t1.id = t2.id; OR ON DELETE ACTION;",
            "SELECT * FROM users u JOIN profiles p ON u.id = p.user_id;"
        ));

        ITEMS.add(new TutorialItem(
            "CONSTRAINT",
            "Constraints",
            "The `CONSTRAINT` keyword is used to name constraints in a table definition.\n\n#### Named primary key constraint\n```sql\nCREATE TABLE accounts (id INT, CONSTRAINT pk_accounts PRIMARY KEY(id));\n```\n\n#### Named unique constraint\n```sql\nCREATE TABLE users (\n  email TEXT,\n  CONSTRAINT uq_email UNIQUE(email)\n);\n```\n\n### Why is `CONSTRAINT` Used?\n\n* To assign custom names to constraint rules for error debugging.\n* To declare multi-column keys dynamically.\n* To support dropping specific constraints later by name.",
            "CONSTRAINT constraint_name CONSTRAINT_TYPE (columns);",
            "CREATE TABLE accounts (id INT, CONSTRAINT pk_accounts PRIMARY KEY(id));"
        ));

        ITEMS.add(new TutorialItem(
            "JOIN",
            "Table Joins",
            "The `JOIN` keyword combines rows from two or more tables based on related columns. By default, it acts as an `INNER JOIN`.\n\n#### Basic join query\n```sql\nSELECT u.name, o.item FROM users u JOIN orders o ON u.id = o.user_id;\n```\n\n#### Multiple joins in a single query\n```sql\nSELECT u.name, o.item, p.payment_method \nFROM users u \nJOIN orders o ON u.id = o.user_id \nJOIN payments p ON o.id = p.order_id;\n```\n\n### Why is `JOIN` Used?\n\n* To link normalized tables during queries.\n* To pull related details spread across tables.\n* To assemble comprehensive reports from multiple entities.",
            "SELECT cols FROM t1 JOIN t2 ON t1.key = t2.key;",
            "SELECT u.name, o.item FROM users u JOIN orders o ON u.id = o.user_id;"
        ));

        ITEMS.add(new TutorialItem(
            "INNER JOIN",
            "Table Joins",
            "The `INNER JOIN` keyword selects records that have matching values in both tables. Unmatched rows are completely excluded.\n\n#### Simple inner join\n```sql\nSELECT * FROM users u INNER JOIN orders o ON u.id = o.user_id;\n```\n\n#### Inner join with filtering\n```sql\nSELECT u.name, o.amount \nFROM users u \nINNER JOIN orders o ON u.id = o.user_id \nWHERE o.amount > 500;\n```\n\n### Why is `INNER JOIN` Used?\n\n* To filter out rows that have no relationships in the linked table.\n* To extract intersecting data between tables.\n* To create strict, consistent query results.",
            "SELECT cols FROM t1 INNER JOIN t2 ON t1.key = t2.key;",
            "SELECT * FROM users u INNER JOIN orders o ON u.id = o.user_id;"
        ));

        ITEMS.add(new TutorialItem(
            "LEFT JOIN",
            "Table Joins",
            "The `LEFT JOIN` (or LEFT OUTER JOIN) selects all records from the left table and matching rows from the right. Unmatched right rows return NULL.\n\n#### List all users and their orders\n```sql\nSELECT u.name, o.amount FROM users u LEFT JOIN orders o ON u.id = o.user_id;\n```\n\n#### Find users who have never placed an order\n```sql\nSELECT u.name FROM users u LEFT JOIN orders o ON u.id = o.user_id WHERE o.id IS NULL;\n```\n\n### Why is `LEFT JOIN` Used?\n\n* To list all primary table elements regardless of child relations.\n* To identify records missing dependencies (where right key IS NULL).\n* To retain unmatched records in analytical queries.",
            "SELECT cols FROM t1 LEFT JOIN t2 ON t1.key = t2.key;",
            "SELECT u.name, o.amount FROM users u LEFT JOIN orders o ON u.id = o.user_id;"
        ));

        ITEMS.add(new TutorialItem(
            "RIGHT JOIN",
            "Table Joins",
            "The `RIGHT JOIN` selects all records from the right table and matching rows from the left. (Note: SQLite does not support this natively; rewrite as LEFT JOIN).\n\n#### Right join (Standard SQL)\n```sql\nSELECT u.name, o.amount FROM users u RIGHT JOIN orders o ON u.id = o.user_id;\n```\n\n#### Equivalent SQLite left join\n```sql\nSELECT u.name, o.amount FROM orders o LEFT JOIN users u ON o.user_id = u.id;\n```\n\n### Why is `RIGHT JOIN` Used?\n\n* To extract all right table records regardless of left table relations.\n* To audit missing parent elements in the left table.\n* To match standard SQL specifications.",
            "SELECT cols FROM t1 RIGHT JOIN t2 ON t1.key = t2.key;",
            "SELECT u.name, o.amount FROM orders o LEFT JOIN users u ON o.user_id = u.id;"
        ));

        ITEMS.add(new TutorialItem(
            "OUTER JOIN",
            "Table Joins",
            "The `OUTER JOIN` keyword references outer table joins (LEFT, RIGHT, or FULL) that preserve unmatched rows by padding them with NULLs.\n\n#### Left outer join example\n```sql\nSELECT u.name, o.amount FROM users u LEFT OUTER JOIN orders o ON u.id = o.user_id;\n```\n\n#### Right outer join example\n```sql\nSELECT u.name, o.amount FROM users u RIGHT OUTER JOIN orders o ON u.id = o.user_id;\n```\n\n### Why is `OUTER JOIN` Used?\n\n* To compile all entries from both sides of the join.\n* To handle optional relationships dynamically.\n* To preserve datasets fully.",
            "SELECT cols FROM t1 LEFT/RIGHT/FULL OUTER JOIN t2 ON conditions;",
            "SELECT u.name, o.amount FROM users u LEFT OUTER JOIN orders o ON u.id = o.user_id;"
        ));

        ITEMS.add(new TutorialItem(
            "FULL OUTER JOIN",
            "Table Joins",
            "The `FULL OUTER JOIN` returns all records when there is a match in either left or right table. Unmatched values return NULL on both sides. (Note: Not supported natively in SQLite; simulate with UNION of LEFT and RIGHT joins).\n\n#### Full outer join (Standard SQL)\n```sql\nSELECT * FROM users u FULL OUTER JOIN orders o ON u.id = o.user_id;\n```\n\n#### Simulate FULL OUTER JOIN in SQLite\n```sql\nSELECT u.name, o.amount FROM users u LEFT JOIN orders o ON u.id = o.user_id\nUNION\nSELECT u.name, o.amount FROM orders o LEFT JOIN users u ON o.user_id = u.id;\n```\n\n### Why is `FULL OUTER JOIN` Used?\n\n* To fully combine and audit matching/unmatching records from both sides.\n* To map complete sets of disjointed relationships.\n* To build comprehensive data comparisons.",
            "SELECT cols FROM t1 FULL OUTER JOIN t2 ON t1.key = t2.key;",
            "SELECT * FROM users u FULL OUTER JOIN orders o ON u.id = o.user_id;"
        ));

        ITEMS.add(new TutorialItem(
            "USING",
            "Table Joins",
            "The `USING` keyword is a shorthand used to specify join column keys when both tables share the exact same column names.\n\n#### Join using a single key\n```sql\nSELECT * FROM users JOIN orders USING (user_id);\n```\n\n#### Multiple joins using keys\n```sql\nSELECT * FROM employees JOIN departments USING (dept_id) JOIN locations USING (loc_id);\n```\n\n### Why is `USING` Used?\n\n* To simplify syntax and write cleaner queries.\n* To join tables easily on standard keys (like id or user_id).\n* To avoid repeating column matches using ON.",
            "SELECT cols FROM t1 JOIN t2 USING (common_column);",
            "SELECT * FROM users JOIN orders USING (user_id);"
        ));

    }

    private static void initOperatorsAndLogic() {
        ITEMS.add(new TutorialItem(
            "AND",
            "Logical Operators",
            "The `AND` operator filters records by returning TRUE only if all of the separated conditions are true.\n\n#### Combine equality filters\n```sql\nSELECT * FROM users WHERE status = 'active' AND age >= 18;\n```\n\n#### Multiple AND conditions\n```sql\nSELECT * FROM products WHERE category = 'Electronics' AND price < 500 AND stock > 0;\n```\n\n### Why is `AND` Used?\n\n* To build precise queries requiring multiple conditions.\n* To filter data ranges (e.g., age >= 18 AND age <= 30).\n* To narrow query scopes.",
            "SELECT cols FROM table WHERE cond1 AND cond2;",
            "SELECT * FROM users WHERE status = 'active' AND age >= 18;"
        ));

        ITEMS.add(new TutorialItem(
            "OR",
            "Logical Operators",
            "The `OR` operator filters records by returning TRUE if any of the separated conditions are true.\n\n#### Alternative equality checks\n```sql\nSELECT * FROM users WHERE country = 'USA' OR country = 'Canada';\n```\n\n#### OR with range checks\n```sql\nSELECT * FROM products WHERE price < 10 OR stock > 100;\n```\n\n### Why is `OR` Used?\n\n* To expand filters to allow multiple alternative matching constraints.\n* To extract rows matching at least one criteria from a list.\n* To match diverse records in a single query.",
            "SELECT cols FROM table WHERE cond1 OR cond2;",
            "SELECT * FROM users WHERE country = 'USA' OR country = 'Canada';"
        ));

        ITEMS.add(new TutorialItem(
            "NOT",
            "Logical Operators",
            "The `NOT` operator negates a condition, returning TRUE if the condition is false.\n\n#### Exclude specific value\n```sql\nSELECT * FROM users WHERE NOT country = 'UK';\n```\n\n#### Invert set checks\n```sql\nSELECT * FROM products WHERE category NOT IN ('Toys', 'Books');\n```\n\n#### Invert pattern checks\n```sql\nSELECT * FROM users WHERE email NOT LIKE '%@example.com';\n```\n\n### Why is `NOT` Used?\n\n* To exclude specific records or values.\n* To invert conditional operators (e.g., NOT LIKE, NOT IN, NOT NULL).\n* To query inverse segments.",
            "SELECT cols FROM table WHERE NOT condition;",
            "SELECT * FROM users WHERE NOT country = 'UK';"
        ));

        ITEMS.add(new TutorialItem(
            "ALL",
            "Logical Operators",
            "The `ALL` operator returns TRUE if a comparison is true for all values in a subquery list.\n\n#### Compare greater than all\n```sql\nSELECT name FROM products WHERE price > ALL (SELECT price FROM products WHERE category = 'Toys');\n```\n\n#### Compare equal to all\n```sql\nSELECT name FROM employees WHERE salary = ALL (SELECT salary FROM employees WHERE dept = 'IT');\n```\n\n### Why is `ALL` Used?\n\n* To compare a column value against every single element in a subquery.\n* To enforce strict checks against an entire dataset.\n* To execute complex value comparisons.",
            "SELECT cols FROM table WHERE col operator ALL (SELECT query);",
            "SELECT name FROM products WHERE price > ALL (SELECT price FROM products WHERE category = 'Toys');"
        ));

        ITEMS.add(new TutorialItem(
            "ANY",
            "Logical Operators",
            "The `ANY` (or SOME) operator returns TRUE if a comparison is true for at least one value in a subquery list.\n\n#### Compare greater than any\n```sql\nSELECT name FROM products WHERE price > ANY (SELECT price FROM products WHERE category = 'Books');\n```\n\n#### Compare equal to any\n```sql\nSELECT name FROM users WHERE id = ANY (SELECT user_id FROM orders);\n```\n\n### Why is `ANY` Used?\n\n* To compare a column value against any element of a subquery list.\n* To execute flexible conditional checks against subqueries.\n* To simplify complex OR lookups.",
            "SELECT cols FROM table WHERE col operator ANY (SELECT query);",
            "SELECT name FROM products WHERE price > ANY (SELECT price FROM products WHERE category = 'Books');"
        ));

        ITEMS.add(new TutorialItem(
            "BETWEEN",
            "Logical Operators",
            "The `BETWEEN` operator selects values within a specified range (inclusive of the start and end values).\n\n#### Filter numbers in range\n```sql\nSELECT * FROM users WHERE age BETWEEN 18 AND 25;\n```\n\n#### Filter dates in range\n```sql\nSELECT * FROM orders WHERE order_date BETWEEN '2025-01-01' AND '2025-12-31';\n```\n\n#### Filter alphabetical ranges\n```sql\nSELECT * FROM products WHERE name BETWEEN 'A' AND 'M';\n```\n\n### Why is `BETWEEN` Used?\n\n* To filter numbers within a range (e.g., BETWEEN 10 AND 50).\n* To filter records within date boundaries.\n* To write cleaner ranges compared to >= and <= combinations.",
            "SELECT cols FROM table WHERE col BETWEEN val1 AND val2;",
            "SELECT * FROM users WHERE age BETWEEN 18 AND 25;"
        ));

        ITEMS.add(new TutorialItem(
            "EXISTS",
            "Logical Operators",
            "The `EXISTS` operator tests for the existence of records in a subquery. It returns TRUE if the subquery yields at least one row.\n\n#### Filter matching relationships\n```sql\nSELECT name FROM users u WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id);\n```\n\n#### Exclude matching relationships\n```sql\nSELECT name FROM users u WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id);\n```\n\n### Why is `EXISTS` Used?\n\n* To run fast conditional checks (stops searching as soon as the first match is found).\n* To filter rows based on relationships in other tables.\n* To optimize queries compared to IN subqueries.",
            "SELECT cols FROM table WHERE EXISTS (SELECT query);",
            "SELECT name FROM users u WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id);"
        ));

        ITEMS.add(new TutorialItem(
            "IN",
            "Logical Operators",
            "The `IN` operator checks if a column value matches any value in a specified list or subquery.\n\n#### Match list of strings\n```sql\nSELECT * FROM users WHERE country IN ('USA', 'Canada', 'Mexico');\n```\n\n#### Match list of integers\n```sql\nSELECT * FROM tasks WHERE status_code IN (1, 3, 5);\n```\n\n#### Match subquery results\n```sql\nSELECT * FROM users WHERE id IN (SELECT DISTINCT user_id FROM orders);\n```\n\n### Why is `IN` Used?\n\n* To replace multiple OR conditions with a clean list.\n* To check values against a static list (e.g., IN ('admin', 'mod')).\n* To match values returned by subqueries.",
            "SELECT cols FROM table WHERE col IN (val1, val2);",
            "SELECT * FROM users WHERE country IN ('USA', 'Canada', 'Mexico');"
        ));

        ITEMS.add(new TutorialItem(
            "LIKE",
            "Logical Operators",
            "The `LIKE` operator is used in a WHERE clause to perform wildcard pattern matching on text strings.\n\n#### Match pattern starting with string\n```sql\nSELECT * FROM users WHERE email LIKE '%@gmail.com';\n```\n\n#### Match pattern containing string\n```sql\nSELECT * FROM products WHERE name LIKE '%shoes%';\n```\n\n#### Match exact length pattern\n```sql\nSELECT * FROM users WHERE name LIKE 'J_n_';\n```\n\n### Why is `LIKE` Used?\n\n* To search for partial text matches.\n* To find words starting with (`prefix%`), ending with (`%suffix`), or containing (`%val%`) specific patterns.\n* To run flexible text filters.",
            "SELECT cols FROM table WHERE col LIKE 'pattern';",
            "SELECT * FROM users WHERE email LIKE '%@gmail.com';"
        ));

        ITEMS.add(new TutorialItem(
            "IS NULL",
            "Logical Operators",
            "The `IS NULL` operator checks if a column has a NULL (empty/missing) value.\n\n#### Find rows with empty columns\n```sql\nSELECT * FROM users WHERE phone_number IS NULL;\n```\n\n#### Verify data consistency\n```sql\nSELECT * FROM employees WHERE manager_id IS NULL;\n```\n\n### Why is `IS NULL` Used?\n\n* To check for missing or optional details.\n* To audit empty fields in records.\n* To isolate incomplete transactions.",
            "SELECT cols FROM table WHERE col IS NULL;",
            "SELECT * FROM users WHERE phone_number IS NULL;"
        ));

        ITEMS.add(new TutorialItem(
            "IS NOT NULL",
            "Logical Operators",
            "The `IS NOT NULL` operator checks if a column has a value that is not NULL (contains valid data).\n\n#### Filter out empty records\n```sql\nSELECT * FROM users WHERE email IS NOT NULL;\n```\n\n#### Audit completed inputs\n```sql\nSELECT * FROM orders WHERE ship_date IS NOT NULL;\n```\n\n### Why is `IS NOT NULL` Used?\n\n* To filter out rows with missing data.\n* To ensure columns contain valid values before calculations.\n* To isolate complete records.",
            "SELECT cols FROM table WHERE col IS NOT NULL;",
            "SELECT * FROM users WHERE email IS NOT NULL;"
        ));

        ITEMS.add(new TutorialItem(
            "NULL",
            "Logical Operators",
            "The `NULL` keyword represents missing, unknown, or unassigned data values in a database table.\n\n#### Define column allowing nulls\n```sql\nCREATE TABLE users (id INT, phone TEXT DEFAULT NULL);\n```\n\n#### Update value to null\n```sql\nUPDATE users SET status = NULL WHERE id = 5;\n```\n\n### Why is `NULL` Used?\n\n* To represent empty or optional inputs.\n* To pad unmatched rows in outer joins.\n* To test for missing values.",
            "column_name datatype DEFAULT NULL;",
            "UPDATE users SET status = NULL WHERE id = 5;"
        ));

        ITEMS.add(new TutorialItem(
            "TRUE",
            "Logical Operators",
            "The `TRUE` keyword represents the boolean logical true state. In SQLite, boolean values are represented as integers (1 for true).\n\n#### Simple boolean check\n```sql\nSELECT * FROM users WHERE is_active = TRUE;\n```\n\n#### Constant check\n```sql\nSELECT * FROM users WHERE TRUE;\n```\n\n### Why is `TRUE` Used?\n\n* To test condition statuses in logic blocks.\n* To store binary states in tables.\n* To write constant filters (e.g., WHERE 1=1).",
            "SELECT columns FROM table WHERE TRUE; OR col = TRUE;",
            "SELECT * FROM users WHERE is_active = TRUE;"
        ));

        ITEMS.add(new TutorialItem(
            "FALSE",
            "Logical Operators",
            "The `FALSE` keyword represents the boolean logical false state. In SQLite, boolean values are represented as integers (0 for false).\n\n#### Simple boolean check\n```sql\nSELECT * FROM users WHERE is_active = FALSE;\n```\n\n#### Reset boolean flag\n```sql\nUPDATE users SET is_active = FALSE WHERE id = 10;\n```\n\n### Why is `FALSE` Used?\n\n* To test negative boolean states.\n* To reset binary flags.\n* To write conditional logic rules.",
            "SELECT columns FROM table WHERE FALSE; OR col = FALSE;",
            "UPDATE users SET is_active = FALSE WHERE id = 10;"
        ));

        ITEMS.add(new TutorialItem(
            "CASE",
            "Logical Operators",
            "The `CASE` statement acts like an if-then-else block, allowing conditional output values within SELECT queries.\n\n#### Simple CASE expression\n```sql\nSELECT name, CASE WHEN score >= 50 THEN 'Pass' ELSE 'Fail' END AS result FROM students;\n```\n\n#### Multiple CASE conditions\n```sql\nSELECT name, CASE \n  WHEN age < 13 THEN 'Child'\n  WHEN age BETWEEN 13 AND 19 THEN 'Teenager'\n  ELSE 'Adult'\nEND AS age_group FROM users;\n```\n\n### Why is `CASE` Used?\n\n* To map raw values to readable labels (e.g., mapping 1 to 'Yes' and 0 to 'No').\n* To perform inline calculations based on rules.\n* To clean up reporting structures.",
            "CASE WHEN cond THEN val1 ELSE val2 END;",
            "SELECT name, CASE WHEN score >= 50 THEN 'Pass' ELSE 'Fail' END AS result FROM students;"
        ));

        ITEMS.add(new TutorialItem(
            "IF",
            "Logical Operators",
            "The `IF` command is used in conditional functions (`IF(cond, val1, val2)`) or in procedural PL-SQL blocks to control execution flow.\n\n#### Inline IF condition\n```sql\nSELECT name, IF(age >= 18, 'Adult', 'Minor') FROM users;\n```\n\n#### Procedural IF (Standard SQL)\n```sql\nIF (SELECT count(*) FROM users) > 100 \nBEGIN\n  PRINT 'Limit reached';\nEND;\n```\n\n### Why is `IF` Used?\n\n* To run simple inline conditional replacements.\n* To branch execution in stored procedures.\n* To manage script pathways.",
            "IF condition THEN statements; END IF; OR IF(cond, true_val, false_val);",
            "SELECT name, IF(age >= 18, 'Adult', 'Minor') FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "ELSE",
            "Logical Operators",
            "The `ELSE` keyword provides a fallback option when all preceding conditions in a CASE or IF block evaluate to false.\n\n#### CASE with ELSE\n```sql\nSELECT name, CASE WHEN age >= 18 THEN 'Yes' ELSE 'No' END FROM users;\n```\n\n#### Procedural ELSE\n```sql\nIF val > 10 \n  PRINT 'Greater';\nELSE \n  PRINT 'Smaller';\n```\n\n### Why is `ELSE` Used?\n\n* To catch all remaining values in logic branches.\n* To prevent queries from returning NULL when no condition matches.\n* To enforce default logic.",
            "CASE WHEN cond THEN val1 ELSE val2 END;",
            "SELECT name, CASE WHEN age >= 18 THEN 'Yes' ELSE 'No' END FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "WHILE",
            "Logical Operators",
            "The `WHILE` keyword defines a loop inside procedural SQL scripts, repeating instructions while a condition remains true.\n\n#### Simple loop iteration\n```sql\nDECLARE i INT DEFAULT 1; WHILE i <= 10 LOOP SET i = i + 1; END LOOP;\n```\n\n#### Iterate cursor sets\n```sql\nWHILE (SELECT COUNT(*) FROM queue) > 0 LOOP \n  DELETE FROM queue WHERE id = (SELECT MIN(id) FROM queue);\nEND LOOP;\n```\n\n### Why is `WHILE` Used?\n\n* To execute iterative processes in stored procedures.\n* To loop through cursor rows.\n* To generate test datasets.",
            "WHILE condition LOOP statements; END LOOP;",
            "DECLARE i INT DEFAULT 1; WHILE i <= 10 LOOP SET i = i + 1; END LOOP;"
        ));

        ITEMS.add(new TutorialItem(
            "LOOP",
            "Logical Operators",
            "The `LOOP` keyword establishes an execution loop block in procedural SQL, repeating actions until explicitly terminated.\n\n#### Simple loop definition\n```sql\nLOOP INSERT INTO logs VALUES ('running'); IF count > 10 THEN LEAVE; END IF; END LOOP;\n```\n\n#### Loop with exit condition\n```sql\nLOOP \n  UPDATE queue SET status = 'done' WHERE id = (SELECT MIN(id) FROM queue);\n  IF NOT EXISTS (SELECT 1 FROM queue WHERE status = 'pending') THEN\n    EXIT;\n  END IF;\nEND LOOP;\n```\n\n### Why is `LOOP` Used?\n\n* To loop through records and datasets.\n* To perform complex procedural logic iterations.\n* To run processes until a limit is reached.",
            "LOOP statements; END LOOP;",
            "LOOP INSERT INTO logs VALUES ('running'); IF count > 10 THEN LEAVE; END IF; END LOOP;"
        ));

    }

    private static void initFunctions() {
        ITEMS.add(new TutorialItem(
            "COUNT",
            "Functions",
            "The `COUNT()` function returns the number of rows that match a specified criteria.\n\n#### Count all rows\n```sql\nSELECT COUNT(*) FROM users;\n```\n\n#### Count non-null entries\n```sql\nSELECT COUNT(phone_number) FROM users;\n```\n\n#### Count distinct entries\n```sql\nSELECT COUNT(DISTINCT country) FROM users;\n```\n\n### Why is `COUNT` Used?\n\n* To calculate total row counts (`COUNT(*)`).\n* To count only non-NULL values in a specific column.\n* To count unique values when combined with DISTINCT.",
            "SELECT COUNT(column_name) FROM table;",
            "SELECT COUNT(*) FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "SUM",
            "Functions",
            "The `SUM()` function returns the total sum of a numeric column.\n\n#### Sum column values\n```sql\nSELECT SUM(amount) FROM orders;\n```\n\n#### Sum aggregated groups\n```sql\nSELECT dept, SUM(salary) FROM employees GROUP BY dept;\n```\n\n### Why is `SUM` Used?\n\n* To calculate cumulative figures (e.g., total sales amount).\n* To run ledger sums in financial databases.\n* To aggregate numeric values grouped by category.",
            "SELECT SUM(column_name) FROM table;",
            "SELECT SUM(amount) FROM orders;"
        ));

        ITEMS.add(new TutorialItem(
            "AVG",
            "Functions",
            "The `AVG()` function calculates and returns the average value of a numeric column.\n\n#### Calculate average value\n```sql\nSELECT AVG(salary) FROM employees;\n```\n\n#### Average with grouped categories\n```sql\nSELECT category, AVG(price) FROM products GROUP BY category;\n```\n\n### Why is `AVG` Used?\n\n* To find average benchmarks (e.g., average product price).\n* To run statistical analytics on datasets.\n* To extract performance averages.",
            "SELECT AVG(column_name) FROM table;",
            "SELECT AVG(salary) FROM employees;"
        ));

        ITEMS.add(new TutorialItem(
            "MIN",
            "Functions",
            "The `MIN()` function returns the smallest (minimum) value in the selected column.\n\n#### Find minimum number\n```sql\nSELECT MIN(price) FROM products;\n```\n\n#### Find minimum date\n```sql\nSELECT MIN(order_date) FROM orders;\n```\n\n### Why is `MIN` Used?\n\n* To find the cheapest price, lowest score, or earliest date.\n* To extract minimal values in dataset limits.\n* To identify starting values in lists.",
            "SELECT MIN(column_name) FROM table;",
            "SELECT MIN(price) FROM products;"
        ));

        ITEMS.add(new TutorialItem(
            "MAX",
            "Functions",
            "The `MAX()` function returns the largest (maximum) value in the selected column.\n\n#### Find maximum number\n```sql\nSELECT MAX(price) FROM products;\n```\n\n#### Find maximum date\n```sql\nSELECT MAX(order_date) FROM orders;\n```\n\n### Why is `MAX` Used?\n\n* To find the highest price, maximum score, or most recent date.\n* To extract ceiling parameters in lists.\n* To identify peaks in records.",
            "SELECT MAX(column_name) FROM table;",
            "SELECT MAX(price) FROM products;"
        ));

        ITEMS.add(new TutorialItem(
            "ROUND",
            "Functions",
            "The `ROUND()` function rounds a numeric value to a specified number of decimal places.\n\n#### Round decimal places\n```sql\nSELECT ROUND(AVG(price), 2) FROM products;\n```\n\n#### Round to nearest integer\n```sql\nSELECT ROUND(4.57, 0) AS value;\n```\n\n### Why is `ROUND` Used?\n\n* To format floating-point values for presentation.\n* To round financial results to two decimal places.\n* To simplify decimal precision.",
            "ROUND(numeric_value, decimal_places);",
            "SELECT ROUND(AVG(price), 2) FROM products;"
        ));

        ITEMS.add(new TutorialItem(
            "NOW",
            "Functions",
            "The `NOW()` function returns the current date and time of the database server. (Note: SQLite uses `datetime('now')` instead).\n\n#### Get current timestamp\n```sql\nSELECT NOW();\n```\n\n#### Relative date arithmetic\n```sql\nSELECT * FROM orders WHERE order_date >= NOW() - INTERVAL 1 DAY;\n```\n\n### Why is `NOW` Used?\n\n* To record the exact time a row was created or modified.\n* To filter records based on current relative time boundaries.\n* To track active session lifetimes.",
            "SELECT NOW();",
            "SELECT * FROM orders WHERE order_date >= NOW() - INTERVAL 1 DAY;"
        ));

        ITEMS.add(new TutorialItem(
            "UPPER",
            "Functions",
            "The `UPPER()` function converts a string to uppercase characters.\n\n#### Convert string to uppercase\n```sql\nSELECT UPPER(name) FROM users;\n```\n\n#### Case-insensitive filter check\n```sql\nSELECT * FROM users WHERE UPPER(country) = 'USA';\n```\n\n### Why is `UPPER` Used?\n\n* To standardize string formats for user queries.\n* To perform case-insensitive comparisons.\n* To clean text outputs.",
            "UPPER(string_column);",
            "SELECT UPPER(name) FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "LOWER",
            "Functions",
            "The `LOWER()` function converts a string to lowercase characters.\n\n#### Convert string to lowercase\n```sql\nSELECT LOWER(email) FROM users;\n```\n\n#### Case-insensitive check\n```sql\nSELECT * FROM users WHERE LOWER(name) = 'alice';\n```\n\n### Why is `LOWER` Used?\n\n* To normalize text values before comparisons.\n* To build lowercase system IDs or emails.\n* To standardize casing.",
            "LOWER(string_column);",
            "SELECT LOWER(email) FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "LENGTH",
            "Functions",
            "The `LENGTH()` function returns the number of characters in a text string.\n\n#### Check password length\n```sql\nSELECT name FROM users WHERE LENGTH(password) < 8;\n```\n\n#### Check string length in selection\n```sql\nSELECT name, LENGTH(name) AS length FROM users;\n```\n\n### Why is `LENGTH` Used?\n\n* To check string length limitations (e.g., verifying password lengths).\n* To run analytical checks on data formats.\n* To identify empty strings.",
            "LENGTH(string_column);",
            "SELECT name FROM users WHERE LENGTH(password) < 8;"
        ));

        ITEMS.add(new TutorialItem(
            "SUBSTRING",
            "Functions",
            "The `SUBSTRING()` (or `SUBSTR()`) function extracts a specified portion of a text string.\n\n#### Extract specific string portion\n```sql\nSELECT SUBSTRING(phone, 1, 3) AS area_code FROM users;\n```\n\n#### Extract string to the end\n```sql\nSELECT SUBSTRING('Database', 5) AS part;\n```\n\n### Why is `SUBSTRING` Used?\n\n* To split text fields into segments (e.g., extracting area codes).\n* To extract prefixes or substrings from fields.\n* To format raw text outputs.",
            "SUBSTRING(string, start_position, length);",
            "SELECT SUBSTRING(phone, 1, 3) AS area_code FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "REPLACE",
            "Functions",
            "The `REPLACE()` string function replaces all occurrences of a specified substring within a string with a new substring.\n\n#### Replace space with dash\n```sql\nSELECT REPLACE(phone, '-', ' ') FROM users;\n```\n\n#### Correct domain name\n```sql\nSELECT REPLACE(email, '@old.com', '@new.com') FROM users;\n```\n\n### Why is `REPLACE` Used?\n\n* To correct text typos in bulk.\n* To strip out unwanted characters (e.g., replacing spaces with dashes).\n* To update URLs or domains.",
            "REPLACE(string, search_for, replace_with);",
            "SELECT REPLACE(phone, '-', ' ') FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "CAST",
            "Functions",
            "The `CAST()` function converts a value from one datatype to another (e.g., converting a string of digits to an integer).\n\n#### Cast string to integer\n```sql\nSELECT CAST('123' AS INTEGER) AS num_val;\n```\n\n#### Cast decimal to integer\n```sql\nSELECT CAST(3.57 AS INTEGER) AS num_val;\n```\n\n#### Cast number to text\n```sql\nSELECT CAST(100 AS TEXT) AS text_val;\n```\n\n### Why is `CAST` Used?\n\n* To resolve datatype mismatches during joins or operations.\n* To format string numbers to calculate mathematical sums.\n* To force numeric columns into text formats.",
            "CAST(value AS datatype);",
            "SELECT CAST('123' AS INTEGER) AS num_val;"
        ));

        ITEMS.add(new TutorialItem(
            "CONCAT",
            "Functions",
            "The `CONCAT()` function joins two or more strings together. (Note: SQLite uses the `||` operator for concatenation).\n\n#### Concatenate multiple strings (Standard SQL)\n```sql\nSELECT CONCAT(first_name, ' ', last_name) AS full_name FROM employees;\n```\n\n#### Concatenate strings in SQLite\n```sql\nSELECT first_name || ' ' || last_name AS full_name FROM employees;\n```\n\n### Why is `CONCAT` Used?\n\n* To merge columns for presentation (e.g., combining first and last names).\n* To construct full addresses or descriptions dynamically.\n* To append text tags to numeric outputs.",
            "CONCAT(str1, str2, ...);",
            "SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM employees;"
        ));

    }

    private static void initProgrammability() {
        ITEMS.add(new TutorialItem(
            "PROCEDURE",
            "Programmability",
            "A `PROCEDURE` (Stored Procedure) is a precompiled collection of SQL statements saved in the database for repetitive executions.\n\n#### Create stored procedure\n```sql\nCREATE PROCEDURE get_users AS \nBEGIN \n  SELECT * FROM users; \nEND;\n```\n\n#### Execute stored procedure\n```sql\nEXEC get_users;\n```\n\n### Why is `PROCEDURE` Used?\n\n* To execute complex business operations on the database server.\n* To optimize performance by executing precompiled paths.\n* To isolate access to table schemas by granting execute rights.",
            "CREATE PROCEDURE proc_name AS BEGIN statements; END;",
            "CREATE PROCEDURE get_users AS BEGIN SELECT * FROM users; END;"
        ));

        ITEMS.add(new TutorialItem(
            "FUNCTION",
            "Programmability",
            "A `FUNCTION` is a database object that executes a set of SQL statements and returns a single value or table.\n\n#### Create scalar function\n```sql\nCREATE FUNCTION get_tax(price REAL) RETURNS REAL AS \nBEGIN \n  RETURN price * 0.15; \nEND;\n```\n\n#### Call scalar function in SELECT\n```sql\nSELECT name, get_tax(price) AS tax FROM products;\n```\n\n### Why is `FUNCTION` Used?\n\n* To encapsulate calculations for reuse inside queries.\n* To format values dynamically in SELECT outputs.\n* To write custom math or string helpers.",
            "CREATE FUNCTION func_name(param) RETURNS type AS BEGIN RETURN val; END;",
            "CREATE FUNCTION get_tax(price REAL) RETURNS REAL AS BEGIN RETURN price * 0.15; END;"
        ));

        ITEMS.add(new TutorialItem(
            "DECLARE",
            "Programmability",
            "The `DECLARE` keyword initializes local variables, cursors, or temporary types within procedural PL-SQL scripts.\n\n#### Declare local variable\n```sql\nDECLARE @total_count INT; SET @total_count = (SELECT COUNT(*) FROM users);\n```\n\n#### Declare cursor\n```sql\nDECLARE user_cursor CURSOR FOR SELECT id, name FROM users;\n```\n\n### Why is `DECLARE` Used?\n\n* To hold intermediate values during calculations.\n* To configure cursor pathways in loops.\n* To allocate memory variables.",
            "DECLARE @var_name datatype;",
            "DECLARE @total_count INT; SET @total_count = (SELECT COUNT(*) FROM users);"
        ));

        ITEMS.add(new TutorialItem(
            "CURSOR",
            "Programmability",
            "A `CURSOR` is a database object used to loop through and process query result rows one-by-one.\n\n#### Declare and open cursor\n```sql\nDECLARE user_cursor CURSOR FOR SELECT id, name FROM users;\nOPEN user_cursor;\nFETCH NEXT FROM user_cursor INTO @id, @name;\nCLOSE user_cursor;\n```\n\n#### Loop cursor elements\n```sql\nWHILE @@FETCH_STATUS = 0\nBEGIN\n  -- do operations\n  FETCH NEXT FROM user_cursor INTO @id, @name;\nEND;\n```\n\n### Why is `CURSOR` Used?\n\n* To run row-by-row procedural processing when batch updates aren't possible.\n* To parse records and calculate custom parameters sequentially.\n* To run complex multi-step validations.",
            "DECLARE cursor_name CURSOR FOR SELECT query;",
            "DECLARE user_cursor CURSOR FOR SELECT id, name FROM users;"
        ));

        ITEMS.add(new TutorialItem(
            "EXEC",
            "Programmability",
            "The `EXEC` (or `EXECUTE`) command executes a stored procedure, dynamic SQL script, or string command.\n\n#### Execute procedure\n```sql\nEXEC get_users;\n```\n\n#### Execute procedure with parameters\n```sql\nEXEC get_user_by_id 5;\n```\n\n### Why is `EXEC` Used?\n\n* To launch preconfigured stored procedures.\n* To compile and run dynamic SQL scripts generated at runtime.\n* To run database administrative tasks.",
            "EXEC procedure_name [params];",
            "EXEC get_users;"
        ));

        ITEMS.add(new TutorialItem(
            "TRIGGER",
            "Programmability",
            "A `TRIGGER` is a database object that automatically executes a set of SQL statements when a specified event (INSERT, UPDATE, DELETE) occurs on a table.\n\n#### Create trigger after insert\n```sql\nCREATE TRIGGER log_user AFTER INSERT ON users \nBEGIN \n  INSERT INTO logs VALUES ('New user: ' || new.name); \nEND;\n```\n\n#### Create trigger before delete\n```sql\nCREATE TRIGGER protect_admin BEFORE DELETE ON users \nBEGIN \n  SELECT CASE WHEN old.role = 'admin' THEN RAISE(FAIL, 'Cannot delete admin') END; \nEND;\n```\n\n### Why is `TRIGGER` Used?\n\n* To log changes to audit tables automatically.\n* To synchronize data across tables.\n* To enforce complex business rules directly in the database.",
            "CREATE TRIGGER name AFTER|BEFORE INSERT|UPDATE|DELETE ON table BEGIN actions; END;",
            "CREATE TRIGGER log_user AFTER INSERT ON users BEGIN INSERT INTO logs VALUES ('New user: ' || new.name); END;"
        ));

        ITEMS.add(new TutorialItem(
            "BEFORE",
            "Programmability",
            "The `BEFORE` keyword defines the trigger timing to execute its statements prior to applying the data changes to the table.\n\n#### Before insert validation check\n```sql\nCREATE TRIGGER check_age BEFORE INSERT ON users \nBEGIN \n  SELECT CASE WHEN new.age < 18 THEN RAISE(ABORT, 'Underage') END; \nEND;\n```\n\n#### Before update validation check\n```sql\nCREATE TRIGGER check_salary BEFORE UPDATE ON employees \nBEGIN \n  SELECT CASE WHEN new.salary < old.salary THEN RAISE(ABORT, 'Salary decrease forbidden') END; \nEND;\n```\n\n### Why is `BEFORE` Used?\n\n* To validate or modify incoming values before they are saved.\n* To prevent invalid inserts from continuing.\n* To enforce strict checks.",
            "CREATE TRIGGER name BEFORE INSERT|UPDATE|DELETE ON table BEGIN actions; END;",
            "CREATE TRIGGER check_age BEFORE INSERT ON users BEGIN SELECT CASE WHEN new.age < 18 THEN RAISE(ABORT, 'Underage') END; END;"
        ));

        ITEMS.add(new TutorialItem(
            "AFTER",
            "Programmability",
            "The `AFTER` keyword defines the trigger timing to execute its statements after the data changes have been applied to the table.\n\n#### After insert logging\n```sql\nCREATE TRIGGER log_new_order AFTER INSERT ON orders \nBEGIN \n  INSERT INTO order_logs (msg) VALUES ('Order placed ID: ' || new.id); \nEND;\n```\n\n#### After delete cleanup\n```sql\nCREATE TRIGGER clean_profile AFTER DELETE ON users \nBEGIN \n  DELETE FROM profiles WHERE user_id = old.id; \nEND;\n```\n\n### Why is `AFTER` Used?\n\n* To safely log database audits.\n* To update totals in related tables after data is verified.\n* To trigger notifications or downstream actions.",
            "CREATE TRIGGER name AFTER INSERT|UPDATE|DELETE ON table BEGIN actions; END;",
            "CREATE TRIGGER log_del AFTER DELETE ON products BEGIN INSERT INTO audit VALUES ('Deleted: ' || old.sku); END;"
        ));

        ITEMS.add(new TutorialItem(
            "COMMIT",
            "TCL",
            "The `COMMIT` command saves all changes made during the current transaction permanently to the database.\n\n#### Commit standard transaction\n```sql\nBEGIN TRANSACTION; \nINSERT INTO users (name) VALUES ('Joe'); \nCOMMIT;\n```\n\n#### Commit nested changes\n```sql\nBEGIN TRANSACTION;\nUPDATE inventory SET qty = qty - 1 WHERE id = 5;\nUPDATE orders SET status = 'completed' WHERE id = 12;\nCOMMIT;\n```\n\n### Why is `COMMIT` Used?\n\n* To finalize safe state changes.\n* To release database transaction locks.\n* To ensure ACID durability.",
            "COMMIT;",
            "BEGIN TRANSACTION; INSERT INTO users (name) VALUES ('Joe'); COMMIT;"
        ));

        ITEMS.add(new TutorialItem(
            "ROLLBACK",
            "TCL",
            "The `ROLLBACK` command reverts the database to its previous stable state, undoing all changes made since the transaction began.\n\n#### Rollback transaction\n```sql\nBEGIN TRANSACTION; \nUPDATE accounts SET balance = balance - 100; \nROLLBACK;\n```\n\n#### Conditional rollback\n```sql\nBEGIN TRANSACTION;\nUPDATE users SET name = 'Invalid';\n-- detect error and rollback\nROLLBACK;\n```\n\n### Why is `ROLLBACK` Used?\n\n* To undo incomplete operations if an error occurs.\n* To protect database integrity during crashes.\n* To restore stable states.",
            "ROLLBACK;",
            "BEGIN TRANSACTION; UPDATE accounts SET balance = balance - 100; ROLLBACK;"
        ));

        ITEMS.add(new TutorialItem(
            "SAVEPOINT",
            "TCL",
            "The `SAVEPOINT` command establishes a checkpoint inside a transaction, allowing you to rollback parts of the transaction without undoing all changes.\n\n#### Revert to savepoint\n```sql\nBEGIN TRANSACTION;\nINSERT INTO logs VALUES ('Step 1 completed');\nSAVEPOINT step1;\nUPDATE users SET name = 'Bob';\nROLLBACK TO step1;\nCOMMIT;\n```\n\n### Why is `SAVEPOINT` Used?\n\n* To divide long transactional scripts into logical steps.\n* To selectively revert errors without rolling back the entire transaction.\n* To manage complex multi-step processes.",
            "SAVEPOINT savepoint_name;",
            "SAVEPOINT step1; UPDATE users SET name = 'Bob'; ROLLBACK TO step1;"
        ));

        ITEMS.add(new TutorialItem(
            "GRANT",
            "DCL",
            "The `GRANT` command assigns database access permissions (SELECT, INSERT, UPDATE, execute) to specific database users or roles.\n\n#### Grant read access\n```sql\nGRANT SELECT ON employees TO HR_department;\n```\n\n#### Grant execute access on procedure\n```sql\nGRANT EXECUTE ON get_users TO web_app;\n```\n\n### Why is `GRANT` Used?\n\n* To configure granular database access security.\n* To restrict administrative commands to specific users.\n* To enforce access controls.",
            "GRANT PRIVILEGE ON object TO user;",
            "GRANT SELECT, INSERT ON employees TO HR_department;"
        ));

        ITEMS.add(new TutorialItem(
            "REVOKE",
            "DCL",
            "The `REVOKE` command withdraws previously assigned database permissions from a user or role.\n\n#### Revoke write access\n```sql\nREVOKE INSERT, UPDATE ON employees FROM contract_workers;\n```\n\n#### Revoke execution rights\n```sql\nREVOKE EXECUTE ON get_users FROM public;\n```\n\n### Why is `REVOKE` Used?\n\n* To remove database permissions when user roles change.\n* To lock down security access.\n* To enforce strict compliance audits.",
            "REVOKE PRIVILEGE ON object FROM user;",
            "REVOKE INSERT, UPDATE ON employees FROM contract_workers;"
        ));

        ITEMS.add(new TutorialItem(
            "CREATE USER",
            "DCL",
            "The `CREATE USER` statement creates a new database user account with custom credentials.\n\n#### Create local database user\n```sql\nCREATE USER 'analyst'@'localhost' IDENTIFIED BY 'secure_password';\n```\n\n#### Create user with password\n```sql\nCREATE USER 'developer' IDENTIFIED BY 'devpass';\n```\n\n### Why is `CREATE USER` Used?\n\n* To assign unique credentials to databases.\n* To isolate database sessions between different clients.\n* To track audit actions by account name.",
            "CREATE USER 'username'@'host' IDENTIFIED BY 'password';",
            "CREATE USER 'analyst'@'localhost' IDENTIFIED BY 'secure_password';"
        ));

        ITEMS.add(new TutorialItem(
            "SHOW DATABASES",
            "Metadata Queries",
            "The `SHOW DATABASES` command lists all active databases available on the database server.\n\n#### Show available databases\n```sql\nSHOW DATABASES;\n```\n\n### Why is `SHOW DATABASES` Used?\n\n* To inspect available databases on a server.\n* To verify database configurations.\n* To locate tables across database environments.",
            "SHOW DATABASES;",
            "SHOW DATABASES;"
        ));

        ITEMS.add(new TutorialItem(
            "SHOW TABLES",
            "Metadata Queries",
            "The `SHOW TABLES` command lists all available tables and views in the current active database.\n\n#### Show database tables\n```sql\nSHOW TABLES;\n```\n\n### Why is `SHOW TABLES` Used?\n\n* To inspect database contents.\n* To verify table setups.\n* To find table names in active databases.",
            "SHOW TABLES;",
            "SHOW TABLES;"
        ));

        ITEMS.add(new TutorialItem(
            "DESCRIBE",
            "Metadata Queries",
            "The `DESCRIBE` (or `DESC`) command displays structural metadata details (columns, datatypes, constraints) of a specified table.\n\n#### Describe table metadata\n```sql\nDESCRIBE employees;\n```\n\n### Why is `DESCRIBE` Used?\n\n* To check column structures and datatypes.\n* To review constraint configurations.\n* To verify table schema metadata.",
            "DESCRIBE table_name;",
            "DESCRIBE employees;"
        ));

        ITEMS.add(new TutorialItem(
            "EXPLAIN",
            "Metadata Queries",
            "The `EXPLAIN` (or `EXPLAIN QUERY PLAN`) statement displays the execution plan the database engine will use to run a query.\n\n#### Explain simple select\n```sql\nEXPLAIN SELECT * FROM users;\n```\n\n#### Explain query plan in SQLite\n```sql\nEXPLAIN QUERY PLAN SELECT * FROM users WHERE email = 'test@example.com';\n```\n\n### Why is `EXPLAIN` Used?\n\n* To profile query performance bottlenecks.\n* To verify that indexes are being utilized correctly.\n* To optimize database read operations.",
            "EXPLAIN SELECT query;",
            "EXPLAIN QUERY PLAN SELECT * FROM users WHERE email = 'test@example.com';"
        ));

        ITEMS.add(new TutorialItem(
            "BACKUP",
            "Administration",
            "The `BACKUP` statement creates a backup copy of a database to protect against data loss.\n\n#### Backup to file path\n```sql\nBACKUP DATABASE shop_db TO DISK = 'C:\\\\backups\\\\shop.bak';\n```\n\n### Why is `BACKUP` Used?\n\n* To schedule disaster recovery procedures.\n* To transfer database snapshots across environments.\n* To secure database states.",
            "BACKUP DATABASE name TO DISK = 'filepath';",
            "BACKUP DATABASE shop_db TO DISK = 'C:\\\\backups\\\\shop.bak';"
        ));

        ITEMS.add(new TutorialItem(
            "BY",
            "Data Query (DQL)",
            "The `BY` keyword acts as a sorting or grouping modifier used in conjunction with `GROUP BY` and `ORDER BY` clauses.\n\n#### BY in order clause\n```sql\nSELECT * FROM users ORDER BY age ASC;\n```\n\n#### BY in group clause\n```sql\nSELECT country, COUNT(*) FROM users GROUP BY country;\n```\n\n### Why is `BY` Used?\n\n* To direct columns for database grouping actions.\n* To specify fields for results sorting.\n* To complete query requirements.",
            "GROUP BY column; OR ORDER BY column;",
            "SELECT * FROM users ORDER BY age ASC;"
        ));

        ITEMS.add(new TutorialItem(
            "USE",
            "Database Context",
            "The `USE` command selects a specific database to make it the active database for all subsequent queries.\n\n#### Switch to database\n```sql\nUSE college_db;\n```\n\n### Why is `USE` Used?\n\n* To switch between databases easily.\n* To set the query execution context.\n* To avoid writing database prefixes.",
            "USE database_name;",
            "USE college_db;"
        ));

        ITEMS.add(new TutorialItem(
            "AUTO_INCREMENT",
            "Constraints",
            "The `AUTO_INCREMENT` constraint automatically generates a unique sequential integer ID when a new row is inserted.\n\n#### Table with auto increment\n```sql\nCREATE TABLE logs (id INTEGER PRIMARY KEY AUTOINCREMENT, note TEXT);\n```\n\n### Why is `AUTO_INCREMENT` Used?\n\n* To create sequential primary keys automatically.\n* To avoid generating IDs manually inside client code.\n* To track registration orders.",
            "column_name INTEGER PRIMARY KEY AUTOINCREMENT; OR AUTO_INCREMENT;",
            "CREATE TABLE logs (id INTEGER PRIMARY KEY AUTOINCREMENT, note TEXT);"
        ));

        ITEMS.add(new TutorialItem(
            "CHAR",
            "Data Types",
            "The `CHAR` datatype defines a fixed-length string, padding short inputs with spaces.\n\n#### Fixed length character column\n```sql\nCREATE TABLE states (code CHAR(2), name TEXT);\n```\n\n### Why is `CHAR` Used?\n\n* To store fixed-size values (e.g., country codes or state flags).\n* To optimize storage speed when data size is strictly constant.\n* To standardise string layouts.",
            "column_name CHAR(size);",
            "CREATE TABLE states (code CHAR(2), name TEXT);"
        ));

        ITEMS.add(new TutorialItem(
            "VARCHAR",
            "Data Types",
            "The `VARCHAR` datatype defines a variable-length string with a maximum limit.\n\n#### Variable character column\n```sql\nCREATE TABLE profiles (username VARCHAR(50), bio VARCHAR(255));\n```\n\n### Why is `VARCHAR` Used?\n\n* To store names, descriptions, or comments of varying length.\n* To optimize space by only storing actual character counts.\n* To enforce string length limits.",
            "column_name VARCHAR(max_size);",
            "CREATE TABLE profiles (username VARCHAR(50), bio VARCHAR(255));"
        ));

        ITEMS.add(new TutorialItem(
            "TEXT",
            "Data Types",
            "The `TEXT` datatype defines a large character string field with virtually unlimited size.\n\n#### Large text column\n```sql\nCREATE TABLE articles (id INT, content TEXT);\n```\n\n### Why is `TEXT` Used?\n\n* To store large text fields (e.g., articles, JSON strings, or HTML content).\n* To manage large, unstructured text datasets.\n* To bypass VARCHAR length limits.",
            "column_name TEXT;",
            "CREATE TABLE articles (id INT, content TEXT);"
        ));

        ITEMS.add(new TutorialItem(
            "INT",
            "Data Types",
            "The `INT` datatype defines standard whole integer numbers.\n\n#### Integer columns\n```sql\nCREATE TABLE scores (user_id INT, points INT);\n```\n\n### Why is `INT` Used?\n\n* To store counts, ages, counters, and IDs.\n* To run fast mathematical comparisons.\n* To store numeric fields.",
            "column_name INT;",
            "CREATE TABLE scores (user_id INT, points INT);"
        ));

        ITEMS.add(new TutorialItem(
            "INTEGER",
            "Data Types",
            "The `INTEGER` datatype is the full keyword representing whole numbers. In SQLite, this is the only integer type.\n\n#### Integer primary key\n```sql\nCREATE TABLE counters (id INTEGER PRIMARY KEY, count INTEGER);\n```\n\n### Why is `INTEGER` Used?\n\n* To declare primary keys with auto-increment behavior.\n* To define standard integer fields.\n* To store whole numbers.",
            "column_name INTEGER;",
            "CREATE TABLE counters (id INTEGER PRIMARY KEY, count INTEGER);"
        ));

        ITEMS.add(new TutorialItem(
            "FLOAT",
            "Data Types",
            "The `FLOAT` datatype defines single-precision floating point numbers.\n\n#### Float column\n```sql\nCREATE TABLE data (id INT, value FLOAT);\n```\n\n### Why is `FLOAT` Used?\n\n* To store decimal numbers (e.g., weights or percentages).\n* To execute floating-point math calculations.\n* To store high-precision scientific values.",
            "column_name FLOAT;",
            "CREATE TABLE data (id INT, value FLOAT);"
        ));

        ITEMS.add(new TutorialItem(
            "DOUBLE",
            "Data Types",
            "The `DOUBLE` datatype defines double-precision floating point numbers, providing more decimals than FLOAT.\n\n#### Double column\n```sql\nCREATE TABLE points (latitude DOUBLE, longitude DOUBLE);\n```\n\n### Why is `DOUBLE` Used?\n\n* To store high-precision decimal measurements.\n* To prevent decimal rounding errors.\n* To run calculations requiring maximum accuracy.",
            "column_name DOUBLE;",
            "CREATE TABLE points (latitude DOUBLE, longitude DOUBLE);"
        ));

        ITEMS.add(new TutorialItem(
            "BOOLEAN",
            "Data Types",
            "The `BOOLEAN` datatype defines logic values (TRUE or FALSE). SQLite stores booleans as 1 and 0 internally.\n\n#### Boolean column\n```sql\nCREATE TABLE members (id INT, is_premium BOOLEAN);\n```\n\n### Why is `BOOLEAN` Used?\n\n* To record binary conditions (e.g., is_deleted, has_paid).\n* To evaluate logical conditions in script pathways.\n* To save storage space.",
            "column_name BOOLEAN;",
            "CREATE TABLE members (id INT, is_premium BOOLEAN);"
        ));

        ITEMS.add(new TutorialItem(
            "DATE",
            "Data Types",
            "The `DATE` datatype stores calendar date values in the standard format `YYYY-MM-DD`.\n\n#### Date column\n```sql\nCREATE TABLE profiles (id INT, date_of_birth DATE);\n```\n\n### Why is `DATE` Used?\n\n* To track birthdays, join dates, or events.\n* To query datasets chronologically.\n* To filter data based on calendar days.",
            "column_name DATE;",
            "CREATE TABLE profiles (id INT, date_of_birth DATE);"
        ));

        ITEMS.add(new TutorialItem(
            "TIME",
            "Data Types",
            "The `TIME` datatype stores time of day values in the standard format `HH:MM:SS`.\n\n#### Time column\n```sql\nCREATE TABLE schedule (task TEXT, start_time TIME);\n```\n\n### Why is `TIME` Used?\n\n* To track appointment times, daily schedules, or trigger events.\n* To run calculations on durations.\n* To record relative times of day.",
            "column_name TIME;",
            "CREATE TABLE schedule (task TEXT, start_time TIME);"
        ));

        ITEMS.add(new TutorialItem(
            "DATETIME",
            "Data Types",
            "The `DATETIME` datatype stores combined calendar dates and times of day (`YYYY-MM-DD HH:MM:SS`).\n\n#### Datetime column\n```sql\nCREATE TABLE transactions (id INT, created_at DATETIME);\n```\n\n### Why is `DATETIME` Used?\n\n* To log timestamps (e.g., when transactions occurred).\n* To measure precise intervals between events.\n* To track system activities.",
            "column_name DATETIME;",
            "CREATE TABLE transactions (id INT, created_at DATETIME);"
        ));

        ITEMS.add(new TutorialItem(
            "TIMESTAMP",
            "Data Types",
            "The `TIMESTAMP` datatype stores combined date/time values, often updated automatically by the database on row modifications.\n\n#### Timestamp column\n```sql\nCREATE TABLE logs (msg TEXT, ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP);\n```\n\n### Why is `TIMESTAMP` Used?\n\n* To track historical updates to table rows automatically.\n* To log high-precision events.\n* To manage synchronization across databases.",
            "column_name TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;",
            "CREATE TABLE logs (msg TEXT, ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"
        ));

        ITEMS.add(new TutorialItem(
            "EXCEPT",
            "Data Query (DQL)",
            "The `EXCEPT` operator returns rows from the first query that do not exist in the second query's results.\n\n#### Set difference\n```sql\nSELECT id FROM users EXCEPT SELECT user_id FROM orders;\n```\n\n### Why is `EXCEPT` Used?\n\n* To calculate set difference.\n* To find items in a table that do not have active relationships.\n* To compare table differences.",
            "SELECT col FROM t1 EXCEPT SELECT col FROM t2;",
            "SELECT id FROM users EXCEPT SELECT user_id FROM orders;"
        ));

        ITEMS.add(new TutorialItem(
            "INTERSECT",
            "Data Query (DQL)",
            "The `INTERSECT` operator returns rows that exist in the output of both queries.\n\n#### Set intersection\n```sql\nSELECT email FROM subscribers INTERSECT SELECT email FROM customers;\n```\n\n### Why is `INTERSECT` Used?\n\n* To calculate set intersection.\n* To identify overlapping data elements.\n* To run strict comparison match checks.",
            "SELECT col FROM t1 INTERSECT SELECT col FROM t2;",
            "SELECT email FROM subscribers INTERSECT SELECT email FROM customers;"
        ));

    }
}
