package org.airtribe.project.LibraryManagementSystem.Patron;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Branch.Branch;
import org.airtribe.project.LibraryManagementSystem.Branch.Token;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Patron implements Observer {
    private static final Logger logger = LogManager.getLogger(Patron.class);

    private final int patronId;
    private String patronName;
    private String patronAddress;
    private String patronMobile;
    private final Map<Book, Token> borrowedBooks = new HashMap<>();
    private final Set<Book> borrowHistory = new HashSet<>();

    public Patron(int patronId, String patronName, String patronAddress, String patronMobile) {
        this.patronId = patronId;
        this.patronName = patronName;
        this.patronAddress = patronAddress;
        this.patronMobile = patronMobile;
    }

    public void updatePatronName(String patronName) {
        this.patronName = patronName;
    }

    public void updatePatronAddress(String patronAddress) {
        this.patronAddress = patronAddress;
    }

    public void updatePatronMobile(String patronMobile) {
        this.patronMobile = patronMobile;
    }

    public int getPatronId() {
        return patronId;
    }

    public String getPatronName() {
        return patronName;
    }

    public String getPatronAddress() {
        return patronAddress;
    }

    public String getPatronMobile() {
        return patronMobile;
    }

    public boolean hasBorrowedTheBook(Book book) {
        return !borrowedBooks.containsKey(book);
    }

    public boolean hasAlreadyReadTheBook(Book book) {
        return !borrowHistory.contains(book);
    }

    public int getBorrowedBooksCount() {
        return borrowedBooks.size();
    }

    public void borrowBook(Book book, Token token) {
        borrowedBooks.put(book, token);
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
        borrowHistory.add(book);
    }

    public Set<String> getPreferredAuthors() {
        Set<String> preferredAuthors = new HashSet<>();
        for (Book book : borrowHistory) {
            preferredAuthors.add(book.getAuthor());
        }
        return preferredAuthors;
    }

    public Set<String> getPreferredGenres() {
        Set<String> preferredGenres = new HashSet<>();
        for (Book book : borrowHistory) {
            preferredGenres.add(book.getGenre());
        }
        return preferredGenres;
    }

    public Set<String> getPreferredTitles() {
        Set<String> preferredTitles = new HashSet<>();
        for (Book book : borrowHistory) {
            preferredTitles.add(book.getTitle());
        }
        return preferredTitles;
    }

    public void unSubscribe(Book book, Branch branch) {
        branch.removeObserver(book, this);
    }

    @Override
    public void update(Book book, boolean isAvailable, Branch branch) {
        if (isAvailable) {
            logger.info("Notification to {}: Book {} is available at Branch: {}", this.getPatronName(), book.getTitle(), branch.getBranchName());
        } else {
            logger.info("Notification to {}: Book {} is not available at Branch: {}", this.getPatronName(), book.getTitle(), branch.getBranchName());
        }
    }
}
