export interface Tarea {
  id: number;
  titulo: string;
  descripcion: string;
  estado: string;
  prioridad: string;
  responsable: string;
}

export type NuevaTarea = Omit<Tarea, 'id'>;

export const ESTADOS = [
  { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
  { valor: 'EN_PROCESO', etiqueta: 'En proceso' },
  { valor: 'TERMINADA', etiqueta: 'Terminada' },
];

export const PRIORIDADES = [
  { valor: 'BAJA', etiqueta: 'Baja' },
  { valor: 'MEDIA', etiqueta: 'Media' },
  { valor: 'ALTA', etiqueta: 'Alta' },
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
