package com.equipo.sistematareas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.repository.TareaRepository;

@Service
public class TareaService {

	private final TareaRepository tareaRepository;

	public TareaService(TareaRepository tareaRepository) {
		this.tareaRepository = tareaRepository;
	}

	public List<Tarea> listar(EstadoTarea estado, String responsable) {
		String responsableLimpio = responsable == null ? "" : responsable.trim();
		boolean tieneEstado = estado != null;
		boolean tieneResponsable = !responsableLimpio.isEmpty();

		if (tieneEstado && tieneResponsable) {
			return tareaRepository.findByEstadoAndResponsableIgnoreCaseOrderByIdAsc(estado, responsableLimpio);
		}
		if (tieneEstado) {
			return tareaRepository.findByEstadoOrderByIdAsc(estado);
		}
		if (tieneResponsable) {
			return tareaRepository.findByResponsableIgnoreCaseOrderByIdAsc(responsableLimpio);
		}
		return tareaRepository.findAllByOrderByIdAsc();
	}
}
