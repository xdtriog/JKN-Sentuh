package com.example.jknsentuh1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class cmd_nik extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private Vibrator vibrator;
    private BrailleMap brailleMap;
    private Handler handler;
    private Thread vibrationThread; // Thread untuk getaran
    private boolean isVibrating; // Menandakan apakah getaran sedang berlangsung

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cmd_nik);

        // Mengatur padding untuk system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_cmd_nik), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        brailleMap = new BrailleMap();
        handler = new Handler();
        isVibrating = false; // Inisialisasi status getaran

        // Mengatur GestureDetector untuk mendeteksi swipe dari bawah ke atas
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {  // Swipe dari bawah ke atas
                        stopVibration(); // Hentikan getaran jika masih berlangsung
                        vibrateShortRepeatedly(3); // Getaran 3x cepat
                        openBrailleKeyboard(); // Buka BrailleKeyboard
                        return true;
                    }
                }
                return false;
            }
        });

        View mainView = findViewById(R.id.main_cmd_nik);
        mainView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Menambahkan jeda 2 detik sebelum memulai getaran Braille "MASUKKAN NIK ANDA"
        handler.postDelayed(this::startVibration, 2000);
    }

    // Memulai getaran dalam thread terpisah
    private void startVibration() {
        if (isVibrating) {
            return; // Jika sudah bergetar, tidak mulai lagi
        }

        isVibrating = true; // Tandai bahwa getaran sedang berlangsung
        vibrationThread = new Thread(() -> playBrailleVibration("MASUKKAN NIK ANDA"));
        vibrationThread.start();
    }

    // Fungsi untuk memulai getaran Braille sesuai teks
    private void playBrailleVibration(String text) {
        for (char character : text.toCharArray()) {
            String brailleKey = brailleMap.getDotsFromCharacter(character);
            if (brailleKey != null) {
                for (char dot : brailleKey.toCharArray()) {
                    if (!isVibrating) {
                        return; // Keluar jika getaran telah dihentikan
                    }
                    if (dot == '1') {
                        vibrateLong();
                    } else {
                        vibrateShort();
                    }
                    try {
                        Thread.sleep(300); // Tunggu sejenak antara getaran
                    } catch (InterruptedException e) {
                        return; // Keluar dari loop jika thread diinterupsi
                    }
                }
                try {
                    Thread.sleep(500); // Tunggu sejenak setelah setiap karakter
                } catch (InterruptedException e) {
                    return; // Keluar dari loop jika diinterupsi
                }
            }
        }
        isVibrating = false; // Tandai bahwa getaran selesai
    }

    private void stopVibration() {
        isVibrating = false; // Tandai bahwa getaran harus dihentikan
        if (vibrationThread != null) {
            vibrationThread.interrupt(); // Hentikan thread getaran
        }
    }

    private void openBrailleKeyboard() {
        Intent intent = new Intent(cmd_nik.this, BrailleKeyboard.class);
        startActivity(intent);
    }

    private void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void vibrateLong() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void vibrateShortRepeatedly(int count) {
        for (int i = 0; i < count; i++) {
            vibrateShort();
            try {
                Thread.sleep(100); // Jeda antara getaran
            } catch (InterruptedException e) {
                // Tangani interupsi jika perlu
            }
        }
    }

    // Menangkap event sentuhan untuk GestureDetector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
