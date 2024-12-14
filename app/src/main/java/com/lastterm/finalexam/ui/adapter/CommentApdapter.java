package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.uComment;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CommentApdapter extends RecyclerView.Adapter<CommentApdapter.CommentViewHolder>{
    private ArrayList<uComment> comments;
    private Context context;

    public CommentApdapter(ArrayList<uComment> comments) {
        this.comments = comments;
    }
    public void setComments(ArrayList<uComment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        uComment comment = comments.get(position);
        holder.txt_name.setText(comment.getName());
        holder.txt_comment.setText(comment.getComment());
        holder.txt_date.setText(new SimpleDateFormat("MM-dd-yyyy").format(comment.getDate()));
        holder.txt_rate.setText(String.valueOf(comment.getRate()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView txt_name, txt_comment, txt_date, txt_rate;;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_comment = itemView.findViewById(R.id.txt_comment);
            txt_date = itemView.findViewById(R.id.txt_date);
            txt_rate = itemView.findViewById(R.id.txt_rate);
        }
    }
}
