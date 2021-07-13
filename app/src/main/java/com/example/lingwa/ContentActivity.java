package com.example.lingwa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.Locale;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class ContentActivity extends AppCompatActivity {

    private static final String TAG = "ContentActivity";
    private final Context context = this;
    Button btnUpload;
    TextView tvBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        btnUpload = findViewById(R.id.btnUpload);
        tvBody = findViewById(R.id.tvBody);
        tvBody.setHighlightColor(Color.LTGRAY);

        // TODO: Allow user to import their own book to read
        /*
        EpubReader epubReader = new EpubReader();
        try {
            Book book = epubReader.readEpub(new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } */

        // TODO: Allow user to upload their own book
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        makeTextClickable();
    }

    // Adds ClickableSpan to every word so that the user can tap on them.
    private void makeTextClickable() {
        // Currently using sample text to check.
        String sampleText = "El aeropuerto se considera como un aeródromo para el tráfico regular de aviones.\n" +
                "\n" +
                "Es un área definida de la superficie, ya sea de tierra, agua o hielo propuesto para la llegada, salida y movimiento en superficie de aeronaves de distintos tipos con llegadas y salidas nacionales e internacionales.\n" +
                "\n" +
                "Habitualmente este término se aplica a todas las pistas donde aterrizan aviones, sin embargo el término correcto es aeródromo.\n" +
                "\n" +
                "Los grandes aeropuertos cuentan con pistas de aterrizaje pavimentadas de uno o varios kilómetros de extensión, calles de rodaje, terminales de pasajeros y carga, grandes superficies de estacionamientos, etc.\n" +
                "\n" +
                "En los aeropuertos los aviones suelen recibir combustible, mantenimiento y reparaciones.".trim();
        // Long link movement method needed so that the text is highlighted when pressed and
        // the onClick and onLongClick functions are fired when clicked and held respectively
        tvBody.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        tvBody.setText(sampleText, TextView.BufferType.SPANNABLE);
        // Create a spannable so that we can edit the properties of individual words
        Spannable spannable = (Spannable) tvBody.getText();
        // Use a BreakIterator so we can go over each word one by one
        BreakIterator iterator = BreakIterator.getWordInstance();
        iterator.setText(sampleText);
        int start = iterator.first();
        // Go over each word one by one until there are no words left (end = BreakIterator.DONE).
        // If the word starts with a letter or digit, add a clickable span to the word.
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String possibleWord = sampleText.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                LongClickableSpan clickSpan = getLongClickableSpan(possibleWord);
                spannable.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }
        }
    }

    private LongClickableSpan getLongClickableSpan(String spanWord) {
        return new LongClickableSpan() {
            @Override
            public void onLongClick(View view) {
                ParseUser.getCurrentUser().addUnique("savedWords", spanWord);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving word: " + e.toString());
                            return;
                        }
                        Toast.makeText(context, "Word saved!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            final String word = spanWord;

            @Override
            public void onClick(@NonNull View widget) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                Translator.translateWord(word, "es", "en", new Translator.TranslatorCallback() {
                    @Override
                    public void onTranslationSuccess(String translation) {
                        builder.setMessage(translation)
                                .setTitle("Translation");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onTranslationFailure(Exception e) {
                        Log.e(TAG, "on failure to translate word: " + e);
                    }
                });

            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
    }
}