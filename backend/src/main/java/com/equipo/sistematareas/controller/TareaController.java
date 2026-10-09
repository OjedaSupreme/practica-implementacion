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

	@GetMapping
	public List<Tarea> listar(
			@RequestParam(required = false) EstadoTarea estado,
			@RequestParam(required = false) String responsable) {
		return tareaService.listar(estado, responsable);
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
