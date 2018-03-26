package com.yada.fact;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkTask extends AsyncTask<String, Void, String> {
    private static final MediaType JSON = MediaType.parse("application/json");

    protected String doInBackground(String... params) {
        String url = params[0];
        String json = params[1];

        OkHttpClient client = new OkHttpClient();

        try {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
