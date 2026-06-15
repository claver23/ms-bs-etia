package com.etlions.ms_bs_etia.app_util.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CorrelativoServiceConcurrencyTest {

    @Autowired
    private CorrelativoService correlativoService;

    @Test
    void debeGenerarCorrelativosUnicosYSecuencialesEnConcurrencia() throws Exception {
        final int totalSolicitudes = 40;
        final ExecutorService executor = Executors.newFixedThreadPool(totalSolicitudes);
        final CountDownLatch ready = new CountDownLatch(totalSolicitudes);
        final CountDownLatch start = new CountDownLatch(1);

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < totalSolicitudes; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(20, TimeUnit.SECONDS));
                return correlativoService.siguienteNumero("cmp-concurrencia", "F999");
            }));
        }

        assertTrue(ready.await(20, TimeUnit.SECONDS));
        start.countDown();

        Set<Integer> numeros = new HashSet<>();
        for (Future<Integer> future : futures) {
            numeros.add(future.get(30, TimeUnit.SECONDS));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));

        assertEquals(totalSolicitudes, numeros.size());
        List<Integer> ordenados = numeros.stream().sorted().toList();
        for (int i = 1; i <= totalSolicitudes; i++) {
            assertEquals(i, ordenados.get(i - 1));
        }
    }
}
