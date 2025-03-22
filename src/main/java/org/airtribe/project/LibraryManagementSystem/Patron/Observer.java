package org.airtribe.project.LibraryManagementSystem.Patron;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Branch.Branch;

public interface Observer {
    void update(Book book, boolean isAvailable, Branch branch);
}
