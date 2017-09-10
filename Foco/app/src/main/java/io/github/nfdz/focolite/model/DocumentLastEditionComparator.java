package io.github.nfdz.focolite.model;

import java.util.Comparator;

/**
 * Document comparator implementation with last edition time criteria (last edition time first).
 * This meets the favorite criteria (as the first ordering criteria)
 * and name comparator (when times are equals).
 */
public class DocumentLastEditionComparator implements Comparator<Document> {

    private final Comparator<Document> mDefaultComparator = new DocumentNameComparator();

    @Override
    public int compare(Document doc1, Document doc2) {
        if (doc1.isFavorite() && !doc2.isFavorite()) {
            return -1;
        } else if (!doc1.isFavorite() && doc2.isFavorite()) {
            return 1;
        } else {
            long time1 = doc1.getLastEditionTimeMillis();
            long time2 = doc2.getLastEditionTimeMillis();
            if (time1 == time2) {
                return mDefaultComparator.compare(doc1, doc2);
            } else {
                return (time1 > time2) ? -1 : 1;
            }
        }
    }

}
