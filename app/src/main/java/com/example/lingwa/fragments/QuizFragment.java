package com.example.lingwa.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lingwa.R;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuizFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuizFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_WORD = "word";
    private static final String ARG_TRANSLATION = "translation";

    private String mWord;
    private String mTranslation;

    TextView tvQuizWord;
    TextView tvQuizTranslation;

    public QuizFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * This fragment shows whenever the user is struggling
     * with a word. This allows them to get more practice with a word
     * ("Here's what this word means")
     *
     * @param word Untranslated word
     * @param translation Translated word
     * @return A new instance of fragment QuizFragment.
     */
    public static QuizFragment newInstance(String word, String translation) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        args.putString(ARG_TRANSLATION, translation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWord = getArguments().getString(ARG_WORD);
            mTranslation = getArguments().getString(ARG_TRANSLATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize views
        tvQuizWord = view.findViewById(R.id.tvQuizWord);
        tvQuizTranslation = view.findViewById(R.id.tvQuizTranslation);
        tvQuizWord.setText(mWord);
        tvQuizTranslation.setText(mTranslation);
    }
}