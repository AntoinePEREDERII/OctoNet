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

        Map<String, Integer> dictionary = new HashMap<>();
        dictionary.put("", 0); // Index 0 est une chaîne vide
        StringBuilder compressed = new StringBuilder();
        StringBuilder current = new StringBuilder();
        int nextIndex = 1;
        
        // Forcer la compression en ajoutant un préfixe spécial
        compressed.append("C");
        
        for (int i = 0; i < input.length(); i++) {
            current.append(input.charAt(i));
            String currentStr = current.toString();
            
            if (!dictionary.containsKey(currentStr)) {
                // Ajouter au dictionnaire
                String prefix = currentStr.substring(0, currentStr.length() - 1);
                int prefixIndex = dictionary.get(prefix);
                char nextChar = currentStr.charAt(currentStr.length() - 1);
                
                compressed.append(prefixIndex).append(nextChar);
                dictionary.put(currentStr, nextIndex++);
                current = new StringBuilder();
            }
        }
        
        // Gérer le dernier caractère si nécessaire
        if (current.length() > 0) {
            String currentStr = current.toString();
            compressed.append(dictionary.get(currentStr));
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
        
        try {
            // Vérifier le préfixe de compression
            if (compressed.charAt(i) != 'C') {
                throw new IllegalArgumentException("Format de compression invalide: préfixe manquant");
            }
            i++;
            
            while (i < compressed.length()) {
                // Lire l'index
                StringBuilder indexStr = new StringBuilder();
                while (i < compressed.length() && Character.isDigit(compressed.charAt(i))) {
                    indexStr.append(compressed.charAt(i));
                    i++;
                }
                
                if (indexStr.length() == 0) {
                    throw new IllegalArgumentException("Format de compression invalide: index manquant");
                }
                
                int index = Integer.parseInt(indexStr.toString());
                if (index >= dictionary.size()) {
                    throw new IllegalArgumentException("Index invalide dans le message compressé");
                }
                
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
        } catch (Exception e) {
            System.err.println("Erreur lors de la décompression: " + e.getMessage());
            return compressed; // Retourner le message original en cas d'erreur
        }
        
        return decompressed.toString();
    }
    
    // Calcule le bit de parité pour une chaîne
    public static boolean calculateParity(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        int ones = 0;
        for (char c : data.toCharArray()) {
            ones += Integer.bitCount(c);
        }
        return ones % 2 == 1;
    }
    
    // Vérifie si les données sont corrompues en utilisant le bit de parité
    public static boolean isDataCorrupted(String data, boolean expectedParity) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        try {
            // Le dernier caractère est le bit de parité
            String message = data.substring(0, data.length() - 1);
            boolean receivedParity = data.charAt(data.length() - 1) == '1';
            boolean calculatedParity = calculateParity(message);
            
            return receivedParity != calculatedParity;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de parité: " + e.getMessage());
            return true; // Considérer comme corrompu en cas d'erreur
        }
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