package com.losheckler.serviciodecafe;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;

@SpringBootApplication
public class ServicioDeCafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioDeCafeApplication.class, args);
    }
}

@RestController
@RequestMapping("/cafes")
class ControladorDeCafes {
    private final ServicioDeCafe servicio;

    ControladorDeCafes(ServicioDeCafe servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    Flux<Cafe> todos() {
        return servicio.todosLosCafes();
    }

    @GetMapping("/{id}")
    Mono<Cafe> cafe(@PathVariable String id) {
        return servicio.cafePorId(id);
    }

    @GetMapping(value = "/{id}/pedidos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<PedidoDeCafe> pedidos(@PathVariable String id) {
        return servicio.pedidos(id);
    }
}

@Service
class ServicioDeCafe {
    private final RepositorioDeCafe repo;

    ServicioDeCafe(RepositorioDeCafe repo) {
        this.repo = repo;
    }

    Flux<Cafe> todosLosCafes() {
        return repo.findAll();
    }

    Mono<Cafe> cafePorId(String id) {
        return repo.findById(id);
    }

    Flux<PedidoDeCafe> pedidos(String idDeCafe) {
        return Flux.<PedidoDeCafe>generate(sink -> sink.next(new PedidoDeCafe(idDeCafe, Instant.now())))
                .delayElements(Duration.ofSeconds(1));
    }
}

@Component
class CargadorDeDatos {
    private final RepositorioDeCafe repo;

    CargadorDeDatos(RepositorioDeCafe repo) {
        this.repo = repo;
    }

    @PostConstruct
    private void cargar() {
        repo.deleteAll().thenMany(
                Flux.just("Kaldi's Coffee", "Esmeralda Especial", "Café Quechua")
                        .map(Cafe::new)
                        .flatMap(repo::save))
                .thenMany(repo.findAll())
                .subscribe(System.out::println);
    }
}

interface RepositorioDeCafe extends ReactiveCrudRepository<Cafe, String> {
}

@Value
class PedidoDeCafe {
    private String idDeCafe;
    private Instant marcaDeTiempo;
}

@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class Cafe {
    @Id
    private String id;
    @NonNull
    private String nombre;
}