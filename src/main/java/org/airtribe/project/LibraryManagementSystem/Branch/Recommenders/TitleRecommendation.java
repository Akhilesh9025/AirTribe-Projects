package org.airtribe.project.LibraryManagementSystem.Branch.Recommenders;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Branch.Inventory;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;

import java.util.HashSet;
import java.util.Set;

public class TitleRecommendation implements RecommendationSystem {

    private static final Set<String> ignoreKeywords = new HashSet<>();

    static{
        ignoreKeywords.add("the");
        ignoreKeywords.add("a");
        ignoreKeywords.add("an");
        ignoreKeywords.add("of");
        ignoreKeywords.add("and");
        ignoreKeywords.add("in");
        ignoreKeywords.add("on");
        ignoreKeywords.add("at");
        ignoreKeywords.add("to");
        ignoreKeywords.add("for");
        ignoreKeywords.add("with");
        ignoreKeywords.add("by");
        ignoreKeywords.add("from");
        ignoreKeywords.add("as");
        ignoreKeywords.add("is");
        ignoreKeywords.add("are");
        ignoreKeywords.add("was");
        ignoreKeywords.add("were");
        ignoreKeywords.add("has");
        ignoreKeywords.add("have");
        ignoreKeywords.add("had");
        ignoreKeywords.add("will");
        ignoreKeywords.add("would");
    }

    @Override
    public void displayRecommendations(Patron patron, Inventory braninventory) {
        logger.info("\tDisplaying recommendations by Title keyword");
        suggestBooksByTitle(patron, braninventory);
    }

    private void suggestBooksByTitle(Patron patron, Inventory braninventory) {
        Set<String> preferredTitles = patron.getPreferredTitles();
        Set<Book> books = braninventory.getAvailableBooks();

        boolean found = false;

        Set<String> preferredKeywords = new HashSet<>();

        for (String title : preferredTitles) {
            String[] keywords = title.split(" ");
            for (String keyword : keywords) {
                if(ignoreKeywords.contains(keyword.toLowerCase())){
                    continue;
                }
                preferredKeywords.add(keyword.toLowerCase());
            }
        }

        for (Book book : books) {
            String[] titleWords = book.getTitle().split(" ");
            for (String titleWord : titleWords) {
                if (preferredKeywords.contains(titleWord.toLowerCase()) && patron.hasBorrowedTheBook(book) && patron.hasAlreadyReadTheBook(book)) {
                    found = true;
                    logger.info('\t' + "\tBook Title: {} Author: {} Genre: {}", book.getTitle(), book.getAuthor(), book.getGenre());
                    break;
                }
            }
        }

        if (!found) {
            logger.info('\t' + "No books found by preferred title keywords");
        }
    }
}
