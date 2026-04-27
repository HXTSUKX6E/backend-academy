package academy.sample;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public sealed class Person extends Human implements Named permits Employee {
    private String name;
    private int age;

    @Id
    private Long id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
