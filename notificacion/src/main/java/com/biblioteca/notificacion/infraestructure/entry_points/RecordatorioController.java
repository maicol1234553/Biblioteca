package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.model.Recordatorio;
import com.biblioteca.notificacion.domain.usecase.RecordatorioUseCase;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.RecordatorioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/api/biblioteca/notificaciones/recordatorios",
        "/notificaciones/recordatorios"
})
@RequiredArgsConstructor
public class RecordatorioController {

    private final RecordatorioUseCase recordatorioUseCase;

    @GetMapping
    public ResponseEntity<List<RecordatorioResponse>> listarRecordatorios() {
        return ResponseEntity.ok(recordatorioUseCase
                .listarRecordatorios()
                .stream()
                .map(RecordatorioResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecordatorioResponse> buscarRecordatorio(@PathVariable String id) {
        return ResponseEntity.ok(RecordatorioResponse.from(
                recordatorioUseCase.buscarRecordatorio(id)));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<RecordatorioResponse>> listarPorUsuario(
            @PathVariable String idUsuario) {

        return ResponseEntity.ok(recordatorioUseCase
                .listarPorUsuario(idUsuario)
                .stream()
                .map(RecordatorioResponse::from)
                .toList());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<RecordatorioResponse>> listarPendientes() {
        return ResponseEntity.ok(recordatorioUseCase
                .listarPendientes()
                .stream()
                .map(RecordatorioResponse::from)
                .toList());
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<RecordatorioResponse> cancelarRecordatorio(@PathVariable String id) {
        return ResponseEntity.ok(RecordatorioResponse.from(
                recordatorioUseCase.cancelarRecordatorio(id)));
    }

    @PostMapping("/procesar")
    public ResponseEntity<List<RecordatorioResponse>> procesarRecordatorios() {
        List<Recordatorio> procesados = recordatorioUseCase.procesarRecordatoriosPendientes();

        return ResponseEntity.ok(procesados
                .stream()
                .map(RecordatorioResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarRecordatorio(@PathVariable String id) {
        recordatorioUseCase.eliminarRecordatorio(id);

        return ResponseEntity.ok(new MensajeResponse("Recordatorio eliminado con exito"));
    }
}
