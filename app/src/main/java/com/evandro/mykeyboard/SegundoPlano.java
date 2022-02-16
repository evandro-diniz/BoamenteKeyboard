package com.evandro.mykeyboard;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SegundoPlano extends AsyncTask<Void, Void, Void> {
    private final String texto;
    private final String identificador;

    public SegundoPlano(String texto, String identificador) {
        this.texto = texto;
        this.identificador = identificador;
    }


    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateTime = dateFormat.format(new Date()); // Find todays date
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("Recebimento de texto", texto);
        Log.d("Recebimento de uuid", identificador);
        //String email = "primeiro.email@mail.com";
        String datetime = getCurrentTimeStamp();
        Log.d("Tempo", datetime);

        // create your json here
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", texto);
            jsonObject.put("identificador", identificador);
            jsonObject.put("datetime", datetime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient okHttpClient=new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        // put your json here

        RequestBody formbody = RequestBody.create(JSON, jsonObject.toString());
        /*RequestBody formbody = new FormBody.Builder()
                .add("text",texto)
                .add("email",email)
                .add("datetime", datetime)
                .build();*/

        Request request= new Request.Builder()
                .url("http://192.168.18.97:5000/classifica")
                .post(formbody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //Toast.makeText(SegundoPlano.this, "network not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("Requeste", "Correto");
            }
        });
        return null;
    }

}
