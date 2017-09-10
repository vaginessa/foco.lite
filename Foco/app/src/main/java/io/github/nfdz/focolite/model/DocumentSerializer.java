package io.github.nfdz.focolite.model;

import timber.log.Timber;

/**
 * Document String serializer static class.
 */
public class DocumentSerializer {

    /** Readable friendly metadata separator (to be placed before and after metadata) */
    private static final String METADATA_SEPARATOR = "--- DOCUMENT METADATA ---";

    /** Number of lines that uses metadata */
    private static final int METADATA_LINES = 6;

    /**
     * Inner static class Document implementation.
     */
    public static class DocumentImpl implements Document {

        public String name;
        public long lastEditionTime = Document.NULL_LAST_EDITION_TIME;
        public String text = Document.NULL_TEXT;
        public boolean favorite = false;

        @Override
        public String getName() {
            return name;
        }
        @Override
        public long getLastEditionTimeMillis() {
            return lastEditionTime;
        }
        @Override
        public String getText() {
            return text;
        }
        @Override
        public boolean isFavorite() {
            return favorite;
        }
        /**
         * This field is not stored in exported documents (it depends of each database).
         * @return Document.NULL_ID
         */
        @Override
        public long getId() {
            return Document.NULL_ID;
        }
    }

    /**
     * Deserializes given document.
     * @param serializedDoc
     * @return Document (it is not have words field)
     * @throws SerializationException
     */
    public static Document deserializeDocument(String serializedDoc) throws SerializationException {

        DocumentImpl result  = new DocumentImpl();

        String[] lines = serializedDoc.split("\\r?\\n", -1);
        if (lines == null || lines.length < METADATA_LINES) {
            Timber.e("There is an error with metadata lines in serialized document: " + serializedDoc);
            throw new SerializationException();
        }

        try {
            result.name = lines[1];
            result.lastEditionTime = Long.parseLong(lines[2]);
            result.favorite = Boolean.parseBoolean(lines[3]);
            result.text = joinLines(lines, METADATA_LINES);

            return result;
        } catch (NumberFormatException ex) {
            Timber.e(ex, "There is an error parsing metadata in serialized document");
            throw new SerializationException();
        }
    }

    private static String joinLines(String[] lines, int start) {
        StringBuilder bld = new StringBuilder();
        for (int i = start; i < lines.length; i++) {
            bld.append(lines[i]);
            if (i + 1 < lines.length) {
                bld.append('\n');
            }
        }
        return bld.toString();
    }

    /**
     * Serializes given document.
     * @param document
     * @return String
     */
    public static String serializeDocument(Document document) {
        StringBuilder bld = new StringBuilder();

        // metadata
        bld.append(METADATA_SEPARATOR).append('\n');

        bld.append(document.getName()).append('\n');
        bld.append(Long.toString(document.getLastEditionTimeMillis())).append('\n');
        bld.append(Boolean.toString(document.isFavorite())).append('\n');

        bld.append(METADATA_SEPARATOR).append('\n').append('\n');

        // text
        bld.append(document.getText());

        return bld.toString();
    }
}