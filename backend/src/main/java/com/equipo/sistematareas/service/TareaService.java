package com.equipo.sistematareas.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.PrioridadTarea;
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

	public Tarea consultarPorId(Long id) {
		return tareaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
	}

	public Tarea actualizar(Long id, Tarea datos) {
		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
		tarea.setTitulo(datos.getTitulo());
		tarea.setDescripcion(datos.getDescripcion());
		tarea.setEstado(datos.getEstado());
		tarea.setPrioridad(datos.getPrioridad());
		tarea.setResponsable(datos.getResponsable());
		return tareaRepository.save(tarea);
	}

	public void eliminar(Long id) {
		if (!tareaRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada");
		}
		tareaRepository.deleteById(id);
	}

	public Tarea actualizarEstado(Long id, String nuevoEstado) {
		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
		tarea.setEstado(EstadoTarea.valueOf(nuevoEstado));
		return tareaRepository.save(tarea);
	}

	public Tarea actualizarPrioridad(Long id, String nuevaPrioridad) {
		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
		tarea.setPrioridad(PrioridadTarea.valueOf(nuevaPrioridad));
		return tareaRepository.save(tarea);
	}

	public Tarea asignar(Long id, String nuevoResponsable) {
		Tarea tarea = tareaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
		tarea.setResponsable(nuevoResponsable.trim());
		return tareaRepository.save(tarea);
	}
}
