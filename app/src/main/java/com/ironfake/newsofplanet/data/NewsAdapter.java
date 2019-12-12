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

import org.json.JSONException;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private ArrayList<News> news;

    private OnUserClickListener listener;
    public interface OnUserClickListener{
        void onUserClick(int position) throws JSONException;
    }
    public void setOnUserClickListener(OnUserClickListener listener){
        this.listener = listener;
    }

    public NewsAdapter(Context context, ArrayList<News> news) {
        this.context = context;
        this.news = news;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item_recycler_view,
                parent, false);
        return new NewsViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News currentNews = news.get(position);

        String title = currentNews.getTitle();
        String description = currentNews.getShortDescription();
        String imageUrl = currentNews.getImageUrl();
        String authorTextView = currentNews.getAuthor();
        String newsSourceTextView = currentNews.getNewsSource();

        holder.titleNewsTextView.setText(title);
        holder.descriptionNewsTextView.setText(description);
        holder.authorTextView.setText(authorTextView);
        holder.newsSourceTextView.setText(newsSourceTextView);

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
        private TextView authorTextView;
        private TextView newsSourceTextView;


        public NewsViewHolder(@NonNull View itemView, final OnUserClickListener listener) {
            super(itemView);

            titleNewsTextView = itemView.findViewById(R.id.titleNewsTextView);
            descriptionNewsTextView = itemView.findViewById(R.id.descriptionNewsTextView);
            newsImageView = itemView.findViewById(R.id.newsImageView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            newsSourceTextView = itemView.findViewById(R.id.newsSourceTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            try {
                                listener.onUserClick(position);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }
}
