package com.equipo.sistematareas.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.PrioridadTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.repository.TareaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class FiltroTareasTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TareaRepository tareaRepository;

	@BeforeEach
	void preparar() {
		tareaRepository.deleteAll();
		tareaRepository.save(new Tarea("Uno", "a", EstadoTarea.PENDIENTE, PrioridadTarea.ALTA, "Ana"));
		tareaRepository.save(new Tarea("Dos", "b", EstadoTarea.EN_PROCESO, PrioridadTarea.MEDIA, "Luis"));
		tareaRepository.save(new Tarea("Tres", "c", EstadoTarea.PENDIENTE, PrioridadTarea.BAJA, "Ana"));
		tareaRepository.save(new Tarea("Cuatro", "d", EstadoTarea.TERMINADA, PrioridadTarea.MEDIA, "Marta"));
	}

	@Test
	void responsableConEspaciosSeRecorta() throws Exception {
		mockMvc.perform(get("/api/tareas").param("responsable", "  ana  "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].responsable", everyItem(is("Ana"))));
	}

	@Test
	void responsableEnBlancoNoFiltra() throws Exception {
		mockMvc.perform(get("/api/tareas").param("responsable", "   "))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));
	}

	@Test
	void combinacionSinCoincidenciasDevuelveListaVacia() throws Exception {
		mockMvc.perform(get("/api/tareas")
						.param("estado", "PENDIENTE")
						.param("responsable", "Luis"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void filtraPorEstadoTerminada() throws Exception {
		mockMvc.perform(get("/api/tareas").param("estado", "TERMINADA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].titulo", is("Cuatro")))
				.andExpect(jsonPath("$[0].responsable", is("Marta")));
	}
}
