# 🎬 BookMyShow Ticketing Platform Assignment

---

## 📌 Part 1: Database Design (P1)

### 🎯 Objective
Design a normalized relational database schema for a movie ticketing platform.

---

## 🧱 Entity-Relationship Design

### ✅ Entities & Attributes

| Entity    | Attributes |
|-----------|------------|
| **Theatre** | `theatre_id` (PK), `name`, `location` |
| **Movie**   | `movie_id` (PK), `title`, `language`, `certification`, `format` |
| **Screen**  | `screen_id` (PK), `theatre_id` (FK), `screen_name` |
| **Show**    | `show_id` (PK), `movie_id` (FK), `screen_id` (FK), `show_date`, `show_time`, `show_type` |

---

## 🧮 Normal Forms (NFs) Explanation

### 🔹 First Normal Form (1NF)
- All columns have atomic values.
- No repeating groups or multivalued fields.
- Each table has a primary key.

> ✅ Example: `show_time` in `show` table stores a single value, not a list.

---

### 🔹 Second Normal Form (2NF)
- No partial dependencies.
- All non-key attributes fully depend on the entire (single-column) primary key.

> ✅ Example: `screen_name` depends fully on `screen_id` in the `screen` table.

---

### 🔹 Third Normal Form (3NF)
- No transitive dependencies.
- Non-key attributes are only dependent on the primary key.

> ✅ Example: `show_type` in `show` table depends on `show_id`, not on `movie_id`.

---

### 🔹 Boyce-Codd Normal Form (BCNF)
- Every determinant is a candidate key.

> ✅ Example: The combination `(movie_id, screen_id, show_date, show_time)` uniquely determines a show — hence each table follows BCNF.

---

## 🛠️ SQL DDL + Sample Data

### 🎯 Schema DDL

- #### Create Theatre table
```sql
CREATE TABLE theatre (
    theatre_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);
```

- #### Create Movie table
```sql
CREATE TABLE movie (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    language VARCHAR(50),
    certification VARCHAR(10),
    format VARCHAR(10)
);
```

- #### Create Screen table
```sql
CREATE TABLE screen (
    screen_id INT AUTO_INCREMENT PRIMARY KEY,
    theatre_id INT NOT NULL,
    screen_name VARCHAR(50) NOT NULL,
    FOREIGN KEY (theatre_id) REFERENCES theatre(theatre_id)
);
```

- #### Create Show table
```sql
CREATE TABLE show (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    screen_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    show_type VARCHAR(50),
    FOREIGN KEY (movie_id) REFERENCES movie(movie_id),
    FOREIGN KEY (screen_id) REFERENCES screen(screen_id),
    CHECK (show_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)) 
);
```
- ##### 🔄 Optional Trigger (Alternative to CHECK in SHOW table)
```sql
DELIMITER //

CREATE TRIGGER show_date_limit
BEFORE INSERT ON show
FOR EACH ROW
BEGIN
IF NEW.show_date < CURDATE() OR NEW.show_date > CURDATE() + INTERVAL 6 DAY THEN
SIGNAL SQLSTATE '45000'
SET MESSAGE_TEXT = 'Show date must be within the next 7 days including today';
END IF;
END;
//

DELIMITER ;
```

### 📥 Sample Data Insertions
- #### Insert Theatres
```sql
INSERT INTO theatre (name, location) VALUES
('PVR: Nexus', 'Mumbai'),
('INOX: Mall', 'Delhi');
```

- #### Insert Movies
```sql
INSERT INTO movie (title, language, certification, format) VALUES
('Avengers: Endgame', 'English', 'UA', '3D'),
('Inception', 'English', 'UA', '2D'),
('Dangal', 'Hindi', 'U', '2D');
```
- #### Insert Screens
```sql
INSERT INTO screen (theatre_id, screen_name) VALUES
(1, 'Screen 1'),
(1, 'Screen 2'),
(2, 'Screen A');
```
- #### Insert Shows (All within 7-day range from CURDATE)
```sql
INSERT INTO show (movie_id, screen_id, show_date, show_time, show_type) VALUES
(1, 1, CURDATE(), '10:00:00', 'Regular'),
(1, 1, CURDATE(), '14:00:00', '3D'),
(2, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', 'IMAX'),
(3, 3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '20:00:00', 'Regular'),
(2, 1, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '22:00:00', '2D');
```
### 📊 Sample Data Output

