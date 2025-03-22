package org.airtribe.project.LibraryManagementSystem.Branch.Recommenders;

import org.airtribe.project.LibraryManagementSystem.Branch.Inventory;
import org.airtribe.project.LibraryManagementSystem.Patron.Patron;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface RecommendationSystem {
    static final Logger logger = LogManager.getLogger(RecommendationSystem.class);
    void displayRecommendations(Patron patron, Inventory inventory);
}
