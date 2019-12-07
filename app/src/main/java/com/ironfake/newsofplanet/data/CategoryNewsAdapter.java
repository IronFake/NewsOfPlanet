package com.ironfake.newsofplanet.data;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ironfake.newsofplanet.R;

import java.util.ArrayList;

public class CategoryNewsAdapter extends RecyclerView.Adapter<CategoryNewsAdapter.CategoryNewsViewHolder> {

    private Context context;
    private ArrayList<String> categories;
    private OnUserClickListener listener;

    private int indexOnClickCategory = -1;

    public interface OnUserClickListener{
        void onUserClick(int position);
    }
    public void setOnUserClickListener(OnUserClickListener listener){
        this.listener = listener;
    }

    public CategoryNewsAdapter(Context context, ArrayList<String> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item_recycler_view,
                parent, false);

        return new CategoryNewsViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryNewsViewHolder holder, final int position) {
//        String category = categories.get(position);
//        holder.categoryTextView.setText(category);
        holder.bindItem(position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class CategoryNewsViewHolder extends RecyclerView.ViewHolder {

        private TextView categoryTextView;

        public CategoryNewsViewHolder(@NonNull View itemView, final OnUserClickListener listener) {
            super(itemView);

            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            indexOnClickCategory = position;
                            notifyDataSetChanged();
                            listener.onUserClick(position);
                        }
                    }
                }
            });
        }
        private void bindItem(int position) {
            String category = categories.get(position);
            categoryTextView.setText(category);
            if (indexOnClickCategory == position){
                categoryTextView.setTextColor(Color.RED);
            } else {
                categoryTextView.setTextColor(Color.BLACK);
            }
        }

    }
}
