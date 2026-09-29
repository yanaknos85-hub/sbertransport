import axios, { AxiosError, AxiosRequestConfig } from 'axios';
import { inject, injectable } from 'inversify';

import {
  action,
  computed,
  observable,
  reaction
} from 'mobx';

import {
  AuthSteps,
  SYSTEM_MESSAGES,
  AuthErrorTypes
} from 'constants/auth.constants';

import type {
  IAuthStore,
  IAuthService,
  AuthResponse,
  AuthResponseDefault
} from './auth.interfaces';
import {
  AuthSessionStorage, containsValidISO88591, getErrorMessage, ignore, jwtDecode
} from 'utils';
import { TYPES } from 'stores/stores.types';
import type { IConfigStore } from 'stores/Config/Config.interface';
import { Token } from 'stores/Token/token';
import { HttpService } from 'stores/Http/HttpService';
import {
  getRefreshToken, getToken, removeRefreshToken, removeToken, setRefreshToken, setSudirLogoutTweet
} from 'utils/storage/storage';
import { routes } from 'constants/routes.constants';
import { isNotDirectEnter } from 'utils/routing/isNotRedirectEnter';
import { logger } from 'utils/logger/logger';

function checkFirstUser(token = ''): boolean {
  const { roles } = jwtDecode<{ roles: string[] }>(token);

  return token ? roles.includes('ROLE_INITIAL_USER') : false;
}

@injectable()
export class DIAuthStore implements IAuthStore {
  @inject(TYPES.IConfigStore)
  private readonly configStore!: IConfigStore;

  @inject(TYPES.IAuthService)
  private readonly service!: IAuthService;

  @inject(TYPES.Token)
  private readonly tokenStore!: Token;

  @inject(TYPES.IHttpService)
  private readonly httpService!: HttpService;

  private fact2Token: string | undefined;

  @computed
  get isBasicAuth(): boolean {
    return !!this.configStore.isBasicAuth;
  }

  @computed
  get isMockedAuth(): boolean {
    return !!this.configStore.isMockedAuth;
  }

  @computed
  get history() {
    return this.configStore.history || null;
  }

  @computed
  get token(): string | null {
    return this.tokenStore.get();
  }

  set token(value: string | null) {
    this.tokenStore.set(value);
  }

  @observable
    refreshToken: string | null = null;

  @observable
  private awaitingAuth = false;

  @observable
  private isRefreshingToken = false;

  @observable
    transportPassword = false;

  @observable
    codeRequested = false;

  @observable
    passwordResetCodeAccepted = false;

  @computed
  get isAuthenticated(): boolean {
    return Boolean(this.token);
  }

  @computed
  get isAwaiting(): boolean {
    return this.awaitingAuth;
  }

  @observable
    goToAuthPage: boolean | undefined = undefined;

  private refreshTimeout?: NodeJS.Timeout;

  private refreshRequest: Promise<AuthResponse> | undefined;

  @observable
    step = AuthSteps.password;

  startAwaiting = (): void => {
    this.awaitingAuth = true;
  };

  stopAwaiting = (): void => {
    this.awaitingAuth = false;
  };

  @action.bound
    setRefreshToken = (refreshToken: string): void => {
      this.refreshToken = refreshToken;
    };

  @action.bound
    setAccessToken = (accessToken: string): void => {
      this.token = accessToken;
    };

  @action.bound
    resetData = (): void => {
      this.stopAwaiting();

      this.token = null;
      this.refreshToken = null;
      this.fact2Token = undefined;
      this.step = AuthSteps.password;

      removeToken();
      removeRefreshToken();

      if (this.refreshTimeout) {
        clearTimeout(this.refreshTimeout);
        delete this.refreshTimeout;
      }
    };

