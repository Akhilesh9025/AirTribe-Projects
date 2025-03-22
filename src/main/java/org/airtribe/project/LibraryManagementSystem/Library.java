package org.airtribe.project.LibraryManagementSystem;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Book.BookBuilder;
import org.airtribe.project.LibraryManagementSystem.Branch.Branch;
import org.airtribe.project.LibraryManagementSystem.Branch.Recommenders.AuthorRecommendation;
import org.airtribe.project.LibraryManagementSystem.Branch.Recommenders.GenreRecommendation;
import org.airtribe.project.LibraryManagementSystem.Branch.Recommenders.TitleRecommendation;
import org.airtribe.project.LibraryManagementSystem.Branch.Token;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Library {
    private static final Logger logger = LogManager.getLogger(Library.class);

    public static void main(String[] args) {

        Branch branch1 = new Branch(54321, "Central Branch", "Vijayawada");
        Branch branch2 = new Branch(54322, "Overseas Branch", "Houston");
        Branch branch3 = new Branch(54323, "Regional Branch", "Ranchi");

        Book book1 = new BookBuilder()
                .setIsbn(1234567890)
                .setTitle("Clean Code")
                .setAuthor("Akhilesh")
                .setPublicationYear(2005)
                .setGenre("Education")
                .build();

        Book book2 = new BookBuilder()
                .setIsbn(1234567891)
                .setTitle("Data Structures and Algorithms")
                .setAuthor("Lokesh")
                .setPublicationYear(2007)
                .setGenre("Educatiom")
                .build();

        Book book3 = new BookBuilder()
                .setIsbn(1234567892)
                .setTitle("Design Patterns")
                .setAuthor("Rahul")
                .setPublicationYear(2009)
                .setGenre("Education")
                .build();

        Book book4 = new BookBuilder()
                .setIsbn(1234567893)
                .setTitle("Java Programming")
                .setAuthor("Sachin")
                .setPublicationYear(2011)
                .setGenre("Education")
                .build();

        Book book5 = new BookBuilder()
                .setIsbn(1234567894)
                .setTitle("System Design")
                .setAuthor("Akhilesh")
                .setPublicationYear(2013)
                .setGenre("Education")
                .build();

        Book book6 = new BookBuilder()
                .setIsbn(1234567895)
                .setTitle("Data Science")
                .setAuthor("Lokesh")
                .setGenre("Education")
                .build();

        Book book7 = new BookBuilder()
                .setIsbn(1234567896)
                .setTitle("Machine Learning")
                .setAuthor("Rahul")
                .setPublicationYear(2015)
                .build();


        branch1.addBook(book1, 2);
        branch1.addBook(book2, 8);
        branch1.addBook(book3, 3);
        branch1.addBook(book4, 7);
        branch1.removeBook(book3);
        branch1.updateQuantity(book1, 1);

        branch2.addBook(book2, 5);
        branch2.addBook(book3, 7);
        branch2.addBook(book4, 4);
        branch2.addBook(book5, 6);
        branch2.removeBook(book4);
        branch2.updateQuantity(book3, 10);

        branch3.addBook(book1, 3);
        branch3.addBook(book3, 5);
        branch3.addBook(book5, 7);

        Patron patron1 = new Patron(1, "Akhil", "Vijayawada", "9999999999");
        Patron patron2 = new Patron(2, "Loki", "Houston", "9999999998");
        Patron patron3 = new Patron(3, "Dhoni", "Ranchi", "9999999997");

        Token t1 = branch1.checkoutBook(book1, patron1);
        Token t2 = branch1.checkoutBook(book1, patron2);
        Token t3 = branch1.checkoutBook(book1, patron3);

        branch1.returnBook(t1);

        Token t4 = branch1.checkoutBook(book1, patron1);

        branch1.returnBook(t2);
        branch1.returnBook(t4);

        branch1.displayBorrowedDetails();

        branch1.displayInventory();

        branch1.transferBook(book1, branch2, 3);

        branch1.displayInventory();

        branch1.searchBook("Data Structures and Algorithms");

        branch1.recommendBooks(patron1, new AuthorRecommendation());
        branch3.recommendBooks(patron1, new GenreRecommendation());
        branch1.recommendBooks(patron1, new TitleRecommendation());

    }
}