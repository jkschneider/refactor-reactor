import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.parse.Parser;
import com.netflix.rewrite.refactor.Refactor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


public class RefactorReactorTest {
    Parser p = new OracleJdkParser();

    @Test
    public void replaceThenAndFlatMap() {
        // Typically, we would operate on a collection of Tr.CompilationUnits, one per source file in the entire codebase
        Tr.CompilationUnit a = p.parse("" +
                "import reactor.core.publisher.*;\n" +
                "public class A {\n" +
                "   public void foo() {\n" +
                "        Mono.just(1)     .flatMap(n -> Flux.just(n, n + 1));\n" +
                "        Mono.just(1)    .then();\n" +
                "        Mono.just(1)  .then(Mono.just(2));\n" +
                "   }\n" +
                "}");

        Refactor refactor = a.refactor();

        // It is possible to do much more sophisticated things like re-ordering, inserting, removing, and transforming arguments.
        // Refactoring operations aren't limited to method invocation changes.
        refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono flatMap(..)"),
                "flatMapMany");

        refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono then(..)"),
                "flatMap");

        assertEquals("" +
                        "import reactor.core.publisher.*;\n" +
                        "public class A {\n" +
                        "   public void foo() {\n" +
                        "        Mono.just(1)     .flatMapMany(n -> Flux.just(n, n + 1));\n" +
                        "        Mono.just(1)    .flatMap();\n" +
                        "        Mono.just(1)  .flatMap(Mono.just(2));\n" +
                        "   }\n" +
                        "}",
                refactor.fix().print());

        System.out.println(refactor.diff());
    }

    @BeforeClass
    public static void setup() {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);
    }
}
