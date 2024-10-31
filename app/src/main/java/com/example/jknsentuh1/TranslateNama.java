package com.example.jknsentuh1;

import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TranslateNama extends AppCompatActivity {
    private TextView translatedTextView;
    private Vibrator vibrator;
    private GestureDetector gestureDetector;
    private boolean isVibrating = false; // Flag untuk menandai apakah sedang bergetar
    private boolean isVibrationFinished = false; // Flag untuk menandai apakah getaran telah selesai

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.translate_nama);

        translatedTextView = findViewById(R.id.translatedTextView);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        String resultText = getIntent().getStringExtra("resultText");
        translatedTextView.setText(resultText);

        gestureDetector = new GestureDetector(this, new GestureListener());

        // Jeda 2 detik sebelum melakukan getaran untuk "NAMA ANDA"
        translatedTextView.postDelayed(() -> {
            // Getaran untuk teks "NAMA ANDA"
            vibrateText("NAMA ANDA", () -> {
                // Setelah selesai, lanjutkan dengan getaran untuk resultText
                translateToVibration(resultText);
            });
        }, 2000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void vibrateText(String text, Runnable onComplete) {
        BrailleMap brailleMap = new BrailleMap();
        brailleMap.initializeBrailleMapForLetters(); // Inisialisasi huruf untuk "NAMA ANDA"
        isVibrating = true; // Setel isVibrating ke true saat mulai getaran

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
                            Thread.sleep(300); // Jeda setelah setiap getaran
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(500); // Jeda antara pembacaan karakter
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isVibrating = false; // Setel isVibrating ke false setelah selesai getaran
            isVibrationFinished = true; // Tandai bahwa getaran "NAMA ANDA" selesai
            runOnUiThread(onComplete); // Jalankan callback di thread UI setelah getaran selesai
        }).start();
    }

    private void translateToVibration(String text) {
        BrailleMap brailleMap = new BrailleMap();
        brailleMap.initializeBrailleMapForLetters(); // Gunakan metode baru
        isVibrating = true; // Setel isVibrating ke true saat mulai getaran
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
                            Thread.sleep(300); // Jeda setelah setiap getaran
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Jeda antar huruf
                try {
                    Thread.sleep(500); // Jeda antara huruf
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isVibrating = false; // Setel isVibrating ke false setelah selesai getaran
            isVibrationFinished = true; // Tandai bahwa getaran resultText selesai
        }).start();
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
                navigateToCmdNama(); // Swipe dari bawah ke atas dalam posisi reverse landscape
                return true;
            }
            return false;
        }
    }

    private void navigateToCmdNama() {
        // Melakukan getaran 3x cepat
        for (int i = 0; i < 3; i++) {
            vibrateShort();
            try {
                Thread.sleep(100); // Jeda 100ms antara getaran
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Pindah ke layar CmdNama
        Intent intent = new Intent(TranslateNama.this, CmdTanggalLahir.class);
        startActivity(intent);
    }
}
