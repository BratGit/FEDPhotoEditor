package com.example.glphotoeditorpro.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.glphotoeditorpro.R;
import com.example.glphotoeditorpro.activities.MainActivity;
import com.karumi.dexter.Dexter;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "myLog";

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<Drawable> mImages = new ArrayList<>();
    private Context mContext;
    private static OnCardClickListener mListener;
    private int selectedIndex = 0;

    public RecyclerViewAdapter(ArrayList<String> mNames, ArrayList<Drawable> mImages, Context mContext) {
        this.mNames = mNames;
        this.mImages = mImages;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_editing_tools, parent, false);
        return new ViewHolder(view);
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.textView.setText(mNames.get(position));
        holder.imageView.setImageDrawable(mImages.get(position));
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public interface OnCardClickListener {
        void onCardClick(View v, String action);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgToolIcon);
            textView = itemView.findViewById(R.id.txtTool);
            itemView.setOnClickListener(this);
            textView.setTextColor(Color.WHITE);
        }

        @Override
        public void onClick(View v) {
            mListener.onCardClick(v, textView.getText().toString());
            Log.d(TAG, "onCardClick activated, text = " + textView.getText().toString());
        }
    }
}
