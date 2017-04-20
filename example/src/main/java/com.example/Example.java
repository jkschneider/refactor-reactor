import reactor.core.publisher.*;

public class Example {
    public void foo() {
        Mono.just(1).flatMap(n -> Flux.just(n, n + 1));
        Mono.just(1).then();
        Mono.just(1).then(Mono.just(2));
        Mono.just(1).then(a -> Mono.just(a + 2));

        Flux.just(1).onErrorResume(t -> Mono.just(2));
        Flux.just(1).onErrorResume(IllegalArgumentException.class, t -> Mono.just(2));
        Flux.just(1).onErrorResume(e -> true, t -> Mono.just(2));
    }
}