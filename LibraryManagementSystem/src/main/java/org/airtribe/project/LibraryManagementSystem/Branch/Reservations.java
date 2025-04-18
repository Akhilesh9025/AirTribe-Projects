package org.airtribe.project.LibraryManagementSystem.Branch;

import org.airtribe.project.LibraryManagementSystem.Book.Book;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;

import java.util.*;

class Reservations {
    private Map<Book, Set<Patron>> reservations = new HashMap<>();
    protected void addObserver(Patron patron, Book book) {
        Set<Patron> patrons = reservations.getOrDefault(book, new HashSet<>());
        patrons.add(patron);
        reservations.put(book, patrons);
    }

    protected void removeObserver(Book book, Patron patron) {
        Set<Patron> patrons = reservations.get(book);
        if(patrons == null) {
            return;
        }
        if (patrons.contains(patron)){
            patrons.remove(patron);
        }
        reservations.put(book, patrons);
    }

    protected void notifyObservers(Book book, boolean isAvailable, Branch branch) {
        Set<Patron> patrons = reservations.get(book);
        if(patrons == null) {
            return;
        }
        for (Patron patron : patrons) {
            patron.update(book, isAvailable, branch);
        }
    }
}
