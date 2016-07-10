package com.example.andrew.artistslist.library;

//Изменяет окончания слов
public class RussiaEnding {

    //Изменять окончания для слова "Альбом"
    public String albumsEnding (int n, String interval) {
        if (n%100 > 20) n = n%10;
        if (n == 0) return " альбомов" + interval;
        if (n == 1) return " альбом" + interval;
        if (n == 2 || n == 3 || n == 4) return " альбома"  + interval;
        else return " альбомов" + interval;
    }

    //Изменять окончания для слова "Песня"
    public String tracksEnding (int n, String interval) {
        if (n%100 > 20) n = n%10;
        if (n == 0) return " песен" + interval;
        if (n == 1) return " песня" + interval;
        if (n == 2 || n == 3 || n == 4) return " песни"  + interval;
        else return " песен" + interval;
    }
}
