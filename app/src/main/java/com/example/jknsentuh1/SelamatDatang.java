package com.example.jknsentuh1;

import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SelamatDatang extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private Vibrator vibrator;
    private BrailleMap brailleMap;
    private Thread brailleVibrationThread;
    private boolean isVibrationStarted = false; // Menandai apakah getaran telah dimulai

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Mengatur tampilan fullscreen dengan menghilangkan system bars
        getWindow().setDecorFitsSystemWindows(false);
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
        setContentView(R.layout.selamat_datang);

        // Mengatur padding untuk system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_selamat_datang), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        brailleMap = new BrailleMap();

        // Mengatur GestureDetector untuk mendeteksi swipe dari bawah ke atas
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();

                // Mendeteksi swipe dari bawah ke atas
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {  // Swipe dari bawah ke atas
                        // Hentikan getaran dan berpindah halaman
                        if (isVibrationStarted || !brailleVibrationThread.isAlive()) {
                            stopBrailleVibration();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        View mainView = findViewById(R.id.main_selamat_datang);
        mainView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Menjalankan getaran Braille "SELAMAT DATANG" setelah 2 detik
        brailleVibrationThread = new Thread(() -> playBrailleVibration("ABC"));
        brailleVibrationThread.start();
    }

    // Fungsi untuk menghentikan getaran Braille "SELAMAT DATANG" dan berpindah halaman
    private void stopBrailleVibration() {
        // Hentikan getaran Braille "SELAMAT DATANG"
        if (brailleVibrationThread != null && brailleVibrationThread.isAlive()) {
            brailleVibrationThread.interrupt(); // Menghentikan thread yang sedang berjalan
        }

        // Jeda silent 250ms
        try {
            Thread.sleep(250); // Jeda sebelum getaran
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Lakukan 3x getaran cepat
        for (int i = 0; i < 3; i++) {
            vibrateShort();
            try {
                Thread.sleep(100); // Jeda singkat antara getaran
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Setelah getaran, pindah ke cmd_nik.java
        openNextActivity();
    }

    // Memulai cmd_nik.java
    private void openNextActivity() {
        Intent intent = new Intent(SelamatDatang.this, CmdNik.class);
        startActivity(intent);
    }

    // Fungsi untuk memulai getaran Braille sesuai teks
    private void playBrailleVibration(String text) {
        // Jeda 2 detik sebelum memulai getaran Braille
        try {
            Thread.sleep(2000); // Jeda 2 detik
        } catch (InterruptedException e) {
            return; // Keluar dari method jika diinterupsi
        }

        isVibrationStarted = true; // Set flag untuk menandai bahwa getaran telah dimulai

        for (char character : text.toCharArray()) {
            String brailleKey = brailleMap.getDotsFromCharacter(character);
            if (brailleKey != null) {
                for (char dot : brailleKey.toCharArray()) {
                    if (dot == '1') {
                        vibrateLong();
                    } else {
                        vibrateShort();
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        // Jika thread diinterupsi, keluar dari loop
                        return;
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return; // Keluar dari loop jika diinterupsi
                }
            }
        }

        isVibrationStarted = false; // Reset flag setelah getaran selesai
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

    // Menangkap event sentuhan untuk GestureDetector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
