package com.example.jknsentuh1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BrailleKeyboardNik extends AppCompatActivity {
    private final boolean[] dots = new boolean[6];
    private Vibrator vibrator;
    private BrailleMap brailleMap;
    private TextView tempTextView;
    private GestureDetector gestureDetector;
    private StringBuilder resultText = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.braille_keyboard_nik);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        brailleMap = new BrailleMap();
        brailleMap.initializeBrailleMapForNumbers();  // Inisialisasi peta Braille angka
        tempTextView = findViewById(R.id.tempTextView);

        // Inisialisasi GestureDetector untuk swipe
        gestureDetector = new GestureDetector(this, new GestureListener());

        Button[] dotButtons = new Button[6];
        dotButtons[0] = findViewById(R.id.dot1);
        dotButtons[1] = findViewById(R.id.dot2);
        dotButtons[2] = findViewById(R.id.dot3);
        dotButtons[3] = findViewById(R.id.dot4);
        dotButtons[4] = findViewById(R.id.dot5);
        dotButtons[5] = findViewById(R.id.dot6);

        for (int i = 0; i < dotButtons.length; i++) {
            int finalI = i;
            dotButtons[i].setOnClickListener(v -> {
                dots[finalI] = !dots[finalI];
                dotButtons[finalI].setSelected(dots[finalI]);
                dotButtons[finalI].setBackgroundColor(dots[finalI] ?
                        ContextCompat.getColor(this, android.R.color.holo_blue_light) :
                        ContextCompat.getColor(this, android.R.color.darker_gray));
                vibrateShort();
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void vibrateShort() {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void vibrateLong() {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private String getBrailleCharacter() {
        StringBuilder pattern = new StringBuilder();
        for (boolean dot : dots) {
            pattern.append(dot ? "1" : "0");
        }
        return brailleMap.getCharacterFromDots(pattern.toString());
    }

    private void resetDots(Button[] dotButtons) {
        for (int i = 0; i < dots.length; i++) {
            dots[i] = false;
            dotButtons[i].setSelected(false);
            dotButtons[i].setBackgroundColor(ContextCompat.getColor(dotButtons[i].getContext(), android.R.color.darker_gray));
        }
    }

    private void vibrateAccordingToDots() {
        // Menghasilkan getaran berdasarkan pola titik yang diketik
        for (boolean dot : dots) {
            if (dot) {
                vibrateLong();
            } else {
                vibrateShort();
            }
            try {
                Thread.sleep(300); // Jeda antara getaran
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            // Deteksi swipe dari kiri ke kanan
            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                    diffX > 0) {  // Swipe dari kiri ke kanan
                confirmInput();
                return true;
            }
            // Deteksi swipe dari bawah ke atas
            if (Math.abs(diffY) > Math.abs(diffX) &&
                    Math.abs(diffY) > SWIPE_THRESHOLD &&
                    Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD &&
                    diffY < 0) {  // Swipe dari bawah ke atas
                navigateToTranslateNik();
                return true;
            }
            // Deteksi swipe dari kanan ke kiri
            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                    diffX < 0) {  // Swipe dari kanan ke kiri
                deleteLastCharacter();
                return true;
            }
            return false;
        }
    }

    private void confirmInput() {
        String character = getBrailleCharacter();
        if (character != null) {
            resultText.append(character);
            tempTextView.setText(resultText.toString());  // Tampilkan hasil input bertahap
            vibrateAccordingToDots();  // Panggil getaran sesuai pola titik Braille
        }
        resetDots(new Button[]{findViewById(R.id.dot1), findViewById(R.id.dot2), findViewById(R.id.dot3),
                findViewById(R.id.dot4), findViewById(R.id.dot5), findViewById(R.id.dot6)});
    }

    private void navigateToTranslateNik() {
        // Hentikan getaran yang sedang berjalan
        if (vibrator != null) {
            vibrator.cancel();
        }

        // Cek apakah sudah ada 4 angka
        if (resultText.length() >= 4) {
            // Melakukan getaran 3x cepat
            for (int i = 0; i < 3; i++) {
                vibrateShort();
                try {
                    Thread.sleep(100);  // Jeda 100ms antara getaran
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Mengirim nilai tempTextView ke TranslateNikActivity
            Intent intent = new Intent(BrailleKeyboardNik.this, TranslateNik.class);
            intent.putExtra("resultText", resultText.toString());
            startActivity(intent);
        } else {
            // Jika kurang dari 4 angka, beri tahu pengguna
            Toast.makeText(this, "Masukkan minimal 4 angka sebelum melanjutkan", Toast.LENGTH_SHORT).show();
            vibrateShort(); // Memberikan umpan balik haptik
        }
    }

    private void deleteLastCharacter() {
        if (resultText.length() > 0) {
            resultText.deleteCharAt(resultText.length() - 1);  // Hapus karakter terakhir
            tempTextView.setText(resultText.toString());  // Perbarui tampilan

            // Haptic feedback saat menghapus: 2x getar panjang
            vibrateLong();
            try {
                Thread.sleep(100); // Jeda 100ms antara getaran
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            vibrateLong(); // Getaran panjang kedua
        }
    }
}
