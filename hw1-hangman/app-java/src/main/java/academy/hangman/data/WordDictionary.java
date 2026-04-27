package academy.hangman.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WordDictionary implements WordProvider{

    private final Map<Category, List<Word>> wordsByCategory;

    public WordDictionary() {
        this.wordsByCategory = initializeDict();
    }

    private Map<Category, List<Word>> initializeDict() {
        Map<Category, List<Word>> dictionary = new HashMap<>();

        dictionary.put(Category.ANIMALS, List.of(
            new Word("Корова"), new Word("Овца"), new Word("Крокодил"),
            new Word("Зебра"), new Word("Обезьяна"), new Word("Панда"),
            new Word("Волк"), new Word("Лиса"), new Word("Медведь"),
            new Word("Кролик"), new Word("Белка"), new Word("Ёж"),
            new Word("Осёл"), new Word("Бык"), new Word("Дельфин")
        ));

        dictionary.put(Category.MOVIE, List.of(
            new Word("Интерстеллар"), new Word("Титаник"), new Word("Матрица"),
            new Word("Начало"), new Word("Аватар"), new Word("Гладиатор"),
            new Word("Форрест Гамп"), new Word("Джокер"), new Word("Гарри Поттер"),
            new Word("Властелин колец"), new Word("Человек-паук"), new Word("Бэтмен"),
            new Word("Чужой"), new Word("Терминатор"), new Word("Крестный отец")
        ));

        dictionary.put(Category.GEOGRAPHY, List.of(
            new Word("Африка"), new Word("Европа"), new Word("Азия"),
            new Word("Австралия"), new Word("Америка"), new Word("Россия"),
            new Word("Франция"), new Word("Япония"), new Word("Италия"),
            new Word("Бразилия"), new Word("Гималаи"), new Word("Амазонка"),
            new Word("Сахара"), new Word("Байкал"), new Word("Эверест")
        ));

        dictionary.put(Category.MUSIC, List.of(
            new Word("Гитара"), new Word("Барабаны"), new Word("Скрипка"),
            new Word("Фортепиано"), new Word("Флейта"), new Word("Рок"),
            new Word("Джаз"), new Word("Поп"), new Word("Хип-хоп"),
            new Word("Классика"), new Word("Опера"), new Word("Балалайка"),
            new Word("Саксофон"), new Word("Рэп"), new Word("Хор")
        ));

        dictionary.put(Category.FOOD, List.of(
            new Word("Пицца"), new Word("Суши"), new Word("Борщ"),
            new Word("Пельмени"), new Word("Стейк"), new Word("Бургер"),
            new Word("Салат"), new Word("Шашлык"), new Word("Хлеб"),
            new Word("Суп"), new Word("Паста"), new Word("Омлет"),
            new Word("Картофель"), new Word("Рыба"), new Word("Яблоко")
        ));

        dictionary.put(Category.SPORTS, List.of(
            new Word("Футбол"), new Word("Баскетбол"), new Word("Хоккей"),
            new Word("Теннис"), new Word("Волейбол"), new Word("Бокс"),
            new Word("Биатлон"), new Word("Формула-1"), new Word("Регби"),
            new Word("Гольф"), new Word("Шахматы"), new Word("Сёрфинг"),
            new Word("Лыжи"), new Word("Бег"), new Word("Плавание")
        ));

        dictionary.put(Category.SCIENCE, List.of(
            new Word("Физика"), new Word("Химия"), new Word("Биология"),
            new Word("Математика"), new Word("Астрономия"), new Word("Геология"),
            new Word("Экология"), new Word("Генетика"), new Word("Медицина"),
            new Word("Информатика"), new Word("Психология"), new Word("Социология"),
            new Word("Антропология"), new Word("Кибернетика"), new Word("Анатомия")
        ));

        dictionary.put(Category.HISTORY, List.of(
            new Word("Египет"), new Word("Рим"), new Word("Греция"),
            new Word("Средневековье"), new Word("Революция"), new Word("Наполеон"),
            new Word("Гитлер"), new Word("Петр Первый"), new Word("Монгольская империя"),
            new Word("Крестовые походы"), new Word("Холодная война"), new Word("Вторая мировая"),
            new Word("Первая мировая"), new Word("Октябрьская революция"), new Word("СССР")
        ));

        return dictionary;
    }

    @Override
    public Word getRandomWord(Category category) {
        List<Word> words = wordsByCategory.get(category);
        int randomIndex = ThreadLocalRandom.current().nextInt(words.size());
        return words.get(randomIndex);
    }

    @Override
    public List<Word> getWordsByCategory(Category category) {
        if (category == Category.RANDOM) {
            // Для RANDOM -> все слова из всех категорий
            List<Word> allWords = new ArrayList<>();
            for (List<Word> words : wordsByCategory.values()) {
                allWords.addAll(words);
            }
            return allWords;
        }

        return Collections.unmodifiableList(wordsByCategory.get(category));
    }

    public int getTotalWordsCount() {
        return wordsByCategory.values().stream()
            .mapToInt(List::size)
            .sum();
    }

}
