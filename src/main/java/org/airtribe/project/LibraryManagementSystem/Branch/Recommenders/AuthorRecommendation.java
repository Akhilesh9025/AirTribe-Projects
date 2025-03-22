package org.airtribe.project.LibraryManagementSystem.Branch.Recommenders;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Branch.Inventory;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;

import java.util.*;


public class AuthorRecommendation implements RecommendationSystem {

    @Override
    public void displayRecommendations(Patron patron, Inventory inventory) {
        logger.info("\tDisplaying recommendations by Author");
        suggestBooksByAuthor(patron, inventory);
    }

    private void suggestBooksByAuthor(Patron patron, Inventory inventory) {
        Set<String> preferredAuthors = patron.getPreferredAuthors();
        Set<Book> books = inventory.getAvailableBooks();

        boolean found = false;

        for (Book book : books) {
            if (preferredAuthors.contains(book.getAuthor()) && patron.hasBorrowedTheBook(book) && patron.hasAlreadyReadTheBook(book)) {
                found = true;
                logger.info('\t' + "\tBook Title: {} Author: {} Genre: {}", book.getTitle(), book.getAuthor(), book.getGenre());
            }
        }

        if (!found) {
            logger.info('\t' + "No books found by preferred authors");
        }
    }
}
