package com.example.andrew.artistslist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrew.artistslist.library.RussiaEnding;
import com.example.andrew.artistslist.models.ArtistModel;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String jsonURL = "http://cache-spb01.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists.json";

    private TextView textMain;
    private ListView listArtists;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create default options which will be used for every
        //displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .defaultDisplayImageOptions(defaultOptions)
        .build();
        ImageLoader.getInstance().init(config);

        listArtists = (ListView)findViewById(R.id.listArtists);
        textMain = (TextView)findViewById(R.id.textMain);

        //Обновление списка артиство свайпом вниз
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new JSONParser().execute(jsonURL);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        //Загружаем и парсим JSON
        new JSONParser().execute(jsonURL);
    }

    public class JSONParser extends AsyncTask<String, String, List<ArtistModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Loading data...", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected List<ArtistModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                //Подключаемся к JSON файлу
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                //Присваиваем StringBuffer сожержимое файла JSON
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                //Из StringBuffer переводим String
                String finalJson = buffer.toString();

                //Парсим String в массив JSON элементов
                JSONArray array = new JSONArray(finalJson);

                //Создаем List модели ArtistModel класса
                List<ArtistModel> artistModelList = new ArrayList<>();

                //Используем библиотеку Googla, чтобы распарсить JSON в наш класс ArtistModel
                Gson gson = new Gson();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject artistObject = array.getJSONObject(i);

                    ArtistModel artistModel = gson.fromJson(artistObject.toString(), ArtistModel.class);

                    //добавляем нашу модель класса в List -> artistModelList
                    artistModelList.add(artistModel);
                }

                return artistModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<ArtistModel> result) {
            super.onPostExecute(result);
            swipeRefreshLayout.setRefreshing(false);
            textMain.setVisibility(View.GONE);

            //Проверяем URL подключение
            if (result != null) {
                //Отображаем список артистов
                ArrayAdapter adapter = new ArtistAdapter(getApplicationContext(), R.layout.row, result);
                listArtists.setAdapter(adapter);

                //Создание новой активити, при нажатии на певца и передача
                //в нее информации об певце
                listArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ArtistModel artistModel = result.get(position);
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra("artistModel", new Gson().toJson(artistModel));
                        startActivity(intent);
                    }
                });
            } else {
                //Нету JSON файла
                Toast.makeText(getApplicationContext(), "Not able to fetch data from server.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ArtistAdapter extends ArrayAdapter {

        private List<ArtistModel> artistModelList;
        private int resource;
        private LayoutInflater inflater;

        public ArtistAdapter(Context context, int resource, List<ArtistModel> objects) {
            super(context, resource, objects);
            artistModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                //Создаем переменные
                holder = new ViewHolder();

                convertView = inflater.inflate(resource, null);

                //Определяем переменные
                holder.imageArtistSmall = (ImageView)convertView.findViewById(R.id.imageArtistSmall);
                holder.textName = (TextView)convertView.findViewById(R.id.textName);
                holder.textGenres = (TextView)convertView.findViewById(R.id.textGenres);
                holder.textTrackAlbum = (TextView)convertView.findViewById(R.id.textTrackAlbum);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            //Отображение картинки по URL, а также прогресс бар пока кортинка не загрузится
            final ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            String imageUrl = artistModelList.get(position).getCover().getSmall();
            ImageLoader.getInstance().displayImage(imageUrl, holder.imageArtistSmall, new ImageLoadingListener() {
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

            //Отображение имени певца
            holder.textName.setText(artistModelList.get(position).getName());

            //Отображение жанра
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < artistModelList.get(position).getGenres().size(); i++) {
                if (i == artistModelList.get(position).getGenres().size()-1) {
                    stringBuffer.append(artistModelList.get(position).getGenres().get(i));
                    break;
                }

                stringBuffer.append(artistModelList.get(position).getGenres().get(i) + ", ");
            }
            holder.textGenres.setText(stringBuffer);

            //Отображение количества альбомов и песен с разными окончаниями
            RussiaEnding ending = new RussiaEnding();
            int albums = artistModelList.get(position).getAlbums();
            int tracks = artistModelList.get(position).getTracks();
            holder.textTrackAlbum.setText(albums + ending.albumsEnding(albums, ", ") + tracks + ending.tracksEnding(tracks, ""));

            return convertView;
        }

        class ViewHolder {
            private ImageView imageArtistSmall;
            private TextView textName;
            private TextView textGenres;
            private TextView textTrackAlbum;
        }
    }
}
