package com.equipo.sistematareas.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.PrioridadTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.repository.TareaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class OperacionesTareaTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TareaRepository tareaRepository;

	private Long id;

	@BeforeEach
	void preparar() {
		tareaRepository.deleteAll();
		id = tareaRepository.save(new Tarea("Uno", "a", EstadoTarea.PENDIENTE, PrioridadTarea.ALTA, "Ana")).getId();
	}

	@Test
	void actualizarModificaLaTarea() throws Exception {
		mockMvc.perform(put("/api/tareas/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "Uno editada", "descripcion": "nueva", "estado": "EN_PROCESO",
								 "prioridad": "BAJA", "responsable": "Luis"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo", is("Uno editada")))
				.andExpect(jsonPath("$.estado", is("EN_PROCESO")))
				.andExpect(jsonPath("$.prioridad", is("BAJA")))
				.andExpect(jsonPath("$.responsable", is("Luis")));
	}

	@Test
	void actualizarInexistenteResponde404() throws Exception {
		mockMvc.perform(put("/api/tareas/{id}", 99999)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "Nadie", "descripcion": "", "estado": "PENDIENTE",
								 "prioridad": "MEDIA", "responsable": "Ana"}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void eliminarQuitaLaTarea() throws Exception {
		mockMvc.perform(delete("/api/tareas/{id}", id))
				.andExpect(status().isNoContent());

		assertFalse(tareaRepository.existsById(id));
		assertEquals(0, tareaRepository.count());
	}

	@Test
	void eliminarInexistenteResponde404() throws Exception {
		mockMvc.perform(delete("/api/tareas/{id}", 99999))
				.andExpect(status().isNotFound());
	}

	@Test
	void cambiarEstadoPrioridadYResponsable() throws Exception {
		mockMvc.perform(patch("/api/tareas/{id}/estado", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"estado\": \"TERMINADA\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado", is("TERMINADA")));

		mockMvc.perform(patch("/api/tareas/{id}/prioridad", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"prioridad\": \"MEDIA\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.prioridad", is("MEDIA")));

		mockMvc.perform(patch("/api/tareas/{id}/responsable", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"responsable\": \"  Marta  \"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.responsable", is("Marta")));
	}
}
