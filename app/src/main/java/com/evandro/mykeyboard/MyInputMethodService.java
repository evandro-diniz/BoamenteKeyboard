package com.evandro.mykeyboard;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard;

import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.InputConnection;
import android.view.View;
import android.view.KeyEvent;
import android.media.AudioManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MyInputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener
{
    private static final java.util.UUID UUID = null;
    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean caps = false;
    private String texto = "";
    private String frase = "";
    private String uuid = "";

    @Override
    public View onCreateInputView()
    {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        //uuid = UUID.randomUUID().toString();
        getUUUID();
        return kv;
    }


    public String getUUUID() {
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor myEditor = myPreferences.edit();

        if (myPreferences.getString("uuid_saved", "unknown") == "unknown"){
            uuid = UUID.randomUUID().toString();
            myEditor.putString("uuid_saved", uuid);
            myEditor.commit();
        } else {
            uuid = myPreferences.getString("uuid_saved", "unknown");
        }

        return uuid;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "mhealth";
            String description = "mhealth-desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("mhealth", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void louchNotification() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "mhealth")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle("Informe seu código para concluir seu cadastro.")
                .setContentText("Já copiado: "+ uuid)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());
        Log.i("mhealth", "chamou a notificação");
        setClipboard(this, uuid);
    }

    private void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Código copiado: ", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        //playSound(primaryCode);
        //onPress(primaryCode);
        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                frase = frase.replaceFirst(".$","");
                Log.d("Caractere removido", frase);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                sendDefaultEditorAction(true);
                break;
            case (8):
                louchNotification();
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }
                if (caps) {
                    caps = false;
                    kv.getKeyboard().setShifted(false);
                    kv.invalidateAllKeys();
                }
                ic.commitText(String.valueOf(code),1);
                texto = String.valueOf(code);
                frase = frase + texto;
                Log.d("Frase criada", frase);
        }
    }

    @Override
    public void onFinishInput() {
        if (!frase.isEmpty()) {
            String[] textoSeparado = frase.split("\\s");
            if (textoSeparado.length > 2) {
                SegundoPlano segundoPlano = new SegundoPlano(frase, uuid);
                segundoPlano.execute();
                frase = "";
            }else{
                frase += " ";
            }
        }
    }

    private void playSound(int keyCode){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 0x20:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 0x0a:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onPress(int vCode) {
        Vibrator vibrator;
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (vCode < 0){
            vibrator.vibrate(50);
        }else {
            vibrator.vibrate(50);
        }

    }

    @Override
    public void onRelease(int primaryCode) { }

    @Override
    public void onText(CharSequence text) { }

    @Override
    public void swipeLeft() { }

    @Override
    public void swipeRight() { }

    @Override
    public void swipeDown() { }

    @Override
    public void swipeUp() { }


}