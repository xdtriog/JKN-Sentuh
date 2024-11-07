package com.example.jknsentuh1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BrailleKeyboardNik extends AppCompatActivity {

    public static String resultTextGlobalNIK;
    private final boolean[] dots = new boolean[6];
    private Vibrator vibrator;
    private BrailleMap brailleMap;
    private TextView tempTextView;
    private GestureDetector gestureDetector;
    private StringBuilder resultText = new StringBuilder();
    private View circleIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mengatur tampilan fullscreen dengan menghilangkan system bars
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.braille_keyboard_nik);

        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        brailleMap = new BrailleMap();
        brailleMap.initializeBrailleMapForNumbers();  // Inisialisasi peta Braille angka
        tempTextView = findViewById(R.id.tempTextView);

        // Inisialisasi circleIndicator
        circleIndicator = findViewById(R.id.circleIndicator);
        updateCircleIndicator(-1);

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
        resultTextGlobalNIK = "";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
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

    // Metode untuk membuat indikator berkedip
    private void blinkCircleIndicator(int blinkColor) {
        circleIndicator.setBackgroundColor(blinkColor);  // Setel warna indikator sesuai jenis getaran

        // Gunakan Handler untuk kembali ke abu-abu setelah waktu singkat untuk efek kedip
        new android.os.Handler().postDelayed(() -> circleIndicator.setBackgroundColor(Color.GRAY), 100);
    }


    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
            updateCircleIndicator(0);  // Update warna ke biru untuk getaran 0
        }
    }

    private void clearVibrationStatus() {
        updateCircleIndicator(-1);  // Kembalikan ke abu-abu jika tidak ada getaran
    }

    // Metode untuk mengubah warna circleIndicator
    private void updateCircleIndicator(int vibrationStatus) {
        int color;
        switch (vibrationStatus) {
            case 1:
                color = Color.RED;  // Getaran pendek, warna merah
                break;
            case 2:
                color = Color.GREEN;  // Getaran panjang, warna hijau
                break;
            default:
                color = Color.GRAY;  // Tidak ada getaran, warna abu-abu
                break;
        }
        circleIndicator.setBackgroundColor(color);
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
        android.os.Handler handler = new android.os.Handler();
        for (int i = 0; i < dots.length; i++) {
            final boolean dot = dots[i];
            handler.postDelayed(() -> {
                if (dot) {
                    vibrateLong();
                    blinkCircleIndicator(Color.GREEN);
                } else {
                    vibrateShort();
                    blinkCircleIndicator(Color.RED);
                }
            }, i * 500);  // jeda 500 ms antara getaran untuk memastikan kedip terlihat
        }
    }



    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                    diffX > 0) {
                confirmInput();
                return true;
            }
            if (Math.abs(diffY) > Math.abs(diffX) &&
                    Math.abs(diffY) > SWIPE_THRESHOLD &&
                    Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD &&
                    diffY < 0) {
                navigateToTranslateNik();
                return true;
            }
            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD &&
                    diffX < 0) {
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
        if (vibrator != null) {
            vibrator.cancel();
        }

        if (resultText.length() >= 16) {
            for (int i = 0; i < 3; i++) {
                vibrateShort();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            resultTextGlobalNIK = resultText.toString();

            Intent intent = new Intent(BrailleKeyboardNik.this, TranslateNik.class);
            //intent.putExtra("resultText", resultTextGlobalNIK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Masukkan minimal 16 angka sebelum melanjutkan", Toast.LENGTH_SHORT).show();
            vibrateShort();
        }
    }

    private void deleteLastCharacter() {
        if (resultText.length() > 0) {
            resultText.deleteCharAt(resultText.length() - 1);
            tempTextView.setText(resultText.toString());
            vibrateLong();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            vibrateLong();
        }
    }
}