````SELECT * FROM theatre;````

| theatre_id | name             | location |
|------------|------------------|----------|
|          1 | PVR: Nexus       | Mumbai   |
|          2 | INOX: Andheri    | Mumbai   |


````SELECT * FROM movie;````

| movie_id | title                      | language | certification | format |
|----------|----------------------------|----------|---------------|--------|
|        1 | Avengers: Endgame          | English  | UA            | 2D     |
|        2 | Spider-Man: No Way Home    | English  | UA            | 3D     |
|        3 | Dangal                     | Hindi    | U             | 2D     |

````SELECT * FROM screen;````

| screen_id | theatre_id | screen_name |
|-----------|------------|-------------|
|         1 |          1 | Screen 1    |
|         2 |          1 | Screen 2    |
|         3 |          2 | Main Hall   |

````SELECT * FROM show ORDER BY show_date, show_time;````

| show_id | movie_id | screen_id | show_date  | show_time | show_type |
|---------|----------|-----------|------------|-----------|-----------|
|       1 |        1 |         1 | 2025-05-26 | 10:00:00  | Regular   |
|       2 |        1 |         1 | 2025-05-26 | 14:00:00  | Regular   |
|       3 |        2 |         2 | 2025-05-27 | 12:00:00  | 3D        |
|       4 |        2 |         2 | 2025-05-28 | 16:00:00  | 3D        |
|       5 |        3 |         3 | 2025-05-29 | 18:00:00  | Regular   |
|       6 |        3 |         3 | 2025-05-30 | 20:00:00  | Regular   |


## 📌 Part 2: SQL Query (P2)
### 🎯 Requirement
Write a query to list down all the shows on a given date at a given theatre along with their respective show timings.

### 🔎 SQL Query
```sql
SELECT 
    t.name AS theatre_name,
    m.title AS movie_title,
    s.show_date,
    s.show_time,
    scr.screen_name,
    s.show_type
FROM show s
JOIN movie m ON s.movie_id = m.movie_id
JOIN screen scr ON s.screen_id = scr.screen_id
JOIN theatre t ON scr.theatre_id = t.theatre_id
WHERE s.show_date = '2025-05-26'
  AND t.name = 'PVR: Nexus'
ORDER BY s.show_time;
```

### 🔎 Stored Procedure (Alternate approach)
- #### Stored Procedure to Get Shows by Date and Theatre
```sql
DELIMITER $$

CREATE PROCEDURE GetShowsByDateAndTheatre (
    IN in_theatre_name VARCHAR(100),
    IN in_show_date DATE
)
BEGIN
    SELECT 
        t.name AS theatre_name,
        m.title AS movie_title,
        s.show_date,
        s.show_time,
        scr.screen_name,
        s.show_type
    FROM show s
    JOIN movie m ON s.movie_id = m.movie_id
    JOIN screen scr ON s.screen_id = scr.screen_id
    JOIN theatre t ON scr.theatre_id = t.theatre_id
    WHERE s.show_date = in_show_date
      AND t.name = in_theatre_name
    ORDER BY s.show_time;
END $$

DELIMITER ;

```
- #### Calling Stored Procedure
```sql
CALL GetShowsByDateAndTheatre('PVR: Nexus', '2025-05-26');
```

### 📊 Sample Output

| theatre_name | movie_title          | show_date  | show_time | screen_name | show_type |
|--------------|----------------------|------------|-----------|-------------|-----------|
| PVR: Nexus   | Avengers: Endgame    | 2025-05-26 | 10:00:00  | Screen 1    | Regular   |
| PVR: Nexus   | Avengers: Endgame    | 2025-05-26 | 14:00:00  | Screen 1    | Regular   |



