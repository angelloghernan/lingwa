package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class MyStuffActivity extends AppCompatActivity {

    ListView lvSavedWords;
    Button btnPractice;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stuff);
        lvSavedWords = findViewById(R.id.lvSavedWords);
        btnPractice = findViewById(R.id.btnPractice);

        JSONArray jArray = ParseUser.getCurrentUser().getJSONArray("savedWords");

        ArrayList<String> savedWords = new ArrayList<>();
        if (jArray != null) {
            for (int i = 0; i < jArray.length(); i++) {
                try {
                    savedWords.add(jArray.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayAdapter<String> savedWordsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedWords);
        lvSavedWords.setAdapter(savedWordsAdapter);

        btnPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FlashcardsActivity.class);
                intent.putStringArrayListExtra("savedWords", savedWords);
                startActivity(intent);
            }
        });

    }
}