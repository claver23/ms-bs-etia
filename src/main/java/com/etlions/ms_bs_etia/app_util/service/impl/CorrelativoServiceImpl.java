package com.etlions.ms_bs_etia.app_util.service.impl;

import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoEntity;
import com.etlions.ms_bs_etia.app_util.persistence.CorrelativoRepository;
import com.etlions.ms_bs_etia.app_util.service.CorrelativoService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class CorrelativoServiceImpl implements CorrelativoService {

    private static final int MAX_REINTENTOS = 3;

    private final CorrelativoRepository correlativoRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    public int siguienteNumero(String companyId, String serie) {
        RuntimeException ultimoError = null;

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                return siguienteNumeroEnNuevaTransaccion(companyId, serie);
            } catch (DataIntegrityViolationException | PessimisticLockingFailureException ex) {
                ultimoError = ex;
            }
        }

        throw new IllegalStateException(
                "No fue posible generar correlativo para companyId=%s, serie=%s tras %d intentos"
                        .formatted(companyId, serie, MAX_REINTENTOS),
                ultimoError);
    }

    private int siguienteNumeroEnNuevaTransaccion(String companyId, String serie) {
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Integer result = txTemplate.execute(status -> {
            CorrelativoEntity correlativo = correlativoRepository
                    .findByCompanyIdAndSerieForUpdate(companyId, serie)
                    .orElseGet(() -> crearOuObtenerConLock(companyId, serie));

            int siguiente = correlativo.getUltimoNumero() + 1;
            correlativo.setUltimoNumero(siguiente);
            correlativoRepository.saveAndFlush(correlativo);
            return siguiente;
        });

        if (result == null) {
            throw new IllegalStateException("No se pudo calcular el siguiente correlativo");
        }
        return result;
    }

    private CorrelativoEntity crearOuObtenerConLock(String companyId, String serie) {
        try {
            CorrelativoEntity created = correlativoRepository.saveAndFlush(
                    CorrelativoEntity.builder()
                            .companyId(companyId)
                            .serie(serie)
                            .ultimoNumero(0)
                            .build());
            return correlativoRepository.findById(created.getId()).orElse(created);
        } catch (DataIntegrityViolationException ex) {
            // Otra transacción creó la fila en paralelo: volver a leer con lock.
            return correlativoRepository
                    .findByCompanyIdAndSerieForUpdate(companyId, serie)
                    .orElseThrow(() -> ex);
        }
    }
}
