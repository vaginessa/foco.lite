package io.github.nfdz.foco.model;

import io.github.nfdz.foco.R;
import timber.log.Timber;

public class DocumentSerializer {

    private static final String METADATA_SEPARATOR = "--- DOCUMENT METADATA ---";
    private static final int METADATA_LINES = 9;

    private static class DocumentImpl implements Document {

        public String name;
        public long workingTime = Document.NULL_WORKING_TIME;
        public long lastEditionTime = Document.NULL_LAST_EDITION_TIME;
        public String text = Document.NULL_TEXT;
        public boolean favorite = false;
        public int coverColor = Document.NULL_COVER_COLOR;
        public String coverImage = Document.NULL_COVER_IMAGE;

        /**
         * This field is not stored in exported documents (it depends of each database).
         * @return Document.NULL_ID
         */
        @Override
        public long getId() {
            return Document.NULL_ID;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public long getWorkingTimeMillis() {
            return workingTime;
        }
        @Override
        public long getLastEditionTimeMillis() {
            return lastEditionTime;
        }
        @Override
        public String getText() {
            return text;
        }

        /**
         * This field is not stored in exported documents (it could be computed with text).
         * @return Document.NULL_WORDS
         */
        @Override
        public int getWords() {
            return Document.NULL_WORDS;
        }
        @Override
        public boolean isFavorite() {
            return favorite;
        }
        @Override
        public int getCoverColor() {
            return coverColor;
        }
        @Override
        public String getCoverImage() {
            return coverImage;
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
            throw new SerializationException(R.string.deserialize_error);
        }

        try {
            result.name = lines[1];
            result.workingTime = Long.parseLong(lines[2]);
            result.lastEditionTime = Long.parseLong(lines[3]);
            result.favorite = Boolean.parseBoolean(lines[4]);
            result.coverColor = Integer.parseInt(lines[5]);
            result.coverImage = lines[6];
            result.text = joinLines(lines, 9);

            return result;
        } catch (NumberFormatException ex) {
            Timber.e(ex, "There is an error parsing metadata in serialized document");
            throw new SerializationException(R.string.deserialize_error);
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
        bld.append(Long.toString(document.getWorkingTimeMillis())).append('\n');
        bld.append(Long.toString(document.getLastEditionTimeMillis())).append('\n');
        bld.append(Boolean.toString(document.isFavorite())).append('\n');
        bld.append(Integer.toString(document.getCoverColor())).append('\n');
        bld.append(document.getCoverImage()).append('\n');

        bld.append(METADATA_SEPARATOR).append('\n').append('\n');

        // text
        bld.append(document.getText());

        return bld.toString();
    }
}