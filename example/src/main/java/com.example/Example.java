import reactor.core.publisher.*;

public class Example {
    public void foo() {
        Mono.just(1).flatMap(n -> Flux.just(n, n + 1));
        Mono.just(1).then();
        Mono.just(1).then(Mono.just(2));
        Mono.just(1).then(a -> Mono.just(a + 2));

        Flux.just(1).switchOnError(Mono.just(2));
        Flux.just(1).switchOnError(IllegalArgumentException.class, Mono.just(2));
        Flux.just(1).switchOnError(e -> true, Mono.just(2));
    }
}