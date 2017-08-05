package io.github.nfdz.foco.model;

import java.util.Comparator;

/**
 * Document comparator implementation with name/title criteria.
 * This meets the favorite criteria (as the first ordering criteria).
 */
public class DocumentNameComparator implements Comparator<Document> {
    @Override
    public int compare(Document doc1, Document doc2) {
        if (doc1.isFavorite() && !doc2.isFavorite()) {
            return -1;
        } else if (!doc1.isFavorite() && doc2.isFavorite()) {
            return 1;
        } else {
            return doc1.getName().compareTo(doc2.getName());
        }
    }
}
