package org.airtribe.project.LibraryManagementSystem.Branch;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Inventory {
    private static final Logger logger = LogManager.getLogger(Inventory.class);

    private final Map<Book, Integer> booksAvailable = new HashMap<>();
    private final Set<Token> booksBorrowed = new HashSet<>();
    private final Reservations reservations = new Reservations();


    protected void addBook(Book book, int quantity) {
        booksAvailable.put(book, quantity);
    }

    protected void removeBook(Book book) {
        booksAvailable.remove(book);
    }

    protected int getQuantity(Book book) {
        return booksAvailable.get(book);
    }

    protected void updateQuantity(Book book, int quantity) {
        booksAvailable.put(book, quantity);
    }

    protected boolean containsBook(Book book) {
        return booksAvailable.containsKey(book);
    }

    protected boolean isAvailable(Book book) {
        return booksAvailable.get(book) > 0;
    }

    private void displayBook(Book book) {
        logger.info("\tTitle: {}\t Author: {}\t Quantity: {}", book.getTitle(), book.getAuthor(), booksAvailable.get(book));
    }

    protected void displayAllBooks() {
        for(Map.Entry<Book, Integer> entry : booksAvailable.entrySet()) {
            displayBook(entry.getKey());
        }
    }

    protected boolean searchBookByTitle(String title) {
        boolean isFound = false;
        for(Map.Entry<Book, Integer> entry : booksAvailable.entrySet()) {
            if(entry.getKey().getTitle().equalsIgnoreCase(title)) {
                isFound = true;
                displayBook(entry.getKey());
            }
        }
        return isFound;
    }

    protected boolean searchBookByAuthor(String author) {
        boolean isFound = false;
        for(Map.Entry<Book, Integer> entry : booksAvailable.entrySet()) {
            if(entry.getKey().getAuthor().equalsIgnoreCase(author)) {
                isFound = true;
                displayBook(entry.getKey());
            }
        }
        return isFound;
    }

    protected boolean searchBookByIsbn(String isbn) {
        boolean isFound = false;
        for(Map.Entry<Book, Integer> entry : booksAvailable.entrySet()) {
            if(String.valueOf(entry.getKey().getIsbn()).equals(isbn)) {
                isFound = true;
                displayBook(entry.getKey());
            }
        }
        return isFound;
    }

    protected void checkoutBook(Book book) {
        booksAvailable.put(book, booksAvailable.get(book) - 1);
    }

    protected void returnBook(Token token) {
        booksBorrowed.remove(token);
        booksAvailable.put(token.getBook(), booksAvailable.getOrDefault(token.getBook(),0) + 1);
    }

    protected Token issueToken(Book book, Patron patron) {
        Token token = new Token(patron, book);
        booksBorrowed.add(token);
        return token;
    }

    protected boolean hasToken(Token token) {
        return booksBorrowed.contains(token);
    }

    protected void displayBorrowedBooks() {
        for(Token token : booksBorrowed) {
            logger.info("\tBook: {}\t Patron: {}\t Issue Date: {}", token.getBook().getTitle(), token.getPatron().getPatronName(), token.getIssueDate());
        }
    }

    public Set<Book> getAvailableBooks() {
        Set<Book> books = new HashSet<>();

        for (Map.Entry<Book, Integer> entry : booksAvailable.entrySet()) {
            if(entry.getValue()>0) books.add(entry.getKey());
        }
        return books;
    }

    protected void removeObserver(Book book, Patron patron) {
        reservations.removeObserver(book, patron);
    }

    protected void addObserver(Book book, Patron patron) {
        reservations.addObserver(patron, book);
    }

    protected void notifyObservers(Book book, boolean isAvailable, Branch branch) {
        reservations.notifyObservers(book, isAvailable, branch);
    }
}
