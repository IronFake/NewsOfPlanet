package com.ironfake.newsofplanet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ironfake.newsofplanet.R;
import com.ironfake.newsofplanet.data.CategoryNewsAdapter;
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

    private RecyclerView categoryNewsRecyclerView;
    private CategoryNewsAdapter categoryNewsAdapter;
    private ArrayList<String> categoriesArrayList;
    private final String[] categories = {"Business", "Entertainment", "General", "Health", "Sports", "Technology"};

    private RequestQueue requestQueue;

    private SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getNews("q=" + query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        categoriesArrayList = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            categoriesArrayList.add(categories[i]);
        }
        categoryNewsRecyclerView = findViewById(R.id.categoryNewsRecyclerView);
        categoryNewsRecyclerView.hasFixedSize();
        categoryNewsRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        categoryNewsAdapter = new CategoryNewsAdapter(MainActivity.this, categoriesArrayList);
        categoryNewsAdapter.setOnUserClickListener(new CategoryNewsAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                getNews("category=" +categories[position]);
            }
        });
        categoryNewsRecyclerView.setAdapter(categoryNewsAdapter);

        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.hasFixedSize();
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsArrayList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        getNews("");

    }

    private void getNews(String query) {
        newsArrayList.clear();
        String url;
        if (query.contains("q=")){
            url = "https://newsapi.org/v2/everything?q=" +query + "&apiKey=" + API_KEY;
        } else if (query.contains("category=")){
            url = "https://newsapi.org/v2/top-headlines?" +query + "&apiKey=" + API_KEY;
        } else {
            url = "https://newsapi.org/v2/top-headlines?category=general&apiKey=" + API_KEY;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("articles");
                            for (int i = 0; i < jsonArray.length(); i++) {
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
