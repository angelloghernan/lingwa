package com.example.lingwa.util;

import com.example.lingwa.wrappers.WordWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FamiliarityAlgo {
    public List<WordWrapper> calculateQuizOrder(List<WordWrapper> wordWrapperList) {
        int size = wordWrapperList.size();

        List<WordWrapper> newList = new ArrayList<>(size);

        // Sort in descending order of familiarity
        Collections.sort(wordWrapperList, new SortByFamiliarity());

        int remainder = size % 3;

        int divider = (int) size / 3;

        for (int i = 0; i < divider; i++) {
            // get a familiar word
            newList.add(wordWrapperList.get(i));
            // get a less familiar word
            newList.add(wordWrapperList.get(divider + i));
            // get an unfamiliar word
            newList.add(wordWrapperList.get((divider * 2) + i));
        }

        for (int i = 0; i < remainder; i++) {
            // add the two least familiar words at the end if
            // there are any left
            newList.add(wordWrapperList.get(size - (i + 1)));
        }

        return newList;
    }
}

class SortByFamiliarity implements Comparator<WordWrapper> {
    @Override
    public int compare(WordWrapper o1, WordWrapper o2) {
        return o2.getFamiliarityScore() - o1.getFamiliarityScore();
    }
}
