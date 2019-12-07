package com.ironfake.newsofplanet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ironfake.newsofplanet.R;
import com.ironfake.newsofplanet.data.NewsAdapter;
import com.ironfake.newsofplanet.model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String API_KEY = "ef28b00ad7e34564b0628ee9dffc4bfc";

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private ArrayList<News> newsArrayList;

    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.hasFixedSize();
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsArrayList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        gerNews();

    }

    private void gerNews() {
        Log.d("News", "start");
        String url = "https://newsapi.org/v2/top-headlines?country=us&apiKey=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("articles");
                            for (int i = 0; i < 10; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String title = jsonObject.getString("title");
                                String description = jsonObject.getString("description");
                                String imageUrl = jsonObject.getString("urlToImage");

                                News news = new News();
                                news.setTitle(title);
                                news.setShortDescription(description);
                                news.setImageUrl(imageUrl);
                                newsArrayList.add(news);
                            }
                            Log.d("News", String.valueOf(newsArrayList));
                            newsAdapter = new NewsAdapter(MainActivity.this, newsArrayList);
                            newsRecyclerView.setAdapter(newsAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }
}
