package com.ironfake.newsofplanet.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private SwipeRefreshLayout swipeRefreshLayout;

    private String url;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLocation;

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

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNews(url);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null){

                }

            }
        });

        getNews("");

    }

    private void getNews(String query) {
        newsArrayList.clear();
        if (query.contains("q=")){
            url = "https://newsapi.org/v2/everything?language=en&q=" +query + "&apiKey=" + API_KEY;
        } else if (query.contains("category=")){
            url = "https://newsapi.org/v2/top-headlines?country=us&" +query + "&apiKey=" + API_KEY;
        } else {
            url = "https://newsapi.org/v2/top-headlines?country=us&category=general&apiKey=" + API_KEY;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("articles");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject newsJsonObject = jsonArray.getJSONObject(i);

                                String title = newsJsonObject.getString("title");
                                String description = newsJsonObject.getString("description");
                                String imageUrl = newsJsonObject.getString("urlToImage");
                                String siteNewsUrl = newsJsonObject.getString("url");
                                String author = newsJsonObject.getString("author");

                                JSONObject newsSourceJsonObject = newsJsonObject.getJSONObject("source");
                                String newsSource = newsSourceJsonObject.getString("name");


                                if (!imageUrl.equals("null")){
                                    News news = new News();
                                    news.setTitle(title);
                                    news.setShortDescription(description);
                                    news.setImageUrl(imageUrl);
                                    news.setSiteNewsUrl(siteNewsUrl);
                                    if (!author.equals("null")) news.setAuthor(author);
                                    news.setNewsSource(newsSource);
                                    newsArrayList.add(news);
                                }
                            }
                            newsAdapter = new NewsAdapter(MainActivity.this, newsArrayList);
                            newsAdapter.setOnUserClickListener(new NewsAdapter.OnUserClickListener() {
                                @Override
                                public void onUserClick(int position) {
                                    News news = newsArrayList.get(position);
                                    Uri address = Uri.parse(news.getSiteNewsUrl());
                                    Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);

                                    if (openLinkIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(openLinkIntent);
                                    } else {
                                        Log.d("Intent", "Не получается обработать намерение!");
                                    }
                                }
                            });
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
