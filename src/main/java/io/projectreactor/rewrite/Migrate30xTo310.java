package io.projectreactor.rewrite;

import com.netflix.rewrite.ast.Expression;
import com.netflix.rewrite.ast.Tr;
import com.netflix.rewrite.auto.AutoRewrite;
import com.netflix.rewrite.refactor.Refactor;

import java.util.List;

/**
 * Migrate Reactor code using 3.0.x releases to code more adapted to 3.1.0.RELEASE,
 * taking into account methods that were deprecated during 3.0.x and removed in 3.1.0.
 *
 * @author Simon Baslé
 */
public class Migrate30xTo310 {

	@AutoRewrite(value = "reactor-mono-then-flatmap", description = "change flatMap to flatMapMany and some then calls to flatMap")
	public static Tr.CompilationUnit migrateMonoThenAndFlatmap(Refactor refactor) {
		Tr.CompilationUnit cu = refactor.getOriginal();

		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Mono flatMap(..)"),
				"flatMapMany");

		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Mono then(java.util.function.Function)"),
				"flatMap");

		return refactor.fix();
	}

	@AutoRewrite(value = "reactor-error-handling-operators", description = "migrate error handling operators")
	public static Tr.CompilationUnit migrateErrorHandlingOperators(Refactor refactor) {
		Tr.CompilationUnit cu = refactor.getOriginal();

		//MONO
		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Mono otherwise(..)"),
				"onErrorResume");
		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Mono otherwiseReturn(..)"),
				"onErrorReturn");
		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Mono otherwiseIfEmpty(..)"),
				"switchIfEmpty");
		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Mono mapError(..)"),
				"onErrorMap");

		//FLUX
		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Flux onErrorResumeWith(..)"),
				"onErrorResume");
		refactor.changeMethodName(cu.findMethodCalls("reactor.core.publisher.Flux mapError(..)"),
				"onErrorMap");

		//FLUX switchOnError is a bit more tricky as it necessitates introducing a lambda
		for (Tr.MethodInvocation mi : cu.findMethodCalls("reactor.core.publisher.Flux switchOnError(..)")) {
			final List<Expression> args = mi.argExpressions();
			int argSize = args.size();
			int pos = argSize <=2 ? argSize - 1 : -1;

			if (pos >= 0) {
				String publisher = args.get(pos).printTrimmed();
				String lambda = "t -> " + publisher;
				refactor.deleteArgument(mi, pos);
				refactor.insertArgument(mi, pos, lambda);
				refactor.changeMethodName(mi, "onErrorResume");
			}
		}

		return refactor.fix();
	}
}
