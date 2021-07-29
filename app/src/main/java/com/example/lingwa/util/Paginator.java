package com.example.lingwa.util;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.Nullable;

import com.example.lingwa.util.epubparser.BookSection;
import com.example.lingwa.util.epubparser.Reader;
import com.example.lingwa.util.epubparser.exception.OutOfPagesException;
import com.example.lingwa.util.epubparser.exception.ReadingException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

// Class to help split text into separate pages in ContentActivity
// Adapted from: https://stackoverflow.com/questions/31837840/paginating-text-in-android
public class Paginator {
    private final boolean includePad;
    private final int width;
    private final int height;
    private final float spacingMultiplier;
    private final float spacingAdded;
    private final Reader reader;
    private CharSequence text;
    private final TextPaint paint;
    private final List<CharSequence> pages;

    public Paginator(CharSequence text, int pageWidth, int pageHeight, TextPaint paint,
                     float spacingMultiplier, float spacingAdded, boolean includePadding, @Nullable Reader reader) {

        this.includePad = includePadding;
        this.width = pageWidth;
        this.height = pageHeight;
        this.spacingMultiplier = spacingMultiplier;
        this.spacingAdded = spacingAdded;
        this.text = text;
        this.paint = paint;
        this.pages = new ArrayList<>();
        this.reader = reader;

        paginateText();
    }

    private void paginateText() {
        final StaticLayout layout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL,
                spacingMultiplier, spacingAdded, includePad);
        final int lines = layout.getLineCount();
        final CharSequence text = layout.getText();
        int startOffset = 0;
        int lHeight = height;

        for (int i = 0; i < lines; i++) {
            if (layout.getLineBottom(i) > lHeight) {
                // if layout height has been exceeded on this line, add a new page starting at
                // the start of the last page, ending at the beginning of this line
                addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                startOffset = layout.getLineStart(i);
                lHeight = layout.getLineTop(i) + height;
            }

            if (i == lines - 1) {
                // If this is the last line in text, put this
                // current chunk of text into the next page and end.
                addPage(text.subSequence(startOffset, layout.getLineStart(i)));
                return;
            }

        }
    }

    private void addPage(CharSequence text) {
        pages.add(text);
    }

    private void removePage(int position) { pages.remove(position); }

    public int size() {
        return pages.size();
    }

    public CharSequence get(int index) {
        if (index >= 0 && index < pages.size()) {
            return pages.get(index);
        } else {
            return null;
        }
    }

    public void changeSection(int index) {
        if (reader == null) {
            return;
        }
        pages.clear();
        try {
            BookSection section = reader.readSection(index);
            text = section.getSectionTextContent();
        } catch (ReadingException | OutOfPagesException e) {
            e.printStackTrace();
        }
        paginateText();

    }
}
