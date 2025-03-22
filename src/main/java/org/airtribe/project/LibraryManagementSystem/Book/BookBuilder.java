package org.airtribe.project.LibraryManagementSystem.Book;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BookBuilder {
    private static final Logger logger = LogManager.getLogger(BookBuilder.class);

    private long isbn;
    private String title;
    private String author;
    private int publicationYear;
    private String genre;

    public BookBuilder setIsbn(long isbn) {
        this.isbn = isbn;
        return this;
    }

    public BookBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public BookBuilder setAuthor(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
        return this;
    }

    public BookBuilder setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public Book build() {
        if(isbn==0 || title == null || author == null || publicationYear == 0) {
            logger.error("Required field(s) are missing" + '\n');
            return null;
        }
        return new Book(isbn, title, author, publicationYear, genre);
    }
}
