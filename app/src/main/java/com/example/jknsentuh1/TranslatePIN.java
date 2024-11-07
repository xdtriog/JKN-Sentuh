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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TranslatePIN extends AppCompatActivity {
    private View circleIndicator;
    private Handler handler;
    private TextView translatedTextView;
    private Vibrator vibrator;
    private GestureDetector gestureDetector;
    private boolean isVibrating = false;
    private boolean isVibrationFinished = false; // Flag untuk menandai apakah getaran telah selesai

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mengatur tampilan fullscreen dengan menghilangkan system bars
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.translate_pin);
        circleIndicator = findViewById(R.id.circleIndicator);

        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        translatedTextView = findViewById(R.id.translatedTextView);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        String resultText = BrailleKeyboardPIN.resultTextGlobalPIN;
        translatedTextView.setText(resultText);

        gestureDetector = new GestureDetector(this, new GestureListener());

        // Jeda 2 detik sebelum melakukan getaran sesuai pola Braille
        translatedTextView.postDelayed(() -> {
            // Getaran untuk teks "PIN ANDA" //HARUS CAPITAL YA :)
            vibrateText("PIN ANDA", () -> {
                // Setelah selesai, lanjutkan dengan getaran untuk resultText
                translateToVibration(resultText);
            });
        }, 2000);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void vibrateText(String text, Runnable onComplete) {
        BrailleMap brailleMap = new BrailleMap();
        brailleMap.initializeBrailleMapForLetters();  // Inisialisasi huruf untuk "NIK ANDA"
        isVibrating = true;  // Setel isVibrating ke true saat mulai getaran

        new Thread(() -> {
            for (char c : text.toCharArray()) {
                String braillePattern = brailleMap.getDotsFromCharacter(c);
                if (braillePattern != null) {
                    for (char dot : braillePattern.toCharArray()) {
                        if (dot == '1') {
                            vibrateLong();
                        } else {
                            vibrateShort();
                        }
                        try {
                            Thread.sleep(300);  // Jeda setelah setiap getaran
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(500);  // Jeda antara pembacaan karakter
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isVibrating = false;  // Setel isVibrating ke false setelah selesai getaran
            isVibrationFinished = true; // Tandai bahwa getaran "NIK ANDA" selesai
            runOnUiThread(onComplete); // Jalankan callback di thread UI setelah getaran selesai
        }).start();
    }

    private void translateToVibration(String text) {
        BrailleMap brailleMap = new BrailleMap();
        brailleMap.initializeBrailleMapForNumbers();
        isVibrating = true;  // Setel isVibrating ke true saat mulai getaran
        new Thread(() -> {
            for (char c : text.toCharArray()) {
                String braillePattern = brailleMap.getDotsFromCharacter(c);
                if (braillePattern != null) {
                    for (char dot : braillePattern.toCharArray()) {
                        if (dot == '1') {
                            vibrateLong();
                        } else {
                            vibrateShort();
                        }
                        try {
                            Thread.sleep(300);  // Jeda setelah setiap getaran
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(500);  // Jeda antara pembacaan angka
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isVibrating = false;  // Setel isVibrating ke false setelah selesai getaran
            isVibrationFinished = true; // Tandai bahwa getaran resultText selesai
        }).start();
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();

            // Cek apakah sedang bergetar, jika ya maka swipe tidak akan diproses
            if (isVibrating || !isVibrationFinished) {
                return false; // Mencegah swipe saat getaran sedang berlangsung
            }

            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD && diffY < 0) {
                navigateToPINSucces();  // Swipe dari bawah ke atas dalam posisi reverse landscape
                return true;
            }
            return false;
        }
    }

    private void navigateToPINSucces() {
        // Melakukan getaran 3x cepat
        for (int i = 0; i < 3; i++) {
            vibrateShort();
            try {
                Thread.sleep(100);  // Jeda 100ms antara getaran
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(TranslatePIN.this, PINSucces.class);
        startActivity(intent);
    }
}
