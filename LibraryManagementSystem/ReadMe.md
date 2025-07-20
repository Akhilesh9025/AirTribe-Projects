# Library Management System

## Description
This system will help librarians manage books, patrons, and lending processes efficiently.

* **Book** : This is the base template for Book object to access its properties
  * **BookBuilder** : This is the builder class for Book object
* **Patron** : This is the base template for Patron object to access its properties and History
    * **Observer** : This is the interface used to define the template for Patron to observe the Book availability
* **Branch** : This is the base class to create Library Branch object, access inventory and manage lending process
* **Inventory** : This is the class to manage the inventory of books and its reservations in the library
* **Token** : This is the class holding the book lending details
* **Reservations** : This is the class to manage the reservations of books for Patrons in the Inventory
* **RecommendationSystem** : This is the interface used to declare the template for multiple Recommendation strategies (Strategy Design Pattern)
  * **AuthorRecommendation** : This is the class to recommend books to Patron based on their borrow history with specific to the author
  * **GenreRecommendation** : This is the class to recommend books to Patron based on their borrow history with specific to the genre
  * **TitleRecommendation** : This is the class to recommend books to Patron based on their borrow history with specific to the keywords in the title
* **Library** : This is the driver class to create branches and manage the library system as a whole

#### Considerations/Assumptions
* Registered Patron has access to all the branches of the library
* Patrons can borrow books from any branch
* Token is created for each book borrowed by the Patron and is the reference for the lending process

### Driver Usage Instructions
1. Create base Objects for interaction
   - Create Library branches with required parameters (*id, name, address*)
   - Create books of choice (using *BookBuilder*) with required parameters (*isbn, title, author, publication year*)
   - Create patrons of choice with required parameters (*id, name, address, phone*)
2. Add books of desired quantity to the inventory of the library branches created in step 1
3. Borrow and return books by patrons from the required branch
4. Check the inventory of the library branches
5. Update inventory between the branches (transfer books)
6. Check the recommendations for the patrons based on their borrow history
7. Check the reservations for the books in the inventory
8. Check the history of the patrons
9. Test the system with multiple scenarios using the below class diagram

### Class Diagram
![Library Management System Class Diagram](https://github.com/Akhilesh9025/AirTribe-Projects/blob/main/LibraryManagementSystem/ClassDiagram_LibraryManagementSystem.png)



