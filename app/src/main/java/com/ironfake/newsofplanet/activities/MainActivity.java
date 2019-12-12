package com.ironfake.newsofplanet.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ironfake.newsofplanet.R;
import com.ironfake.newsofplanet.data.CategoryNewsAdapter;
import com.ironfake.newsofplanet.data.NewsAdapter;
import com.ironfake.newsofplanet.model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String API_KEY_NEWS = "ef28b00ad7e34564b0628ee9dffc4bfc";
    private final String API_KEY_WEATHER = "763ee1da671f4f21913bffda4f36937d";

    //news
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private ArrayList<News> newsArrayList;
    private TextView newsNotFoundTextView;

    //categories
    private RecyclerView categoryNewsRecyclerView;
    private CategoryNewsAdapter categoryNewsAdapter;
    private ArrayList<String> categoriesArrayList;
    private final String[] categories = {"Business", "Entertainment", "General", "Health", "Sports", "Technology"};

    //search queue
    private RequestQueue requestQueue;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String newsUrl;
    private String currentCategory;

    //weather
    private ImageView weatherImageView;
    private TextView tempTextView, cityTextView, appTempTextView, humidityTextView, windSpeedTextView;

    //filter dialog elements
    private Spinner sortBySpinner;
    private String currentMethodSort;
    private TextView headingTextView;
    private LinearLayout sortByLinearLayout;

    //location
    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 3600, FASTEST_INTERVAL = 3600; // 1 hour in seconds

    //lists for permissions
    ArrayList<String> permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions result request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    //
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headingTextView = findViewById(R.id.headingTextView);
        sortByLinearLayout = findViewById(R.id.sortByLinearLayout);

        sharedPreferences = this.getSharedPreferences("app preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        createSearchView();
        createSortBySpinner();
        createCategoriesRecyclerView();
        createNewsRecyclerView();

        //we build google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        //we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (permissionsToRequest.size() > 0){
                requestPermissions(permissionsToRequest
                        .toArray(new String[permissionsToRequest.size()]),ALL_PERMISSIONS_RESULT);
            }
        }



        getNews("");
        getWeather();
    }

    private void createSearchView(){
        searchView = findViewById(R.id.searchView);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery(query, false);
                searchView.clearFocus();
                headingTextView.setText(R.string.heading_search_result);
                sortByLinearLayout.setVisibility(View.VISIBLE);
                getNews("q=" + query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void createSortBySpinner(){
        sortBySpinner = findViewById(R.id.sortBySpinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.sort_by_spinner_array));
        sortBySpinner.setAdapter(arrayAdapter);
        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMethodSort = sortBySpinner.getSelectedItem().toString();
                Toast.makeText(MainActivity.this, currentMethodSort, Toast.LENGTH_LONG).show();
                getNews("q=" + searchView.getQuery());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void createCategoriesRecyclerView(){
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
                currentCategory = categories[position];
                headingTextView.setText(R.string.heading_headlines);
                sortByLinearLayout.setVisibility(View.GONE);
                searchView.setQuery("", false);
                getNews("category=" +currentCategory);
            }
        });
        categoryNewsRecyclerView.setAdapter(categoryNewsAdapter);
    }

    private void createNewsRecyclerView(){
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsNotFoundTextView = findViewById(R.id.newsNotFoundTextView);
        int countColumn = getResources().getInteger(R.integer.column_count);
        newsRecyclerView.hasFixedSize();
        newsRecyclerView.setLayoutManager(new GridLayoutManager(this, countColumn));
        newsArrayList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!searchView.getQuery().toString().trim().equals("")){
                    getNews("q=" + searchView.getQuery().toString().trim());
                }else getNews("category=" +currentCategory);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm: wantedPermissions) {
            if (!hasPermission(perm)){
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()){
            Toast.makeText(this,
                    "You need to install Google Play Services", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
            }else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        //Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        //startLocationUpdates();
        if (location != null){
            getWeather();
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "You need to enable permissions to display location!",
                    Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null){
            getWeather();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest){
                    if (!hasPermission(perm)){
                        permissionsRejected.add(perm);
                    }
                }
                if (permissionsRejected.size() > 0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (shouldShowRequestPermissionRationale(
                                permissionsRejected.get(0))){
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]),
                                                        ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).
                                    setNegativeButton("Cancel", null).create().show();
                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null){
                        googleApiClient.reconnect();
                    }
                }

                break;
        }
    }

    private void getNews(String query) {
        newsArrayList.clear();
        if (query.contains("q=")){
            newsUrl = "https://newsapi.org/v2/everything?language=en&sortBy="+ currentMethodSort +
                    "&" +query + "&apiKey=" + API_KEY_NEWS;
        } else if (query.contains("category=")){
            newsUrl = "https://newsapi.org/v2/top-headlines?country=us&" +query + "&apiKey=" + API_KEY_NEWS;
        } else {
            newsUrl = "https://newsapi.org/v2/top-headlines?country=us&category=general&apiKey=" + API_KEY_NEWS;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, newsUrl, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                try {
                    final JSONArray jsonArray = response.getJSONArray("articles");
                    if (jsonArray.length() < 1) newsNotFoundTextView.setVisibility(View.VISIBLE);
                    else newsNotFoundTextView.setVisibility(View.GONE);
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
                        public void onUserClick(int position) throws JSONException {
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

    private void getWeather(){

        weatherImageView = findViewById(R.id.weatherImageView);
        tempTextView = findViewById(R.id.temperatureTextView);
        cityTextView = findViewById(R.id.cityTextView);
        appTempTextView = findViewById(R.id.appTempTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);

        String weatherUrl;
        if (googleApiClient.isConnected() && location != null){
            weatherUrl = "https://api.weatherbit.io/v2.0/current?lat=" + location.getLatitude() +
                    "&lon=" + location.getLongitude() + "&key=" + API_KEY_WEATHER;
        }else{
            String city = sharedPreferences.getString("city name", "Moscow");
            weatherUrl = "https://api.weatherbit.io/v2.0/current?city=" + city
                    + "&key=" + API_KEY_WEATHER;
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, weatherUrl, null,
            new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String temperature = jsonObject.getString("temp");
                            String town = jsonObject.getString("city_name");
                            String appTemp = jsonObject.getString("app_temp");
                            String humidity = jsonObject.getString("rh");
                            String windSpeed = jsonObject.getString("wind_spd");

                            JSONObject weatherJsonObject = jsonObject.getJSONObject("weather");
                            String imageCode = weatherJsonObject.getString("icon");
                            weatherImageView.setImageResource(getResources().
                                    getIdentifier(imageCode, "drawable", getPackageName()));
                            tempTextView.setText(getString(R.string.temperature, temperature));
                            cityTextView.setText(town);
                            appTempTextView.setText(getString(R.string.app_temperature, appTemp));
                            humidityTextView.setText(getString(R.string.humidity, humidity));
                            windSpeedTextView.setText(getString(R.string.wind_speed, windSpeed));
                        }

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

    public void setTown(View view) {
        AlertDialog.Builder setTownBuilder = new AlertDialog.Builder(MainActivity.this);
        View setTownView = getLayoutInflater().inflate(R.layout.change_city_alert_dialog, null);
        final EditText cityNameEditText = setTownView.findViewById(R.id.cityNameEditText);
        final Button saveButton = setTownView.findViewById(R.id.saveButton);

        setTownBuilder.setView(setTownView);
        final AlertDialog dialog = setTownBuilder.create();
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String city = cityNameEditText.getText().toString().trim();
                String weatherUrl = "https://api.weatherbit.io/v2.0/current?city=" + city +
                        "&key=" + API_KEY_WEATHER;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, weatherUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray jsonArray = response.getJSONArray("data");
                                    editor.putString("city name", city);
                                    editor.commit();
                                    Toast.makeText(MainActivity.this, "City: " + city + " saved", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    getWeather();
                                } catch (JSONException e) {
                                    //Toast.makeText(MainActivity.this, "Enter correct city name", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Enter correct city name", Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                });
                requestQueue.add(request);
            }
        });

    }
}
