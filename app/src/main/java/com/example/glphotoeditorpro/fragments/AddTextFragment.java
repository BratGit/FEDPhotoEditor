package com.example.glphotoeditorpro.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.glphotoeditorpro.Interface.AddTextFragmentListener;
import com.example.glphotoeditorpro.R;
import com.example.glphotoeditorpro.adapters.ColorAdapter;

public class AddTextFragment extends Fragment implements ColorAdapter.ColorAdapterListener {
    public static final String TAG = "AddTextFragment";

    int colorSelected = Color.parseColor("#000000");//default

    AddTextFragmentListener listener;

    EditText edt_add_text;
    RecyclerView recycler_color;
    Button btn_add_text;
    ColorAdapter colorAdapter;

    public void setListener(AddTextFragmentListener listener) {
        this.listener = listener;
    }

    static AddTextFragment instance;
    public static AddTextFragment getInstance(){
        if (instance == null)
            instance = new AddTextFragment();
        return instance;
    }

    public AddTextFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_add_text, container, false);

        edt_add_text = itemView.findViewById(R.id.edt_add_text);
        recycler_color = itemView.findViewById(R.id.recycler_color);
        btn_add_text = itemView.findViewById(R.id.btn_add_text);

        recycler_color.setHasFixedSize(true);
        recycler_color.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));

        colorAdapter = new ColorAdapter(getContext(), this);
        recycler_color.setAdapter(colorAdapter);

        btn_add_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                listener.onAddTextButtonClick(edt_add_text.getText().toString(), colorSelected);
            }
        });

        return  itemView;
    }

    @Override
    public void onColorSelected(int color) {
        colorSelected = color;
    }
}