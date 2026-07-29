package com.example.villagetocityreseilingapp.ui.main.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.R;

public class SellerVerificationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_verification, container, false);

        AppCompatButton btn1 = view.findViewById(R.id.btnUploadCnicFront);
        btn1.setOnClickListener(v -> {
            Toast.makeText(getContext(), "CNIC Front Selected!", Toast.LENGTH_SHORT).show();
        });

        AppCompatButton btn2 = view.findViewById(R.id.btnUploadCnicBack);
        btn2.setOnClickListener(v -> {
            Toast.makeText(getContext(), "CNIC Back Selected!", Toast.LENGTH_SHORT).show();
        });

        AppCompatButton btn3 = view.findViewById(R.id.btnSubmit);
        btn3.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Verification Submitted!", Toast.LENGTH_LONG).show();
        });

        return view;
    }
}