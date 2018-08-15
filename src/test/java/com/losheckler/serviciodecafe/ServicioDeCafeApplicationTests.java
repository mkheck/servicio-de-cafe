package com.losheckler.serviciodecafe;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServicioDeCafeApplicationTests {
	@Autowired
    private ServicioDeCafe servicio;

    @Test
    public void tomar10Pedidos() {
        String idDeCafe = servicio.todosLosCafes().blockFirst().getId();

        StepVerifier.withVirtualTime(() -> servicio.pedidos(idDeCafe).take(10))
                .thenAwait(Duration.ofHours(10))
                .expectNextCount(10)
                .verifyComplete();
    }

}
