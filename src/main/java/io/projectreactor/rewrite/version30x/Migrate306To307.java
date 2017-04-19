package io.projectreactor.rewrite.version30x;

import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.refactor.Refactor;
import io.projectreactor.rewrite.AbstractMigrate;

/**
 * Migrate Reactor code using 3.0.6.RELEASE to code adapted to 3.0.7.RELEASE.
 *
 * @author Simon Baslé
 */
public class Migrate306To307 extends AbstractMigrate {

	public Tr.CompilationUnit migrateFlatmap(Tr.CompilationUnit a) {
		Refactor refactor = a.refactor();

		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono flatMap(..)"),
				"flatMapMany");

		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono then(java.util.function.Function)"),
				"flatMap");

		return refactor.fix();
	}

}