  @action.bound
    parseResponse = (response: AuthResponse): void => {
      this.fact2Token = undefined;
      this.step = AuthSteps.password;

      try {
        const isFirstUser = !response.transferPassword && !!response.token && checkFirstUser(response.token);
        const isRegularUser = !response.transferPassword && !!response.refreshToken && !!response.token;
        const isTranferUser = response.transferPassword;

        if (isTranferUser) {
          this.transportPassword = response.transferPassword;
        } else if (isRegularUser || isFirstUser) {
          const { token, refreshToken } = response as AuthResponseDefault;
          this.token = token;
          this.refreshToken = refreshToken;

          setRefreshToken(refreshToken);

          if (!this.isRefreshingToken) {
            logger('success', SYSTEM_MESSAGES.welcome);
          }
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(SYSTEM_MESSAGES.authFailed);
      }
    };

  @action.bound
    parseError = (error: AxiosError): void => {
      this.resetData();

      if (error.response?.data.type === AuthErrorTypes.TOO_MANY_LOGIN_TRIES) {
        throw { isLockoutAccount: true };
      }

      if (
        (error.response?.data.type === AuthErrorTypes.REFRESH_EXPIRED)
        || (error.response?.data.path === '/login' && error.response?.status === 500)
      ) {
        logger('fail', SYSTEM_MESSAGES.refreshExpired);
        return;
      }

      if (error.response?.status === 401) {
        logger('fail', SYSTEM_MESSAGES.loginFailed);
      } else {
        logger('fail', error.message);
      }
    };

  @action.bound
    parseCodeError = (error: AxiosError): void => {
      if (error.response?.status === 401) {
        logger('fail', SYSTEM_MESSAGES.codeFailed);
      } else {
        logger('fail', error.message);
      }
    };

  @action.bound
    parseResetPasswordCodeError = (error: AxiosError): void => {
      if (error.response?.status === 403) {
        logger('fail', SYSTEM_MESSAGES.resetFailed);
      } else if (error.response?.status === 404) {
        logger('fail', SYSTEM_MESSAGES.userNotFoundFailed);
      } else if (error.response?.status === 409) {
        logger('fail', SYSTEM_MESSAGES.serviceFailed);
      } else if (error.response?.status === 412) {
        logger('fail', SYSTEM_MESSAGES.connectionFailed);
      } else {
        logger('fail', error.message);
      }
    };

  @action.bound
    parseResetPasswordError = (error: AxiosError): void => {
      if (error.response?.status === 404) {
        logger('fail', SYSTEM_MESSAGES.incorrectLogin);
      } else if (error.response?.status === 409) {
        logger('fail', SYSTEM_MESSAGES.incorrectCode);
      } else {
        logger('fail', error.message);
      }
    };

  @action.bound
  checkFactor(response: AuthResponse): void {
    if (jwtDecode<{ factor: 'BASIC' | 'TWO_FA' }>(response.token).factor === 'TWO_FA') {
      this.step = AuthSteps.code;
      this.fact2Token = response.token;
    } else {
      this.parseResponse(response);
    }
  }

  async requestMiddleware(config: AxiosRequestConfig): Promise<AxiosRequestConfig> {
    config = await this.checkAuth(config);

    if (!config.url) {
      return config;
    }

    let pathname = config.url;
    Object.entries(config.urlParams || {}).forEach(([k, v]) => {
      pathname = pathname.replace(`:${k}`, encodeURIComponent(v));
    });

    return {
      ...config,
      notifyOnError: config.notifyOnError ?? true,
      url: pathname,
    };
  }

  async checkAuth(config?: AxiosRequestConfig) {
    const authHeader = config?.headers?.Authorization;
    const refreshToken = getRefreshToken();

    if (!authHeader && !refreshToken && !this.token) {
      throw new axios.Cancel('No Authorization Header');
    }

    const isNeedRefresh
      = !authHeader
      && ((refreshToken && this.refreshToken !== refreshToken)
      || (this.token && jwtDecode<{ exp: number }>(this.token).exp * 1000 <= Date.now()));

    if (isNeedRefresh) {
      await this.updateRefreshToken(getRefreshToken());
    }

    return {
      ...config,
      headers: {
        ...config?.headers,
        Authorization: authHeader || `Bearer ${this.token}`,
      },
    };
  }

  async login(login: string, password: string): Promise<void> {
    const isValidISO88591 = containsValidISO88591(`${login}:${password}`);

    if (!isValidISO88591) {
      logger('fail', SYSTEM_MESSAGES.authISOError);
      return;
    }

    this.startAwaiting();
    await this.service
      .login(login, password)
      .then(this.checkFactor)
      .catch(this.parseError)
      .finally(this.stopAwaiting);
  }

  code(code: string): void {
    if (!this.fact2Token) {
      throw new Error('Отсутствует первичный токен для аутентификации');
    }

    this.startAwaiting();

    this.service
      .code(code, this.fact2Token)
      .then(this.parseResponse.bind(this))
      .catch(this.parseCodeError.bind(this))
      .finally(this.stopAwaiting.bind(this));
  }

  @action
    parseUrl = (url: string): void => {
      try {
        const parsedUrlHash = new URL(url).hash;

        if (parsedUrlHash) {
          const accessToken = parsedUrlHash.substring(parsedUrlHash.indexOf('=') + 1, parsedUrlHash.lastIndexOf('&'));
          const refreshToken = parsedUrlHash.split('&refreshToken=')[1];
          if (accessToken && refreshToken) {
            this.token = accessToken;
            this.refreshToken = refreshToken;
            setRefreshToken(refreshToken);
          }
        }
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(SYSTEM_MESSAGES.authFailed);
      }
    };

  sudir = (url: string): Promise<void> => {
    const promise = new Promise((resolve, reject) => {
      this.parseUrl(url);
      if (this.refreshToken && this.token) {
        resolve(true);
      } else {
        reject();
      }
    });

    return promise
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      .then(() => {})
      .catch((): void => ignore());
  };

  logout(): Promise<void> {
    // console.log('Разлогирование...');
    return new Promise(resolve => {
      const dispose = reaction(
        () => this.isAwaiting,
        isAwaiting => {
          if (!isAwaiting) {
            this.service.logout().finally(() => {
              this.resetData();
              this.goToAuth(!this.isBasicAuth);
              dispose();
              resolve();
            });
          }
        },
        { fireImmediately: true }
      );
    });
  }

  @action.bound
  async updateRefreshToken(refreshToken: string): Promise<void> {
    this.isRefreshingToken = true;
    this.startAwaiting();

    if (!this.refreshRequest) {
      this.refreshRequest = this.service.updateRefreshToken(refreshToken);
    }

    await this.refreshRequest
      .then(this.parseResponse.bind(this))
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      .catch((error: any) => {
        this.parseError(error);
        if (this.history) {
          this.history.push(routes.Auth);
        }
      })
      .finally(() => {
        this.stopAwaiting();
        this.isRefreshingToken = false;
        this.refreshRequest = undefined;
      });
  }

  @action.bound
  updateAccessToken(token: string): void {
    this.token = token;
  }

  @action
  check(): void {
    // console.log(`Это ${this.isBasicAuth ? 'BASIC' : 'СУДИР'}${this.isMockedAuth ? ' MOCK!' : ''} авторизация`);
    // console.log('Чекаю авторизацию...');

    this.httpService.setRequestInterceptor(this.requestMiddleware.bind(this));

    this.token = getToken();
    this.refreshToken = getRefreshToken();

    const isSudirPage
      = window.location.pathname === routes.Success
      || window.location.pathname === routes.Failure
      || window.location.pathname === routes.SudirApi;

    if (this.isBasicAuth || !isSudirPage) {
      if (this.refreshToken) {
        this.updateRefreshToken(this.refreshToken);
      } else {
        this.goToAuth();
      }
    }
  }

  goToAuth(logoutHandler = false): void {
    const { pathname } = window.location;
    // console.log('Юзер не авторизoван');

    removeToken();
    removeRefreshToken();

    if (pathname !== routes.Auth && isNotDirectEnter(pathname)) {
      if (pathname.length > 1 && pathname !== routes.Logout && pathname !== routes.SudirApi) {
        AuthSessionStorage.set(AuthSessionStorage.Keys.previousPath, pathname);
      }

      if (logoutHandler) {
        setSudirLogoutTweet();
      }

      if (this.history) {
        // this.history.push(routes.Auth);
        window.location.pathname = routes.Auth;
      }
    }
  }

  updatePassword(credentials: string): Promise<void> {
    this.startAwaiting();

    return this.service
      .updatePassword(credentials)
      .then(() => {
        this.transportPassword = false;
        logger('success', 'Обновление пароля прошло успешно!');
      })
      .catch(error => {
        logger('fail', error ? getErrorMessage(error) : 'При обновлении пароля произошла ошибка!');

        throw new Error(error);
      })
      .finally(this.stopAwaiting);
  }

  requestResetPasswordCode(login: string, channel: string): void {
    this.startAwaiting();

    this.service
      .requestResetPasswordCode(login, channel)
      .then(() => {
        this.codeRequested = true;
      })
      .catch(this.parseResetPasswordCodeError)
      .finally(this.stopAwaiting);
  }

  resetPassword(login: string, code: string): void {
    this.startAwaiting();

    this.service
      .resetPassword(login, code)
      .then(() => {
        this.codeRequested = false;
        this.passwordResetCodeAccepted = true;
      })
      .catch(this.parseResetPasswordError)
      .finally(this.stopAwaiting);
  }
}
