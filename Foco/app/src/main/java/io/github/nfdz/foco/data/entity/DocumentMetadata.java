package io.github.nfdz.foco.data.entity;

import android.arch.persistence.room.ColumnInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class DocumentMetadata implements Parcelable {

    public DocumentMetadata() {

    }

    @ColumnInfo(name = DocumentEntity.COLUMN_ID)
    public long id;

    @ColumnInfo(name = DocumentEntity.COLUMN_NAME)
    public String name;

    @ColumnInfo(name = DocumentEntity.COLUMN_WORKING_TIME)
    public long workingTime;

    @ColumnInfo(name = DocumentEntity.COLUMN_LAST_EDITION_TIME)
    public long lastEditionTime;

    @ColumnInfo(name = DocumentEntity.COLUMN_WORDS)
    public int words;

    @ColumnInfo(name = DocumentEntity.COLUMN_FAVORITE)
    public boolean isFavorite;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeLong(workingTime);
        dest.writeLong(lastEditionTime);
        dest.writeInt(words);
        dest.writeInt(isFavorite ? 1 : 0);
    }

    private DocumentMetadata(Parcel in) {
        id = in.readLong();
        name = in.readString();
        workingTime = in.readLong();
        lastEditionTime = in.readLong();
        words = in.readInt();
        isFavorite = in.readInt() == 1;
    }

    public static final Parcelable.Creator<DocumentMetadata> CREATOR
            = new Parcelable.Creator<DocumentMetadata>() {
        public DocumentMetadata createFromParcel(Parcel in) {
            return new DocumentMetadata(in);
        }

        public DocumentMetadata[] newArray(int size) {
            return new DocumentMetadata[size];
        }
    };

}
