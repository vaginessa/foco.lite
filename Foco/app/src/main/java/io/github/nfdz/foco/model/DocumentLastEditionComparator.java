package io.github.nfdz.foco.model;

import java.util.Comparator;

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
                return (time1 < time2) ? -1 : 1;
            }
        }
    }

}
