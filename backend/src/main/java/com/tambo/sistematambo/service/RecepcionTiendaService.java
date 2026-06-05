package com.tambo.sistematambo.service;

import com.tambo.sistematambo.dto.RecepcionTiendaRequest;
import com.tambo.sistematambo.response.TransferenciaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecepcionTiendaService {

    private final TransferenciaService transferenciaService;

    public RecepcionTiendaService(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @Transactional
    public TransferenciaResponse confirmar(RecepcionTiendaRequest request) {
        return transferenciaService.recibir(request.transferenciaId());
    }
}
