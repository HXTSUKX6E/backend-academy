package academy.transformations;

import static org.junit.jupiter.api.Assertions.*;

import academy.transformations.impl.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransformationRegistryTest {

    @Test
    @DisplayName("Корректно возвращаются все зарегистрированные трансформации")
    void test1() {
        assertInstanceOf(LinearTransformation.class, TransformationRegistry.byName("linear"));
        assertInstanceOf(SinusoidalTransformation.class, TransformationRegistry.byName("sinusoidal"));
        assertInstanceOf(SphericalTransformation.class, TransformationRegistry.byName("spherical"));
        assertInstanceOf(SwirlTransformation.class, TransformationRegistry.byName("swirl"));
        assertInstanceOf(HorseshoeTransformation.class, TransformationRegistry.byName("horseshoe"));
        assertInstanceOf(CurlTransformation.class, TransformationRegistry.byName("curl"));
        assertInstanceOf(PolarTransformation.class, TransformationRegistry.byName("polar"));
        assertInstanceOf(DiscTransformation.class, TransformationRegistry.byName("disc"));
        assertInstanceOf(SpiralTransformation.class, TransformationRegistry.byName("spiral"));
    }

    @Test
    @DisplayName("Поиск трансформации не зависит от регистра")
    void test2() {
        Transformation t1 = TransformationRegistry.byName("SINUSOIDAL");
        Transformation t2 = TransformationRegistry.byName("sinusoidal");

        assertSame(t1, t2);
    }

    @Test
    @DisplayName("Registry возвращает один и тот же экземпляр трансформации")
    void test3() {
        Transformation t1 = TransformationRegistry.byName("swirl");
        Transformation t2 = TransformationRegistry.byName("swirl");

        assertSame(t1, t2);
    }

    @Test
    @DisplayName("Неизвестное имя трансформации вызывает исключение")
    void test4() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> TransformationRegistry.byName("unknown"));

        assertTrue(ex.getMessage().contains("Unknown transformation"));
    }

    @Test
    @DisplayName("Имя трансформации используется в нижнем регистре")
    void test5() {
        Transformation t = TransformationRegistry.byName("LiNeAr");
        assertNotNull(t);
    }
}
