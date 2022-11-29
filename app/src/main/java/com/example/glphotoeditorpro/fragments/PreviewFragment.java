package com.example.glphotoeditorpro.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.glphotoeditorpro.Interface.PreviewFragmentListener;
import com.example.glphotoeditorpro.R;

import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class PreviewFragment extends Fragment {

    public static final String TAG = "PreviewFragment";
    ImageButton btn_gallery;

    PreviewFragmentListener listener;

    public void setListener(PreviewFragmentListener listener) {
        this.listener = listener;
    }

    static PreviewFragment instance;

    public static PreviewFragment getInstance() {
        if (instance == null)
            instance = new PreviewFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preview, container, false);

        btn_gallery = root.findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onGalleryButtonCLickListener();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft
                        .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_right)
                        .remove(PreviewFragment.this)
                        .commit();
            }
        });
        return root;
    }
}
