package common;

import java.util.*;

public class CompressionUtil {
    
    // Classe interne pour représenter une entrée du dictionnaire LZ78
    private static class DictionaryEntry {
        int index;
        char nextChar;
        
        DictionaryEntry(int index, char nextChar) {
            this.index = index;
            this.nextChar = nextChar;
        }
    }
    
    // Compresse une chaîne en utilisant LZ78
    public static String compressLZ78(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        List<DictionaryEntry> dictionary = new ArrayList<>();
        StringBuilder compressed = new StringBuilder();
        String current = "";
        
        for (int i = 0; i < input.length(); i++) {
            current += input.charAt(i);
            int index = findInDictionary(dictionary, current);
            
            if (index == -1) {
                // Ajouter au dictionnaire
                String prefix = current.substring(0, current.length() - 1);
                int prefixIndex = findInDictionary(dictionary, prefix);
                char nextChar = current.charAt(current.length() - 1);
                
                dictionary.add(new DictionaryEntry(prefixIndex, nextChar));
                compressed.append(prefixIndex).append(nextChar);
                current = "";
            }
        }
        
        // Gérer le dernier caractère si nécessaire
        if (!current.isEmpty()) {
            int index = findInDictionary(dictionary, current);
            compressed.append(index);
        }
        
        return compressed.toString();
    }
    
    // Décompresse une chaîne LZ78
    public static String decompressLZ78(String compressed) {
        if (compressed == null || compressed.isEmpty()) {
            return "";
        }

        List<String> dictionary = new ArrayList<>();
        dictionary.add(""); // Index 0 est une chaîne vide
        
        StringBuilder decompressed = new StringBuilder();
        int i = 0;
        
        while (i < compressed.length()) {
            // Lire l'index
            StringBuilder indexStr = new StringBuilder();
            while (i < compressed.length() && Character.isDigit(compressed.charAt(i))) {
                indexStr.append(compressed.charAt(i));
                i++;
            }
            
            int index = Integer.parseInt(indexStr.toString());
            String entry = dictionary.get(index);
            
            // Lire le caractère suivant s'il existe
            if (i < compressed.length()) {
                char nextChar = compressed.charAt(i);
                entry += nextChar;
                i++;
            }
            
            dictionary.add(entry);
            decompressed.append(entry);
        }
        
        return decompressed.toString();
    }
    
    // Calcule le bit de parité pour une chaîne
    public static boolean calculateParity(String data) {
        int ones = 0;
        for (char c : data.toCharArray()) {
            ones += Integer.bitCount(c);
        }
        return ones % 2 == 1;
    }
    
    // Vérifie si les données sont corrompues en utilisant le bit de parité
    public static boolean isDataCorrupted(String data, boolean expectedParity) {
        return calculateParity(data) != expectedParity;
    }
    
    // Trouve une chaîne dans le dictionnaire
    private static int findInDictionary(List<DictionaryEntry> dictionary, String str) {
        for (int i = 0; i < dictionary.size(); i++) {
            DictionaryEntry entry = dictionary.get(i);
            String dictStr = "";
            if (entry.index == 0) {
                dictStr = String.valueOf(entry.nextChar);
            } else {
                dictStr = dictionary.get(entry.index).nextChar + String.valueOf(entry.nextChar);
            }
            if (dictStr.equals(str)) {
                return i;
            }
        }
        return -1;
    }
} 