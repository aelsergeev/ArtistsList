package com.example.andrew.artistslist;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.andrew.artistslist.models.ArtistModel;
import com.example.andrew.artistslist.library.RussiaEnding;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageArtistBig;
    private TextView textGenres;
    private TextView textTrackAlbum;
    private TextView textDescription;
    private TextView textLink;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Определение переменных
        setUpUIViews();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //Получаем JSON с первой активити
            String json = bundle.getString("artistModel");
            ArtistModel artistModel = new Gson().fromJson(json, ArtistModel.class);

            //Устанавливаем название для второй активити (имя певца)
            setTitle(artistModel.getName());

            //Отображение картинки по URL, а также прогресс бар пока кортинка не загрузится
            ImageLoader.getInstance().displayImage(artistModel.getCover().getBig(), imageArtistBig, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            //Отображение жанра
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < artistModel.getGenres().size(); i++) {
                //Последний жанр без запятой
                if (i == artistModel.getGenres().size()-1) {
                    stringBuffer.append(artistModel.getGenres().get(i));
                    break;
                }

                stringBuffer.append(artistModel.getGenres().get(i) + ", ");
            }
            textGenres.setText(stringBuffer);

            //Отображение количества альбомов и песен с разными окончаниями
            RussiaEnding ending = new RussiaEnding();
            int albums = artistModel.getAlbums();
            int tracks = artistModel.getTracks();
            textTrackAlbum.setText(albums + ending.albumsEnding(albums, " • ") + tracks + ending.tracksEnding(tracks, ""));

            //Отображение описания певца
            textDescription.setText(artistModel.getDescription());

            //Отображение ссылки
            if (artistModel.getLink() != null)
                textLink.setText(artistModel.getLink());
            else
                textLink.setVisibility(View.GONE);
        }
    }

    public void setUpUIViews() {
        imageArtistBig = (ImageView)findViewById(R.id.imageArtistBig);
        textGenres = (TextView)findViewById(R.id.textGenres);
        textTrackAlbum = (TextView)findViewById(R.id.textTrackAlbum);
        textDescription = (TextView)findViewById(R.id.textDescription);
        textLink = (TextView)findViewById(R.id.textLink);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }
}
