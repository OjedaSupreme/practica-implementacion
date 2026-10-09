package com.equipo.sistematareas.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.service.TareaService;

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
}
