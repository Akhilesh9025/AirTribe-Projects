package org.airtribe.project.LibraryManagementSystem.Branch;

import org.airtribe.project.LibraryManagementSystem.Patron.Patron;
import org.airtribe.project.LibraryManagementSystem.Book.Book;

import java.util.Date;
import java.util.UUID;

public class Token {
    private final UUID tokenId;
    private final Patron patron;
    private final Book book;
    private final Date issueDate;

    protected Token(Patron patron, Book book) {
        this.tokenId = UUID.randomUUID();
        this.patron = patron;
        this.book = book;
        this.issueDate = new Date();
    }

    public Book getBook() {
        return book;
    }

    public Patron getPatron() {
        return patron;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public Date getIssueDate() {
        return issueDate;
    }
}
