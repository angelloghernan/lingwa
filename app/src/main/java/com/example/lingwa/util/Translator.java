package com.example.lingwa.util;

import android.os.Handler;
import android.os.Looper;

import com.example.lingwa.BuildConfig;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Translator {
    private static final String subscriptionKey = BuildConfig.TRANSLATOR_KEY;

    private static final String location = "global";

    OkHttpClient client = new OkHttpClient();

    public HttpUrl buildUrl(String from, String to) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.cognitive.microsofttranslator.com")
                .addPathSegment("/translate")
                .addQueryParameter("api-version", "3.0")
                .addQueryParameter("from", from)
                .addQueryParameter("to", to)
                .build();
    }

    public String Post(String text, String from, String to) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "[{\"Text\": \"" + text + "\"}]");
        Request request = new Request.Builder().url(buildUrl(from, to)).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Ocp-Apim-Subscription-Region", location)
                .addHeader("Content-type", "application/json")
                .build();
        client.setProtocols(Collections.singletonList(Protocol.HTTP_1_1));
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void translateWord(String word, String from, String to, TranslatorCallback callback) {
        Translator translateRequest = new Translator();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String translation = null;
            try {
                translation = translateRequest.Post(word, from, to);
                translation = new JSONArray(translation)
                        .getJSONObject(0)
                        .getJSONArray("translations")
                        .getJSONObject(0)
                        .getString("text");
            } catch (IOException | JSONException e) {
                callback.onTranslationFailure(e);
            }
            String finalTranslation = translation;
            handler.post(() -> {
                if (finalTranslation != null)
                    callback.onTranslationSuccess(finalTranslation);
            });
        });
    }


    public interface TranslatorCallback {
        void onTranslationSuccess(String translation);
        void onTranslationFailure(Exception e);
    }
}
