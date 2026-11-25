package com.biblioteca.notificacion.infraestructure.entry_points;

import com.biblioteca.notificacion.domain.model.PlantillaNotificacion;
import com.biblioteca.notificacion.domain.usecase.PlantillaUseCase;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.MensajeResponse;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.PlantillaRequest;
import com.biblioteca.notificacion.infraestructure.entry_points.dto.PlantillaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/api/biblioteca/notificaciones/plantillas",
        "/notificaciones/plantillas"
})
@RequiredArgsConstructor
public class PlantillaController {

    private final PlantillaUseCase plantillaUseCase;

    @GetMapping
    public ResponseEntity<List<PlantillaResponse>> listarPlantillas() {
        return ResponseEntity.ok(plantillaUseCase
                .listarPlantillas()
                .stream()
                .map(PlantillaResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaResponse> buscarPlantilla(@PathVariable String id) {
        return ResponseEntity.ok(PlantillaResponse.from(
                plantillaUseCase.buscarPlantilla(id)));
    }

    @PostMapping
    public ResponseEntity<PlantillaResponse> crearPlantilla(
            @Valid @RequestBody PlantillaRequest request) {

        PlantillaNotificacion plantilla = aPlantilla(request);

        return new ResponseEntity<>(
                PlantillaResponse.from(plantillaUseCase.crearPlantilla(plantilla)),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantillaResponse> actualizarPlantilla(
            @PathVariable String id,
            @Valid @RequestBody PlantillaRequest request) {

        return ResponseEntity.ok(PlantillaResponse.from(
                plantillaUseCase.actualizarPlantilla(id, aPlantilla(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarPlantilla(@PathVariable String id) {
        plantillaUseCase.eliminarPlantilla(id);

        return ResponseEntity.ok(new MensajeResponse("Plantilla eliminada con exito"));
    }

    private PlantillaNotificacion aPlantilla(PlantillaRequest request) {
        PlantillaNotificacion plantilla = new PlantillaNotificacion();
        plantilla.setNombre(request.nombre());
        plantilla.setAsunto(request.asunto());
        plantilla.setContenido(request.contenido());
        plantilla.setTipo(request.tipo());
        plantilla.setEstado(request.estado());

        return plantilla;
    }
}
