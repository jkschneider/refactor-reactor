package io.projectreactor.rewrite.version30x;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.parse.Parser;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Simon Baslé
 */
public class Migrate306To307Test {

	private Parser p = new OracleJdkParser();
	private Migrate306To307 migrate = new Migrate306To307();

	@Test
	public void migrateFlatmap() throws Exception {
		Tr.CompilationUnit a = p.parse("" +
				"import reactor.core.publisher.*;\n" +
				"public class A {\n" +
				"   public void foo() {\n" +
				"        Mono.just(1).flatMap(n -> Flux.just(n, n + 1));\n" +
				"        Mono.just(1).then();\n" +
				"        Mono.just(1).then(Mono.just(2));\n" +
				"        Mono.just(1).then(a -> Mono.just(a + 2));\n" +
				"   }\n" +
				"}");

		String refactored = migrate.migrateFlatmap(a).print();

		assertThat(refactored).isEqualTo("" +
						"import reactor.core.publisher.*;\n" +
						"public class A {\n" +
						"   public void foo() {\n" +
						"        Mono.just(1).flatMapMany(n -> Flux.just(n, n + 1));\n" +
						"        Mono.just(1).then();\n" +
						"        Mono.just(1).then(Mono.just(2));\n" +
						"        Mono.just(1).flatMap(a -> Mono.just(a + 2));\n" +
						"   }\n" +
						"}");
	}

}