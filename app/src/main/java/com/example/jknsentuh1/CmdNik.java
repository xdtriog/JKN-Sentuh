package com.example.jknsentuh1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class CmdNik extends AppCompatActivity {
    private View circleIndicator;
    private GestureDetector gestureDetector;
    private Vibrator vibrator;
    private BrailleMap brailleMap;
    private Handler handler;
    private Thread brailleVibrationThread;
    private boolean isVibrationStarted = false; // Flag untuk menandai apakah getaran telah dimulai
    private boolean isVibrating = false; // Flag untuk menandai apakah getaran sedang berlangsung
    private boolean isVibrationFinished = false; // Flag untuk menandai apakah getaran telah selesai

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
        setContentView(R.layout.cmd_nik);
        circleIndicator = findViewById(R.id.circleIndicator);

        // Mengatur padding untuk system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_cmd_nik), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        brailleMap = new BrailleMap();
        handler = new Handler();

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
                        // Cek apakah getaran telah selesai
                        if (isVibrationFinished) {
                            performSwipeAction(); // Panggil metode untuk melakukan tindakan swipe
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        View mainView = findViewById(R.id.main_cmd_nik);
        mainView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Menambahkan jeda 2 detik sebelum memulai getaran Braille "MASUKKAN NIK ANDA"
        handler.postDelayed(() -> {
            isVibrationStarted = true; // Set flag untuk menandai bahwa getaran telah dimulai
            playBrailleVibration("MASUKKAN NIK ANDA"); //HARUS CAPITAL YA :)
        }, 2000);
        handler = new Handler(Looper.getMainLooper());
    }

    // Fungsi untuk melakukan tindakan swipe
    private void performSwipeAction() {
        // Hentikan getaran Braille "MASUKKAN NIK ANDA"
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

        // Setelah getaran, pindah ke BrailleKeyboardNik.java
        openNextActivity();
    }

    // Memulai BrailleKeyboardNik.java
    private void openNextActivity() {
        Intent intent = new Intent(CmdNik.this, BrailleKeyboardNik.class);
        startActivity(intent);
    }

    // Fungsi untuk memulai getaran Braille sesuai teks
    private void playBrailleVibration(String text) {
        isVibrating = true; // Setel isVibrating ke true saat mulai getaran
        brailleVibrationThread = new Thread(() -> {
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
                            Thread.sleep(300); // Tunggu sejenak antara getaran
                        } catch (InterruptedException e) {
                            // Jika thread diinterupsi, keluar dari loop
                            return;
                        }
                    }
                    try {
                        Thread.sleep(500); // Tunggu sejenak setelah setiap karakter
                    } catch (InterruptedException e) {
                        return; // Keluar dari loop jika diinterupsi
                    }
                }
            }
            isVibrationStarted = false; // Reset flag setelah getaran selesai
            isVibrating = false; // Setel isVibrating ke false setelah selesai getaran
            isVibrationFinished = true; // Tandai bahwa getaran telah selesai
        });
        brailleVibrationThread.start();
    }

    private void vibrateShort() {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            blinkCircleIndicator(Color.RED);  // Kedip merah untuk getaran pendek
        }
    }

    private void vibrateLong() {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            blinkCircleIndicator(Color.GREEN);  // Kedip hijau untuk getaran panjang
        }
    }
    private void blinkCircleIndicator(int blinkColor) {
        circleIndicator.setBackgroundColor(blinkColor);  // Setel warna indikator sesuai jenis getaran

        // Gunakan Handler untuk kembali ke abu-abu setelah waktu singkat untuk efek kedip
        handler.postDelayed(() -> circleIndicator.setBackgroundColor(Color.GRAY), 100);
    }

    // Menangkap event sentuhan untuk GestureDetector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
