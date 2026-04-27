package academy.hangman.data;

import java.util.Set;

public class Word {
    private final String originalWord;
    private final String normalizedWord;

    public Word(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new IllegalArgumentException("Слово не может быть пустым");
        }
        this.originalWord = word.trim();
        this.normalizedWord = originalWord.toLowerCase();
    }

    public boolean contains(char letter) {
        char lowerLetter = Character.toLowerCase(letter);
        return normalizedWord.indexOf(lowerLetter) >= 0;
    }

    public String getMasked(Set<Character> guessedLetters) {
        StringBuilder masked = new StringBuilder();

        for (char c : originalWord.toCharArray()) {
            if (Character.isLetter(c)) {
                char lowerChar = Character.toLowerCase(c);
                if (guessedLetters.contains(lowerChar)) {
                    masked.append(c); // Показываем угаданную
                } else {
                    masked.append('*'); // Скрываем не угаданную
                }
            } else {
                masked.append(c); // Пробелы, дефисы
            }
        }

        return masked.toString().trim();
    }

    public boolean isGuessed(Set<Character> guessedLetters) {
        for (char c : normalizedWord.toCharArray()) {
            if (Character.isLetter(c) && !guessedLetters.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public String getOriginal() { return originalWord; }
    public int getLength() { return originalWord.length(); }

    @Override
    public String toString() { return originalWord; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return normalizedWord.equals(word.normalizedWord);
    }

    @Override
    public int hashCode() { return normalizedWord.hashCode(); }
}
