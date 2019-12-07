package com.ironfake.newsofplanet.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ironfake.newsofplanet.R;
import com.ironfake.newsofplanet.model.News;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private ArrayList<News> news;

    public NewsAdapter(Context context, ArrayList<News> news) {
        this.context = context;
        this.news = news;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item_recycler_view,
                parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News currentNews = news.get(position);

        String title = currentNews.getTitle();
        String description = currentNews.getShortDescription();
        String imageUrl = currentNews.getImageUrl();

        holder.titleNewsTextView.setText(title);
        holder.descriptionNewsTextView.setText(description);
        Picasso.get().load(imageUrl).fit().centerInside().into(holder.newsImageView);
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {

        private TextView titleNewsTextView;
        private TextView descriptionNewsTextView;
        private ImageView newsImageView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            titleNewsTextView = itemView.findViewById(R.id.titleNewsTextView);
            descriptionNewsTextView = itemView.findViewById(R.id.descriptionNewsTextView);
            newsImageView = itemView.findViewById(R.id.newsImageView);
        }
    }
}
