package io.projectreactor.rewrite.version31x;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.parse.OracleJdkParser;
import com.netflix.rewrite.parse.Parser;
import io.projectreactor.rewrite.version30x.Migrate306To307;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Simon Baslé
 */
public class Migrate30xTo310Test {

	private Parser          p       = new OracleJdkParser();
	private Migrate30xTo310 migrate = new Migrate30xTo310();

	@Test
	public void migrateMonoThenAndFlatmap() throws Exception {
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

		String refactored = migrate.migrateMonoThenAndFlatmap(a).print();

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

	@Test
	public void migrateFluxSwitchOnError() throws Exception {
		Tr.CompilationUnit a = p.parse("" +
				"import reactor.core.publisher.*;\n" +
				"public class A {\n" +
				"   public void foo() {\n" +
				"        Flux.just(1).flatMap(n -> Flux.just(n, n + 1));\n" +
				"        Flux.just(1).then();\n" +
				"        Flux.just(1).switchOnError(Mono.just(2));\n" +
				"        Flux.just(1).switchOnError(IllegalArgumentException.class, Mono.just(2));\n" +
				"        Flux.just(1).switchOnError(e -> true, Mono.just(2));\n" +
				"   }\n" +
				"}");

		String refactored = migrate.migrateErrorHandlingOperators(a).print();

		assertThat(refactored).isEqualTo("" +
				"import reactor.core.publisher.*;\n" +
				"public class A {\n" +
				"   public void foo() {\n" +
				"        Flux.just(1).flatMap(n -> Flux.just(n, n + 1));\n" +
				"        Flux.just(1).then();\n" +
				"        Flux.just(1).onErrorResume(t -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(IllegalArgumentException.class, t -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(e -> true, t -> Mono.just(2));\n" +
				"   }\n" +
				"}");
	}

	@Test
	public void migrateErrorHandlingOperators() throws Exception {
		Tr.CompilationUnit a = p.parse("" +
				"import reactor.core.publisher.*;\n" +
				"public class A {\n" +
				"   public void foo() {\n" +
				"        Mono.just(1).otherwise(e -> Mono.just(2));\n" +
				"        Mono.just(1).otherwise(IllegalArgumentException.class, e -> Mono.just(2));\n" +
				"        Mono.just(1).otherwise(e -> true, e -> Mono.just(2));\n" +
				"        Mono.just(1).otherwiseReturn(2);\n" +
				"        Mono.just(1).otherwiseReturn(IllegalArgumentException.class, 2);\n" +
				"        Mono.just(1).otherwiseReturn(e -> true, 2);\n" +
				"        Mono.just(1).otherwiseIfEmpty(Mono.just(2));\n" +
				"        Mono.just(1).mapError(e -> e);\n" +
				"        Mono.just(1).mapError(IllegalArgumentException.class, e -> e);\n" +
				"        Mono.just(1).mapError(e -> true, e -> e);\n" +
				"        Flux.just(1).onErrorResumeWith(e -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResumeWith(IllegalArgumentException.class, e -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResumeWith(e -> true, e -> Mono.just(2));\n" +
				"        Flux.just(1).switchOnError(Mono.just(2));\n" +
				"        Flux.just(1).switchOnError(IllegalArgumentException.class, Mono.just(2));\n" +
				"        Flux.just(1).switchOnError(e -> true, Mono.just(2));\n" +
				"        Flux.just(1).mapError(e -> e);\n" +
				"        Flux.just(1).mapError(IllegalArgumentException.class, e -> e);\n" +
				"        Flux.just(1).mapError(e -> true, e -> e);\n" +
				"   }\n" +
				"}");

		String refactored = migrate.migrateErrorHandlingOperators(a).print();

		assertThat(refactored).isEqualTo("" +
				"import reactor.core.publisher.*;\n" +
				"public class A {\n" +
				"   public void foo() {\n" +
				"        Mono.just(1).onErrorResume(e -> Mono.just(2));\n" +
				"        Mono.just(1).onErrorResume(IllegalArgumentException.class, e -> Mono.just(2));\n" +
				"        Mono.just(1).onErrorResume(e -> true, e -> Mono.just(2));\n" +
				"        Mono.just(1).onErrorReturn(2);\n" +
				"        Mono.just(1).onErrorReturn(IllegalArgumentException.class, 2);\n" +
				"        Mono.just(1).onErrorReturn(e -> true, 2);\n" +
				"        Mono.just(1).switchIfEmpty(Mono.just(2));\n" +
				"        Mono.just(1).onErrorMap(e -> e);\n" +
				"        Mono.just(1).onErrorMap(IllegalArgumentException.class, e -> e);\n" +
				"        Mono.just(1).onErrorMap(e -> true, e -> e);\n" +
				"        Flux.just(1).onErrorResume(e -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(IllegalArgumentException.class, e -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(e -> true, e -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(t -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(IllegalArgumentException.class, t -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorResume(e -> true, t -> Mono.just(2));\n" +
				"        Flux.just(1).onErrorMap(e -> e);\n" +
				"        Flux.just(1).onErrorMap(IllegalArgumentException.class, e -> e);\n" +
				"        Flux.just(1).onErrorMap(e -> true, e -> e);\n" +
				"   }\n" +
				"}");
	}

}