package com.equipo.sistematareas.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class TareaControllerTest {

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
	}

	@Test
	void sinFiltrosDevuelveTodasLasTareas() throws Exception {
		mockMvc.perform(get("/api/tareas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)));
	}

	@Test
	void filtraPorEstadoPendiente() throws Exception {
		mockMvc.perform(get("/api/tareas").param("estado", "PENDIENTE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].estado", everyItem(is("PENDIENTE"))));
	}

	@Test
	void filtraPorResponsable() throws Exception {
		mockMvc.perform(get("/api/tareas").param("responsable", "ana"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].responsable", everyItem(is("Ana"))));
	}

	@Test
	void combinaEstadoYResponsable() throws Exception {
		mockMvc.perform(get("/api/tareas")
						.param("estado", "EN_PROCESO")
						.param("responsable", "Luis"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].titulo", is("Dos")));
	}

	@Test
	void estadoInvalidoResponde400() throws Exception {
		mockMvc.perform(get("/api/tareas").param("estado", "URGENTE"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void registraTareaCompleta() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "  Cuatro  ", "descripcion": "d", "estado": "EN_PROCESO",
								 "prioridad": "ALTA", "responsable": " Marta "}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.titulo", is("Cuatro")))
				.andExpect(jsonPath("$.estado", is("EN_PROCESO")))
				.andExpect(jsonPath("$.prioridad", is("ALTA")))
				.andExpect(jsonPath("$.responsable", is("Marta")));

		assertEquals(4, tareaRepository.count());
	}

	@Test
	void registroSinEstadoNiPrioridadUsaValoresPorDefecto() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "Cinco", "responsable": "Ana"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.estado", is("PENDIENTE")))
				.andExpect(jsonPath("$.prioridad", is("MEDIA")));
	}

	@Test
	void registroIgnoraIdEnviado() throws Exception {
		Long idExistente = tareaRepository.findAllByOrderByIdAsc().get(0).getId();

		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id": %d, "titulo": "Seis", "responsable": "Ana"}
								""".formatted(idExistente)))
				.andExpect(status().isCreated());

		assertEquals(4, tareaRepository.count());
		assertEquals("Uno", tareaRepository.findById(idExistente).orElseThrow().getTitulo());
	}

	@Test
	void registroSinTituloResponde400() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "   ", "responsable": "Ana"}
								"""))
				.andExpect(status().isBadRequest());

		assertEquals(3, tareaRepository.count());
	}

	@Test
	void registroSinResponsableResponde400() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "Siete"}
								"""))
				.andExpect(status().isBadRequest());

		assertEquals(3, tareaRepository.count());
	}

	@Test
	void registroConPrioridadInvalidaResponde400() throws Exception {
		mockMvc.perform(post("/api/tareas")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"titulo": "Ocho", "responsable": "Ana", "prioridad": "URGENTE"}
								"""))
				.andExpect(status().isBadRequest());
	}
}
