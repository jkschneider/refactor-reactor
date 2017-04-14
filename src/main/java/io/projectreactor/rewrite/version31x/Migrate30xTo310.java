package io.projectreactor.rewrite.version31x;

import java.util.Iterator;
import java.util.stream.Collectors;

import com.netflix.rewrite.ast.Expression;
import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.refactor.Refactor;
import io.projectreactor.rewrite.AbstractMigrate;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Migrate Reactor code using 3.0.x releases to code more adapted to 3.1.0.RELEASE,
 * taking into account methods that were deprecated during 3.0.x and removed in 3.1.0.
 *
 * @author Simon Baslé
 */
public class Migrate30xTo310 extends AbstractMigrate {

	public Tr.CompilationUnit migrateMonoThenAndFlatmap(Tr.CompilationUnit a) {
		Refactor refactor = a.refactor();

		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono flatMap(..)"),
				"flatMapMany");

		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono then(java.util.function.Function)"),
				"flatMap");

		return refactor.fix();
	}

	public Tr.CompilationUnit migrateErrorHandlingOperators(Tr.CompilationUnit a) {
		Refactor refactor = a.refactor();

		//MONO
		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono otherwise(..)"),
				"onErrorResume");
		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono otherwiseReturn(..)"),
				"onErrorReturn");
		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono otherwiseIfEmpty(..)"),
				"switchIfEmpty");
		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Mono mapError(..)"),
				"onErrorMap");

		//FLUX
		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Flux onErrorResumeWith(..)"),
				"onErrorResume");
		refactor.changeMethodName(a.findMethodCalls("reactor.core.publisher.Flux mapError(..)"),
				"onErrorMap");

		//FLUX switchOnError is a bit more tricky as it necessitates introducing a lambda
		for (Tr.MethodInvocation mi : a.findMethodCalls("reactor.core.publisher.Flux switchOnError(..)")) {
			int pos = -1;
			int argSize = mi.argExpressions().size();
			if (argSize == 2) {
				pos = 1;
			} else if (argSize == 1) {
				pos = 0;
			}

			if (pos >= 0) {
				String publisher = mi.argExpressions().get(pos).printTrimmed();
				String lambda = "t -> " + publisher;
				refactor.deleteArgument(mi, pos);
				refactor.insertArgument(mi, pos, lambda);
				refactor.changeMethodName(mi, "onErrorResume");
			}
		}

		return refactor.fix();
	}
}
