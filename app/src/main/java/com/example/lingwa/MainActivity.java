package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.lingwa.fragments.AddPostFragment;
import com.example.lingwa.fragments.HomeFragment;
import com.example.lingwa.fragments.MyProfileFragment;
import com.example.lingwa.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bnvTabs);

        final Fragment homeFragment = new HomeFragment();
        final Fragment myProfileFragment = new MyProfileFragment();
        final Fragment searchFragment = new SearchFragment();
        final Fragment addPostFragment = new AddPostFragment();

        fragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch(item.getItemId()) {
                    case R.id.action_home:
                        fragment = homeFragment;
                        break;
                    case R.id.action_my_profile:
                        fragment = myProfileFragment;
                        break;
                    case R.id.action_search:
                        fragment = searchFragment;
                        break;
                    case R.id.action_new_post:
                        fragment = addPostFragment;
                        break;
                    default:
                        return false;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
    }
}