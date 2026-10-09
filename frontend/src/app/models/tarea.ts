export interface Tarea {
  id: number;
  titulo: string;
  descripcion: string;
  estado: string;
  prioridad: string;
  responsable: string;
}

export const ESTADOS = [
  { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
  { valor: 'EN_PROCESO', etiqueta: 'En proceso' },
  { valor: 'TERMINADA', etiqueta: 'Terminada' },
];

const ETIQUETAS: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  EN_PROCESO: 'En proceso',
  TERMINADA: 'Terminada',
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
};

export function etiqueta(valor: string): string {
  return ETIQUETAS[valor] ?? valor;
}
