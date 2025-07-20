package org.airtribe.project.LibraryManagementSystem.Book;

public class Book {
    private final long isbn;
    private final String title;
    private final String author;
    private final int publicationYear;
    private String genre;

    protected Book(long isbn, String title, String author, int publicationYear, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.genre = genre;
    }

    public long getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getGenre() {
        return genre;
    }

    public void updateGenre(String genre) {
        this.genre = genre;
    }

}
