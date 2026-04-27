package academy;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class StudentNameBenchmark {

    private Student student;

    private Method nameMethod;
    private MethodHandle nameHandle;
    private NameGetter nameGetter;

    @FunctionalInterface
    public interface NameGetter {
        String get(Student s);
    }

    @Setup
    public void setup() throws Throwable {
        student = new Student("Alice", 20);

        nameMethod = Student.class.getMethod("name");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        nameHandle = lookup.findVirtual(Student.class, "name", MethodType.methodType(String.class));

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "get",
                MethodType.methodType(NameGetter.class),
                MethodType.methodType(String.class, Student.class),
                nameHandle,
                MethodType.methodType(String.class, Student.class));

        nameGetter = (NameGetter) callSite.getTarget().invokeExact();
    }

    @Benchmark
    public void direct(Blackhole bh) {
        bh.consume(student.name());
    }

    @Benchmark
    public void reflection(Blackhole bh) throws Exception {
        bh.consume((String) nameMethod.invoke(student));
    }

    @Benchmark
    public void methodHandles(Blackhole bh) throws Throwable {
        bh.consume((String) nameHandle.invokeExact(student));
    }

    @Benchmark
    public void lambdaMetafactory(Blackhole bh) {
        bh.consume(nameGetter.get(student));
    }
}
