package com.example.villagetocityreseilingapp.ui.main.seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.villagetocityreseilingapp.LocaleHelper;
import com.example.villagetocityreseilingapp.R;
import com.example.villagetocityreseilingapp.activity.seller_MainActivity;

public class SellerChooseLanguageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_seller_choose_language,
                container, false
        );

        AppCompatButton btnUrdu =
                view.findViewById(R.id.btnUrdu);

        AppCompatButton btnEnglish =
                view.findViewById(R.id.btnEnglish);

        btnUrdu.setOnClickListener(v -> {
            LocaleHelper.setNewLocale(
                    requireActivity(), "ur"
            );
            restartApp();
        });

        btnEnglish.setOnClickListener(v -> {
            LocaleHelper.setNewLocale(
                    requireActivity(), "en"
            );
            restartApp();
        });

        return view;
    }

    private void restartApp() {
        Intent intent = new Intent(
                getActivity(),
                seller_MainActivity.class
        );
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        requireActivity().finish();
    }
}