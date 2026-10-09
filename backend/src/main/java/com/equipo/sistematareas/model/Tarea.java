package com.equipo.sistematareas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tareas")
public class Tarea {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String titulo;

	@Column(length = 1000)
	private String descripcion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoTarea estado;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PrioridadTarea prioridad;

	@Column(nullable = false)
	private String responsable;

	public Tarea() {
	}

	public Tarea(String titulo, String descripcion, EstadoTarea estado, PrioridadTarea prioridad, String responsable) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.estado = estado;
		this.prioridad = prioridad;
		this.responsable = responsable;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public EstadoTarea getEstado() {
		return estado;
	}

	public void setEstado(EstadoTarea estado) {
		this.estado = estado;
	}

	public PrioridadTarea getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(PrioridadTarea prioridad) {
		this.prioridad = prioridad;
	}

	public String getResponsable() {
		return responsable;
	}

	public void setResponsable(String responsable) {
		this.responsable = responsable;
	}
}
