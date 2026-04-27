package academy.hangman.data;

import java.util.List;

public interface WordProvider {

    Word getRandomWord(Category category);

    List<Word> getWordsByCategory(Category category);

}

