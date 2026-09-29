import { AxiosRequestConfig } from 'axios';
import { AuthSteps } from 'constants/auth.constants';

/* eslint-disable no-use-before-define */
export interface IAuthConfig {
  isBasicAuth: boolean;
  isMockedApi: boolean;
  isMockedAuth: boolean;
}

export interface IAuthStore {
  token: string | null;
  isAuthenticated: boolean;
  isAwaiting: boolean;
  transportPassword: boolean;
  step: AuthSteps;
  codeRequested: boolean;
  passwordResetCodeAccepted: boolean;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setRefreshToken: (refreshToken: string) => any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  setAccessToken: (accessToken: string) => any;
  updateRefreshToken: (refreshToken: string) => Promise<void>;
  updateAccessToken: (accessToken: string) => void;
  login: (login: string, password: string) => Promise<void>;
  code: (code: string) => void;
  logout: () => void;
  sudir: (url: string) => Promise<void>;
  goToAuthPage?: boolean;
  check: () => void;
  updatePassword: (credentials: string) => Promise<void>;
  requestResetPasswordCode: (login: string, channel: string) => void;
  resetPassword: (login: string, code: string) => void;
  checkAuth: (config?: AxiosRequestConfig) => Promise<AxiosRequestConfig>;
}

export interface IAuthService {
  login: (login: string, password: string) => Promise<AuthResponse>;
  code: (code: string, token: string) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  updateRefreshToken: (refreshToken: string) => Promise<AuthResponse>;
  updatePassword: (credentials: string) => Promise<void>;
  requestResetPasswordCode: (login: string, channel: string) => Promise<void>;
  resetPassword: (login: string, code: string) => Promise<void>;
}

export interface AuthResponseDefault {
  token: string;
  refreshToken: string;
  transferPassword: false;
}

export interface AuthResponseTransport {
  transferPassword: true;
  token: string;
}

export type AuthResponse = AuthResponseDefault | AuthResponseTransport;
