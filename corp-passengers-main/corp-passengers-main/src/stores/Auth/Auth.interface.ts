/* eslint-disable no-use-before-define */
/* eslint-disable @typescript-eslint/no-explicit-any */
export enum STEPS {
  password,
  code,
}

export interface IAuthStore {
  token: string | null;
  isAuthenticated: boolean;
  isAwaiting: boolean;
  transportPassword: boolean;
  step: STEPS;

  setRefreshToken: (refreshToken: string) => any;
  setAccessToken: (accessToken: string) => any;
  setStep: (step: STEPS) => any;

  updateRefreshToken: (refreshToken: string) => Promise<void>;
  updateAccessToken: (accessToken: string) => void;
  login: (login: string, password: string) => void;
  code: (code: string) => void;
  logout: () => void;
  sudir: (url: string) => Promise<void>;
  goToAuthPage?: boolean;
}

export interface IAuthService {
  login: (login: string, password: string) => Promise<AuthResponse>;
  code: (code: string, token: string) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  updateRefreshToken: (refreshToken: string) => Promise<AuthResponse>;
}

export interface AuthResponseDefault {
  token: string;
  refreshToken: string;
  transferPassword: false;
}

export interface AuthResponseTransport {
  token: string;
  transferPassword: true;
}

export type AuthResponse = AuthResponseDefault | AuthResponseTransport;
export interface ITokenContent {
  roles: IRole[];
  exp: number;
  iss: string;
  iat: number;
  sub: string;
}

export interface IRole {
  code: string;
  default: boolean;
}
