package com.example.glphotoeditorpro.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.glphotoeditorpro.R;

import java.util.ArrayList;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    Context context;
    List<Integer> colorList;
    ColorAdapterListener listener;

    public ColorAdapter(Context context, ColorAdapterListener listener) {
        this.context = context;
        this.colorList = genColorList();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.color_item,
                parent, false);
        return new ColorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        holder.color_selection.setCardBackgroundColor(colorList.get(position));
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder{
        public CardView color_selection;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            color_selection = itemView.findViewById(R.id.color_selection);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("myLog", "color: " + colorList.get(getAdapterPosition()));
                    listener.onColorSelected(colorList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface ColorAdapterListener{
        void onColorSelected(int color);
    }

    private List<Integer> genColorList() {
        List<Integer> colorList = new ArrayList<>();

        colorList.add(Color.parseColor("#000000"));
        colorList.add(Color.parseColor("#ffffff"));
        colorList.add(Color.parseColor("#f55652"));
        colorList.add(Color.parseColor("#c02519"));
        colorList.add(Color.parseColor("#ff0040"));
        colorList.add(Color.parseColor("#ff7e00"));
        colorList.add(Color.parseColor("#a5570e"));
        colorList.add(Color.parseColor("#1f4c25"));
        colorList.add(Color.parseColor("#7fffd4"));
        colorList.add(Color.parseColor("#623053"));

        return colorList;
    }
}
