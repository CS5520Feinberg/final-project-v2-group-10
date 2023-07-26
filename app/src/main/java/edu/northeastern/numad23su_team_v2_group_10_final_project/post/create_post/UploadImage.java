package edu.northeastern.numad23su_team_v2_group_10_final_project.post.create_post;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UploadImage implements Parcelable {
    public String imageUri;
    public String imagePath;
    public Boolean isSelected;
    public Boolean isInEditMode;
    public Integer width;

    public UploadImage(String imageUri, String path, int width) {
        this.imageUri = imageUri;
        this.imagePath = path;
        this.isSelected = false;
        this.isInEditMode = false;
        this.width = width;
    }

    public static final Creator<UploadImage> CREATOR = new Creator<UploadImage>() {
        @Override
        public UploadImage createFromParcel(Parcel in) {
            return new UploadImage(in);
        }

        @Override
        public UploadImage[] newArray(int size) {
            return new UploadImage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected UploadImage(Parcel in) {
        imageUri = in.readString();
        imagePath = in.readString();
        isSelected = in.readByte() == 1;
        isInEditMode = in.readByte() == 1;
        width = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(imageUri);
        parcel.writeString(imagePath);
        parcel.writeByte((byte) (isSelected == null ? 0 : isSelected ? 1 : 2));
        parcel.writeByte((byte) (isInEditMode == null ? 0 : isInEditMode ? 1 : 2));
        parcel.writeInt(width);
    }
}
