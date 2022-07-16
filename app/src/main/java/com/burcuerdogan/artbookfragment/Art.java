package com.burcuerdogan.artbookfragment;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Art {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String artname;

    @Nullable
    @ColumnInfo(name = "artistname")
    public String artistName;

    @Nullable
    @ColumnInfo(name = "year")
    public String year;

    @Nullable
    @ColumnInfo(name = "image")
    public byte[] image;

    public Art(String artname, String artistName, String year, byte[] image) {
        this.artname = artname;
        this.artistName = artistName;
        this.year = year;
        this.image = image;
    }

}
