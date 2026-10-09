package com.equipo.sistematareas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.Tarea;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

	List<Tarea> findByEstadoOrderByIdAsc(EstadoTarea estado);

	List<Tarea> findByResponsableIgnoreCaseOrderByIdAsc(String responsable);

	List<Tarea> findByEstadoAndResponsableIgnoreCaseOrderByIdAsc(EstadoTarea estado, String responsable);

	List<Tarea> findAllByOrderByIdAsc();
}
