package com.example.jknsentuh1;

import java.util.HashMap;

public class BrailleMap {
    private final HashMap<String, String> brailleMap;

    public BrailleMap() {
        brailleMap = new HashMap<>();
        initializeBrailleMap();  // Inisialisasi default dengan alfabet A-Z dan simbol
    }

    private void initializeBrailleMap() {
        // Peta kombinasi titik Braille untuk alfabet A-Z
        brailleMap.put("100000", "A");
        brailleMap.put("101000", "B");
        brailleMap.put("110000", "C");
        brailleMap.put("110100", "D");
        brailleMap.put("100100", "E");
        brailleMap.put("111000", "F");
        brailleMap.put("111100", "G");
        brailleMap.put("101100", "H");
        brailleMap.put("011000", "I");
        brailleMap.put("011100", "J");
        brailleMap.put("100010", "K");
        brailleMap.put("101010", "L");
        brailleMap.put("110010", "M");
        brailleMap.put("110110", "N");
        brailleMap.put("100110", "O");
        brailleMap.put("111010", "P");
        brailleMap.put("111110", "Q");
        brailleMap.put("101110", "R");
        brailleMap.put("011010", "S");
        brailleMap.put("011110", "T");
        brailleMap.put("100011", "U");
        brailleMap.put("101011", "V");
        brailleMap.put("011101", "W");
        brailleMap.put("110011", "X");
        brailleMap.put("110111", "Y");
        brailleMap.put("100111", "Z");

        // Peta kombinasi titik Braille untuk simbol
        brailleMap.put("001101", ".");  // Titik
        brailleMap.put("001000", ",");  // Koma
        brailleMap.put("001011", "?");  // Tanda tanya
        brailleMap.put("001110", "!");  // Tanda seru
        brailleMap.put("000010", "'");  // Apostrof
        brailleMap.put("000011", "_");  // Garis bawah
        brailleMap.put("010111", "#");  // Tagar
    }

        public void initializeBrailleMapForLetters() {
        initializeBrailleMap();  // Panggil metode private untuk menginisialisasi huruf
    }

    public void initializeBrailleMapForNumbers() {
        brailleMap.clear();  // Bersihkan map sebelum menambahkan angka

        // Peta kombinasi titik Braille untuk angka 0-9
        brailleMap.put("010110", "0");
        brailleMap.put("100000", "1");
        brailleMap.put("101000", "2");
        brailleMap.put("110000", "3");
        brailleMap.put("110100", "4");
        brailleMap.put("100100", "5");
        brailleMap.put("111000", "6");
        brailleMap.put("111100", "7");
        brailleMap.put("101100", "8");
        brailleMap.put("011100", "9");
    }

    // Metode untuk mendapatkan peta Braille
    public HashMap<String, String> getBrailleMap() {
        return brailleMap;
    }

    public String getCharacterFromDots(String dotsPattern) {
        return brailleMap.get(dotsPattern);
    }

    public String getDotsFromCharacter(char character) {
        for (HashMap.Entry<String, String> entry : brailleMap.entrySet()) {
            if (entry.getValue().equals(String.valueOf(character))) {
                return entry.getKey();
            }
        }
        return null;
    }
}
