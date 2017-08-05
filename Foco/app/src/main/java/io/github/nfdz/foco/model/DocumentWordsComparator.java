package io.github.nfdz.foco.model;

import java.util.Comparator;

/**
 * Document comparator implementation with number of words criteria (more number of words first).
 * This meets the favorite criteria (as the first ordering criteria)
 * and name comparator (when number of words are equals).
 */
public class DocumentWordsComparator implements Comparator<Document> {

    private final Comparator<Document> mDefaultComparator = new DocumentNameComparator();

    @Override
    public int compare(Document doc1, Document doc2) {
        if (doc1.isFavorite() && !doc2.isFavorite()) {
            return -1;
        } else if (!doc1.isFavorite() && doc2.isFavorite()) {
            return 1;
        } else {
            int words1 = doc1.getWords();
            int words2 = doc2.getWords();
            if (words1 == words2) {
                return mDefaultComparator.compare(doc1, doc2);
            } else {
                return (words1 > words2) ? -1 : 1;
            }
        }
    }

}
