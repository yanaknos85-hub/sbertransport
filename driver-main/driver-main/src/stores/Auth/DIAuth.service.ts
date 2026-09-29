import { inject, injectable } from 'inversify';

import {
  AUTH, SUDIR_LOGIN, SUDIR_LOGOUT
} from 'constants/auth.constants';

import { IAuthService, AuthResponse } from './auth.interfaces';
import { TYPES } from 'stores/stores.types';
import type { IConfigStore } from 'stores/Config/Config.interface';
import type { IResponseService } from 'stores/Http/Response.service';
import type { IHttpService } from 'stores/Http/http.interface';
import { MOCKED_API_PREFIX } from 'api/constants';

@injectable()
export class DIAuthService implements IAuthService {
  @inject(TYPES.IConfigStore)
  private readonly configStore!: IConfigStore;

  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: IResponseService;

  private apiPrefix(): string {
    return this.configStore.isMockedAuth ? MOCKED_API_PREFIX : '';
  }

  login(login: string, password: string): Promise<AuthResponse> {
    const url = this.configStore.isBasicAuth || this.configStore.isMockedAuth ? `${this.apiPrefix()}${AUTH}/login` : SUDIR_LOGIN;

    return this.http
      .post<AuthResponse>(url, {}, { headers: { Authorization: `Basic ${login}:${password}` } })
      .then(this.process.getResponseData);
  }

  code(code: string, token: string): Promise<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${AUTH}/code`, { code }, { headers: { Authorization: `Bearer ${token}` } })
      .then(this.process.getResponseData);
  }

  logout = async (): Promise<void> => {
    const url = this.configStore.isBasicAuth || this.configStore.isMockedAuth ? `${AUTH}/logout` : SUDIR_LOGOUT;

    await this.http.get(url);
  };

  updateRefreshToken(refreshToken: string): Promise<AuthResponse> {
    const url = this.configStore.isBasicAuth || this.configStore.isMockedAuth ? `${AUTH}/login` : SUDIR_LOGIN;

    return this.http
      .post<AuthResponse>(url, {}, { headers: { Authorization: `Token ${refreshToken}` } })
      .then(this.process.getResponseData);
  }

  updatePassword = async (credentials: string): Promise<void> => {
    await this.http
      .post(`${AUTH}/changePassword`, {},
        { headers: { 'Authorization': 'Basic ', 'x-changePassword': `Basic ${credentials}` } }
      );
  };

  requestResetPasswordCode = async (login: string, channel: string): Promise<void> => {
    await this.http
      .post(`${AUTH}/ownership-code`, { login, channel }, { headers: { Authorization: 'none' } });
  };

  resetPassword = async (login: string, code: string): Promise<void> => {
    await this.http
      .put(`${AUTH}/confirmation`, { login }, { headers: { 'x-code': code, 'Authorization': 'none' } });
  };
}
