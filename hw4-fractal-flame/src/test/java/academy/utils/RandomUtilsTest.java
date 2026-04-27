package academy.utils;

import static org.junit.jupiter.api.Assertions.*;

import academy.transformations.WeightedTransformation;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RandomUtilsTest {

    @Test
    @DisplayName("weighted возвращает взвешенную случайную трансформацию")
    void testWeighted() {
        List<WeightedTransformation> list = new ArrayList<>();
        WeightedTransformation wt1 = new WeightedTransformation(null, 1.0, 0, 0, 0);
        WeightedTransformation wt2 = new WeightedTransformation(null, 2.0, 0, 0, 0);
        WeightedTransformation wt3 = new WeightedTransformation(null, 3.0, 0, 0, 0);
        list.add(wt1);
        list.add(wt2);
        list.add(wt3);

        Random rnd = new Random(12345L);

        WeightedTransformation result = RandomUtils.weighted(list, rnd);

        assertNotNull(result);
        assertTrue(list.contains(result));
    }

    @Test
    @DisplayName("weighted корректно работает с одним элементом")
    void testWeightedSingleElement() {
        List<WeightedTransformation> list = new ArrayList<>();
        WeightedTransformation wt = new WeightedTransformation(null, 1.0, 0, 0, 0);
        list.add(wt);

        Random rnd = new Random();

        WeightedTransformation result = RandomUtils.weighted(list, rnd);

        assertSame(wt, result);
    }

    @Test
    @DisplayName("weighted выбрасывает исключение для пустого списка")
    void testWeightedEmptyList() {
        List<WeightedTransformation> list = new ArrayList<>();
        Random rnd = new Random();

        assertThrows(NoSuchElementException.class, () -> RandomUtils.weighted(list, rnd));
    }

    @Test
    @DisplayName("weightedWithIndex возвращает трансформацию с индексом")
    void testWeightedWithIndex() {
        List<WeightedTransformation> list = new ArrayList<>();
        WeightedTransformation wt1 = new WeightedTransformation(null, 1.0, 0, 0, 0);
        WeightedTransformation wt2 = new WeightedTransformation(null, 2.0, 0, 0, 0);
        WeightedTransformation wt3 = new WeightedTransformation(null, 3.0, 0, 0, 0);
        list.add(wt1);
        list.add(wt2);
        list.add(wt3);

        Random rnd = new Random(12345L);

        RandomUtils.WeightedTransformationWithIndex result = RandomUtils.weightedWithIndex(list, rnd);

        assertNotNull(result);
        assertNotNull(result.transformation());
        assertTrue(result.index() >= 0 && result.index() < list.size());
        assertSame(list.get(result.index()), result.transformation());
    }

    @Test
    @DisplayName("weightedWithIndex корректно работает с одним элементом")
    void testWeightedWithIndexSingleElement() {
        List<WeightedTransformation> list = new ArrayList<>();
        WeightedTransformation wt = new WeightedTransformation(null, 1.0, 0, 0, 0);
        list.add(wt);

        Random rnd = new Random();

        RandomUtils.WeightedTransformationWithIndex result = RandomUtils.weightedWithIndex(list, rnd);

        assertEquals(wt, result.transformation());
        assertEquals(0, result.index());
    }

    @Test
    @DisplayName("weightedWithIndex выбрасывает исключение для пустого списка")
    void testWeightedWithIndexEmptyList() {
        List<WeightedTransformation> list = new ArrayList<>();
        Random rnd = new Random();

        assertThrows(
                NoSuchElementException.class, // Изменено с IndexOutOfBoundsException
                () -> RandomUtils.weightedWithIndex(list, rnd));
    }

    @Test
    @DisplayName("Статистическое распределение соответствует весам")
    void testStatisticalDistribution() {
        List<WeightedTransformation> list = new ArrayList<>();
        WeightedTransformation wt1 = new WeightedTransformation(null, 1.0, 0, 0, 0);
        WeightedTransformation wt2 = new WeightedTransformation(null, 2.0, 0, 0, 0);
        WeightedTransformation wt3 = new WeightedTransformation(null, 3.0, 0, 0, 0);
        list.add(wt1);
        list.add(wt2);
        list.add(wt3);

        int[] counts = new int[3];
        Random rnd = new Random(99999L);
        int trials = 10000;

        for (int i = 0; i < trials; i++) {
            WeightedTransformation result = RandomUtils.weighted(list, rnd);
            int index = list.indexOf(result);
            counts[index]++;
        }

        double totalWeight = 6.0;
        double expected1 = (1.0 / totalWeight) * trials;
        double expected2 = (2.0 / totalWeight) * trials;
        double expected3 = (3.0 / totalWeight) * trials;

        double tolerance = 0.05 * trials;

        assertEquals(expected1, counts[0], tolerance);
        assertEquals(expected2, counts[1], tolerance);
        assertEquals(expected3, counts[2], tolerance);
    }

    @Test
    @DisplayName("WeightedTransformationWithIndex record корректно работает")
    void testWeightedTransformationWithIndexRecord() {
        WeightedTransformation wt = new WeightedTransformation(null, 1.0, 0, 0, 0);
        int index = 2;

        RandomUtils.WeightedTransformationWithIndex record = new RandomUtils.WeightedTransformationWithIndex(wt, index);

        assertEquals(wt, record.transformation());
        assertEquals(index, record.index());
    }
}
