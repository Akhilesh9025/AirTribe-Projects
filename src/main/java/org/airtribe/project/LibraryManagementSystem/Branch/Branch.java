package org.airtribe.project.LibraryManagementSystem.Branch;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Branch.Recommenders.RecommendationSystem;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Branch {
    private static final Logger logger = LogManager.getLogger(Branch.class);

    private final int branchId;
    private String branchName;
    private String branchAddress;
    private final Inventory inventory;

    public Branch(int branchId, String branchName, String branchAddress) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.inventory = new Inventory();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void updateBranchName(String branchName) {
        this.branchName = branchName;
    }

    public void updateBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public int getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void displayInventory() {
        logger.info( "Display Inventory - Branch: {}\tBranch ID: {}", branchName, branchId);
        inventory.displayAllBooks();
    }

    public void addBook(Book book, int quantity) {
        if(inventory.containsBook(book)) {
            inventory.updateQuantity(book, inventory.getQuantity(book) + quantity);
            return;
        }
        inventory.addBook(book, quantity);
    }

    public void removeBook(Book book) {
        if(inventory.containsBook(book)) {
            inventory.removeBook(book);
        }
    }

    public void searchBook(String parameter) {
        logger.info("Search Book - Branch: {}\tBranch ID: {}", branchName, branchId);
        boolean isFound = (inventory.searchBookByTitle(parameter) || inventory.searchBookByAuthor(parameter) || inventory.searchBookByIsbn(parameter));
        if (!isFound) {
            logger.info('\t' + " Book not found with property : {}\n", parameter);
        }
    }

    public void updateQuantity(Book book, int quantity) {
        inventory.updateQuantity(book, quantity);
    }

    public Token checkoutBook(Book book, Patron patron) {
        if(!inventory.isAvailable(book) ) {
            logger.warn("Failed Transaction: Book not available" + '\n');
            inventory.addObserver(book, patron);
            logger.info("Notification: Patron {} is subscribed for book: {}", patron.getPatronName(), book.getTitle());
            return null;
        }
        if(patron.getBorrowedBooksCount() < 5 && patron.hasBorrowedTheBook(book)) {
            Token token = inventory.issueToken(book, patron);
            patron.borrowBook(book, token);
            inventory.checkoutBook(book);
            inventory.notifyObservers(book, false, this);
            return token;
        }

        logger.warn("Invalid Transaction: {} cannot borrow the book {}" + '\n', patron.getPatronName(), book.getTitle());
        return null;
    }

    public void returnBook(Token token) {
        if(token != null && inventory.hasToken(token)) {
            inventory.returnBook(token);
            token.getPatron().returnBook(token.getBook());
            logger.info("{} Book is returned successfully by {}\n", token.getBook().getTitle(), token.getPatron().getPatronName());
            inventory.removeObserver(token.getBook(), token.getPatron());
            inventory.notifyObservers(token.getBook(), true, this);
            return;
        }
        logger.warn("Token is invalid" + '\n');
    }

    public void recommendBooks(Patron patron, RecommendationSystem recommendationSystem) {
        logger.info("Recommend Books - Branch: {}\tBranch ID: {}", branchName, branchId);
        recommendationSystem.displayRecommendations(patron, inventory);
    }

    public void displayBorrowedDetails(){
        logger.info("Display Borrowed Details - Branch: {}\tBranch ID: {}", branchName, branchId);
        inventory.displayBorrowedBooks();
    }

    public void transferBook(Book book, Branch branch, int quantity) {
        if(inventory.isAvailable(book) && inventory.getQuantity(book) >= quantity) {
            inventory.updateQuantity(book, inventory.getQuantity(book) - quantity);
            branch.addBook(book, quantity);
            return;
        }
        logger.warn("Invalid Transaction: Book not available or quantity requested is more than available" + '\n');
    }

    public void searchBookAvailabilityInOtherBranches(Book book, Branch branch) {
        logger.info("Search Book Availability in Other Branches - Branch: {}\tBranch ID: {}", branchName, branchId);
        branch.searchBook(book.getTitle());
    }

    public void removeObserver(Book book, Patron patron) {
        inventory.removeObserver(book, patron);
    }
}
