export interface TokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  username: string;
  rol: 'ADMIN' | 'USUARIO';
}

export interface Sesion {
  username: string;
  rol: 'ADMIN' | 'USUARIO';
  accessToken: string;
  expiraEn: number; // milisegundos epoch
}
