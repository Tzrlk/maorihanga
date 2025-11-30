#! /usr/bin/env nix
#! nix shell installables --command kotlin

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
    // 1. Normalize and clean the text, convert to lowercase, and handle macrons.
    var text = inputText.toLowerCase()
        .replace('ā', 'a' + '·')
        .replace('ē', 'e' + '·')
        .replace('ī', 'i' + '·')
        .replace('ō', 'o' + '·')
        .replace('ū', 'u' + '·')

    // Regex Update: Capture an optional second vowel/long mark (V2) to identify diphthongs.
    // Diphthongs are V1 followed by V2, both treated as part of the vowel component.
    // The pattern tries to match: (C or V-PL) + V1 + (optional ·) + (optional V2 + optional ·)
    val syllablePattern = Pattern.compile("((?:ng|wh|[mpntrkhw])?)((?:[aeiou])(·)?((?:[aeiou])(·)?)?)")
    val matcher = syllablePattern.matcher(text)

    val output = StringBuilder()

    while (matcher.find()) {
        val consonant = matcher.group(1).takeIf { it.isNotEmpty() } // C (e.g., 'k', 'ng') or null
        val vowelComponent = matcher.group(2)!! // Full Vowel/Diphthong Component (e.g., 'ao', 'a·')

        // Deconstruct Vowel Component for Compaction logic
        val v1Base = vowelComponent.first().toString() // The base vowel (V1) determines compaction
        val v1Char = MAORIHANGA_MAP[v1Base]!! + if (vowelComponent.contains('·')) "·" else "" // Simplified long mark logic

        // Check for Diphthong (Vowel component is longer than 2 characters: V + V or V + · + V)
        val isDiphthong = vowelComponent.length > 2 || (vowelComponent.length == 2 && vowelComponent.first() !in listOf('a','e','i','o','u'))

        // Simplified Diphthong Mapping: Combine the Jamo characters
        val finalVowel = if (vowelComponent.length > 1 && vowelComponent.substring(1).first() in listOf('a','e','i','o','u')) {
             // Basic implementation for V1V2 (e.g., 'ao')
             val v2Base = vowelComponent.substring(1).first().toString()
             MAORIHANGA_MAP[v1Base]!! + MAORIHANGA_MAP[v2Base]!!
        } else {
            MAORIHANGA_MAP[v1Base]!! + if (vowelComponent.contains('·')) "·" else ""
        }


        // --- Assemble the Consonant Component ---
        val initialC = if (consonant != null) {
            MAORIHANGA_MAP[consonant]!!
        } else {
            MAORIHANGA_MAP["VOWEL_PL"]!! // Use silent 'ㅇ' for vowel-initial syllables
        }

        // --- 3. Apply Compaction/Stacking Logic ---
        // Compaction is based on the FIRST vowel (V1)

        val firstBaseVowel = v1Base.first()

        val block = when (firstBaseVowel) {
            'o', 'u' -> {
                // Horizontal Vowel (V1): Stack Consonant on Top
                "(\n$initialC\n$finalVowel\n)"
            }
            'a', 'e', 'i' -> {
                // Vertical Vowel (V1): Place Consonant and Vowel side-by-side.
                "($initialC$finalVowel)"
            }
            else -> "($initialC$finalVowel)"
        }

        output.append(block).append(" ")
    }

    return output.toString().trim()
}

// --- 3. Example Execution (Testing Diphthongs) ---

fun main() {
    val exampleDiphthongs = "Aotearoa, tau, wai, koe, rua"
    val maorihangaResult = translateToMaorihanga(exampleDiphthongs)

    println("Original Māori: $exampleDiphthongs")
    println("---")
    println("Māorihanga Translation with Diphthongs (Simulated Compaction):")
    println(maorihangaResult)
}
