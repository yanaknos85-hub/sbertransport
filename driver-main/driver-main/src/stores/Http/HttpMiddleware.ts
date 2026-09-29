import axios, {
  AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse
} from 'axios';
import { inject, injectable } from 'inversify';
import { IStringifyOptions, stringify } from 'qs';

import { CustomErrorCode, errorText } from 'constants/app.constants';
import type { IConfigStore } from '../../stores/Config/Config.interface';
import { IHttpService, IServerErrorResponseData } from './http.interface';
import { TYPES } from 'stores/stores.types';
import { getErrorCode } from 'utils/getErrorCode';
import { routes } from 'constants/routes.constants';
import { Toast } from 'antd-mobile';
import { API_URL, X_CLIENT_TYPE } from 'api/constants';

@injectable()
export abstract class HttpMiddleware implements IHttpService {
  private readonly _promise: PromiseConstructor;

  protected readonly client: AxiosInstance;

  // @inject(TYPES.IConfigStore)
  // private configStore!: IConfigStore;

  // @inject(TYPES.INavigator)
  // private _navigator!: INavigator;

  // @inject(TYPES.Token)
  // private _token!: Token;

  constructor(
    // @ts-ignore
    @inject(TYPES.IConfigStore) private configStore: IConfigStore,
    // @ts-ignore
    @inject(TYPES.INavigator) private _navigator: INavigator,
    // @ts-ignore
    @inject(TYPES.Token) private _token: Token
  ) {
    this._promise = Promise;
    this.client = axios.create({
      baseURL: API_URL, timeout: 10000, headers: { 'x-client-type': X_CLIENT_TYPE },
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const errorMiddleware = (error: Error | AxiosError<IServerErrorResponseData>): any => {
      // Логика обработки ошибок при авторизации находится в authStore и компонентах, поэтому тут пропускаем
      if (window.location.pathname === '/oauth') {
        return this._promise.reject(error);
      }

      const code = getErrorCode(error);

      // Если выелезла ошибка авторизации, когда мы находились внутри приложения
      if (code === 401) {
        window.location.href = routes.Auth;
      }

      const isLogToConsole = 'isAxiosError' in error && !error.config?.hush?.includes(code);

      if (isLogToConsole) {
        // eslint-disable-next-line no-console
        console.error(error.message);
      }

      // Выводим уведомление об ошибке только для post, put, patch, delete, т.к. get 100% будет отловлен errorBoundary
      // и нет смысла дублировать ошибку в уведомлении
      const methodsForNotifications = ['POST', 'post', 'PUT', 'put', 'PATCH', 'patch', 'DELETE', 'delete'];
      const notifyOnError = isLogToConsole && error.config?.notifyOnError && methodsForNotifications.includes(error.config?.method ?? '');

      if (notifyOnError) {
        Toast.show({
          content: `${code}: ${errorText[code]?.title ?? errorText[CustomErrorCode.UNKNOWN]?.title}`,
          position: 'top',
          icon: 'fail',
        });
      }

      return this._promise.reject(error);
    };

    this.client.interceptors.response.use(this.responseMiddlware.bind(this), errorMiddleware);
  }

  public changeClientType(clientType: string): void {
    this.client.defaults.headers['x-client-type'] = clientType;
  }

  public setParamsSerializer(options?: IStringifyOptions): void {
    this.client.defaults.paramsSerializer = params => stringify(params, options);
  }

  // eslint-disable-next-line @stylistic/max-len
  public setRequestInterceptor(middleware: (config: AxiosRequestConfig) => Promise<AxiosRequestConfig>) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    this.client.interceptors.request.use(middleware as any);
  }

  private responseMiddlware(response: AxiosResponse): Promise<AxiosResponse> {
    return this._promise.resolve(response);
  }

  public abstract get<T>(url: string, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;

  public abstract post<T>(
    url: string,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data: Record<string, any>,
    params?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>>;

  public abstract postFormData<T>(
    url: string,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data: Record<string, any>,
    params?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>>;

  public abstract put<T>(
    url: string,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data: Record<string, any>,
    params?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>>;

  public abstract delete<T>(url: string, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;

  public abstract patch<T>(
    url: string,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data: Record<string, any>,
    params?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>>;

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public abstract request<T = any>(config: AxiosRequestConfig): Promise<AxiosResponse<T>>;
}
