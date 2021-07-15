package com.example.lingwa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MyStuffActivity extends AppCompatActivity {

    ListView lvSavedWords;
    Button btnPractice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stuff);
        lvSavedWords = findViewById(R.id.lvSavedWords);
        btnPractice = findViewById(R.id.btnPractice);

        JSONArray jArray = ParseUser.getCurrentUser().getJSONArray("savedWords");

        List<String> savedWords = new ArrayList<>();
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

    }
}