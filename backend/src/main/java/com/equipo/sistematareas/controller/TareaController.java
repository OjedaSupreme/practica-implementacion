package com.equipo.sistematareas.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.service.TareaService;

import java.util.Map;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

	private final TareaService tareaService;

	public TareaController(TareaService tareaService) {
		this.tareaService = tareaService;
	}

	/**
	 * Consulta las tareas y permite filtrar opcionalmente por estado y responsable.
	 *
	 * @param estado estado por el que se desea filtrar
	 * @param responsable responsable por el que se desea filtrar
	 * @return tareas encontradas, o una lista vacía si no hay coincidencias
	 */
	@GetMapping
	public List<Tarea> listar(
			@RequestParam(required = false) EstadoTarea estado,
			@RequestParam(required = false) String responsable) {
		return tareaService.listar(estado, responsable);
	}

	/**
	 * Consulta una tarea por su identificador.
	 *
	 * @param id identificador de la tarea
	 * @return tarea encontrada con estado HTTP 200
	 * @throws org.springframework.web.server.ResponseStatusException si la tarea no existe (HTTP 404)
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Tarea> consultarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(tareaService.consultarPorId(id));
	}

	@PostMapping
	public ResponseEntity<Tarea> crear(@RequestBody Tarea datos) {
		Tarea creada = tareaService.crear(datos);
		return ResponseEntity.created(URI.create("/api/tareas/" + creada.getId())).body(creada);
	}

	@PutMapping("/{id}")
	public Tarea actualizar(@PathVariable Long id, @RequestBody Tarea datos) {
		return tareaService.actualizar(id, datos);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		tareaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/estado")
	public Tarea actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
		return tareaService.actualizarEstado(id, body.get("estado"));
	}

	@PatchMapping("/{id}/prioridad")
	public Tarea actualizarPrioridad(@PathVariable Long id, @RequestBody Map<String, String> body) {
		return tareaService.actualizarPrioridad(id, body.get("prioridad"));
	}

	@PatchMapping("/{id}/responsable")
	public Tarea asignar(@PathVariable Long id, @RequestBody Map<String, String> body) {
		return tareaService.asignar(id, body.get("responsable"));
	}
}
