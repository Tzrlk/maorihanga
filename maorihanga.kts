import java.util.regex.Pattern

// --- 1. Māorihanga Character Map ---
// Maps Māori phonemes (C/V/Diphthong + Length) to Hangul Jamo.
// We use a dot (·) to represent the long vowel marker for visualization.

val MAORIHANGA_MAP = mapOf(
    // Consonants (C)
    "ng" to "ㄱ̇", // Ng (ㄱ + nasal dot)
    "wh" to "ㅍ", // Wh (fricative)
    "m" to "ㅁ̇",  // M (ㅁ + nasal dot)
    "p" to "ㅁ",  // P (base bilabial)
    "n" to "ㄴ̇",  // N (ㄴ + nasal dot)
    "t" to "ㄴ",  // T (base alveolar)
    "r" to "ㄹ",  // R (flap/trill)
    "k" to "ㄱ",  // K (base velar)
    "h" to "ㅎ",  // H (glottal)
    "w" to "ㅁ",  // W (simpler base for semi-vowel)

    // Vowels (V) and Long Vowels (V + .)
    "a" to "ㅏ", "ā" to "ㅏ·",
    "e" to "ㅓ", "ē" to "ㅓ·",
    "i" to "ㅣ", "ī" to "ㅣ·",
    "o" to "ㅗ", "ō" to "ㅗ·",
    "u" to "ㅡ", "ū" to "ㅡ·",
    
    // Vowel-Initial Syllable Placeholder (Silent 'ㅇ')
    "VOWEL_PL" to "ㅇ"
)

// --- 2. Syllabification and Translation Logic ---

fun translateToMaorihanga(inputText: String): String {
    // 1. Normalize and clean the text, convert to lowercase.
    // Replace macrons (ā, ō etc.) with the standard letter plus the long vowel marker (ā -> a·)
    var text = inputText.toLowerCase()
        .replace('ā', 'a' + '·')
        .replace('ē', 'e' + '·')
        .replace('ī', 'i' + '·')
        .replace('ō', 'o' + '·')
        .replace('ū', 'u' + '·')

    // 2. Tokenize the text into syllables/words using a simplified RegEx.
    // This Regex attempts to capture Ng, Wh, C, V, and Vowel-Long markers (·)
    // NOTE: True syllabification would require a more complex state machine.
    val syllablePattern = Pattern.compile("(ng|wh|[mpntrkhw])?([aeiou])(·)?([aeiou])?(·)?")
    val matcher = syllablePattern.matcher(text)
    
    val output = StringBuilder()

    while (matcher.find()) {
        val consonant = matcher.group(1) // C (e.g., 'k', 'ng')
        val vowel1 = matcher.group(2)    // V1 (e.g., 'a')
        val long1 = matcher.group(3)     // · (Long Vowel Marker)
        val vowel2 = matcher.group(4)    // V2 (For diphthongs/double vowels, e.g., 'o' in 'ao')
        val long2 = matcher.group(5)     // · (Long Vowel Marker for V2)
        
        // --- Assemble the Consonant Component ---
        val initialC = if (consonant != null) {
            MAORIHANGA_MAP[consonant]!! 
        } else if (vowel1 != null) {
            MAORIHANGA_MAP["VOWEL_PL"]!! // Use silent 'ㅇ' for vowel-initial syllables
        } else {
            "" // Should not happen in C-V structure
        }

        // --- Assemble the Vowel Component(s) ---
        var finalVowel = ""
        val v1Char = MAORIHANGA_MAP[vowel1]!! + (long1 ?: "")
        
        if (vowel2 != null) {
            // This is a diphthong or a double vowel (e.g., 'ao', 'ia')
            val v2Char = MAORIHANGA_MAP[vowel2]!! + (long2 ?: "")
            finalVowel = "$v1Char$v2Char" // Concatenate V1 and V2
        } else {
            finalVowel = v1Char
        }

        // --- 3. Apply Compaction/Stacking Logic ---
        // We use a simple vertical/horizontal Unicode line for visual stacking/compaction.
        // Horizontal Vowels: O (ㅗ), U (ㅡ) -> Stack Consonant on Top
        // Vertical Vowels: A (ㅏ), E (ㅓ), I (ㅣ) -> Place Consonant Left
        
        val firstBaseVowel = vowel1?.get(0)
        
        val block = when (firstBaseVowel) {
            'o', 'u' -> {
                // Horizontal Vowel: Stack vertically.
                "(\n$initialC\n$finalVowel\n)" 
            }
            'a', 'e', 'i' -> {
                // Vertical Vowel: Place Consonant and Vowel side-by-side.
                "($initialC$finalVowel)"
            }
            else -> "($initialC$finalVowel)"
        }
        
        output.append(block).append(" ")
    }
    
    return output.toString().trim()
}

// --- 3. Example Execution ---

fun main() {
    val maoriProverb = "Mā te rā ka mōhio"
    val maorihangaResult = translateToMaorihanga(maoriProverb)

    println("Original Māori: $maoriProverb")
    println("---")
    println("Māorihanga Translation (Simulated Compaction):")
    println(maorihangaResult)
    println("\n-------------------------------------------------\n")

    val anotherExample = "Aotearoa, Kia ora e hoa ma."
    val aotearoaResult = translateToMaorihanga(anotherExample)
    
    println("Original Māori: $anotherExample")
    println("---")
    println("Māorihanga Translation (Simulated Compaction):")
    println(aotearoaResult)
}