package academy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

public class SampleTest {
    record Student(String name, int grade) {}

    @Test
    void student_shouldHaveCorrectNameAndGrade() {
        // Arrange
        Student student = Instancio.create(Student.class);

        // Act
        String name = student.name();
        int grade = student.grade();

        // Assert
        assertThat(name).isEqualTo(student.name());
        assertThat(grade).isEqualTo(student.grade());
    }
}
