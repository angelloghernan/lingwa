package com.example.lingwa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lingwa.R;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WordPickerAdapter extends RecyclerView.Adapter<WordPickerAdapter.ViewHolder> {

    List<String> wordList;
    List<Boolean> checkedWords;
    Context context;
    int numCardsSelected = 0;

    public WordPickerAdapter(Context context, List<String> wordList, List<Boolean> checkedWords) {
        this.wordList = wordList;
        this.context = context;
        this.checkedWords = checkedWords;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_word_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        String word = wordList.get(position);
        holder.bind(word, position);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public int getNumCardsSelected() {
        return numCardsSelected;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCard;
        MaterialCardView card;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvCard = itemView.findViewById(R.id.tvCard);
            card = itemView.findViewById(R.id.mcvCard);

            card.setOnClickListener(v -> {
                boolean set = false;

                if (card.isChecked()) {
                    numCardsSelected--;
                } else if (numCardsSelected < 10) {
                    set = true;
                    numCardsSelected++;
                }

                card.setChecked(set);
                checkedWords.set(getAdapterPosition(), set);
            });
        }

        public void bind(String word, int position) {
            tvCard.setText(word);
            card.setChecked(checkedWords.get(position));
        }
    }
}
