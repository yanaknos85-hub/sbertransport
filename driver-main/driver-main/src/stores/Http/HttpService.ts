import type { AxiosRequestConfig, AxiosResponse } from 'axios';

import { HttpMiddleware } from './HttpMiddleware';
import { Final } from 'utils/decorators';

export class HttpService extends HttpMiddleware {
  @Final
  public get<T>(url: string, params: AxiosRequestConfig = {}): Promise<AxiosResponse<T>> {
    return this.client.get<T>(url, params);
  }

  @Final
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public post<T = any>(
    url: string,
    data: object = {},
    params: AxiosRequestConfig = {}
  ): Promise<AxiosResponse<T>> {
    return this.client.post<T>(url, data, params);
  }

  @Final
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public postFormData<T = any>(
    url: string,
    data: object = {},
    params: AxiosRequestConfig = {}
  ): Promise<AxiosResponse<T>> {
    const paramsFormData: AxiosRequestConfig = {
      ...params,
      headers: {
        ...params.headers,
        ...(!(params.headers && 'Content-Type' in params.headers) && {
          'Content-Type': 'multipart/form-data; boundary="boundary"',
        }),
      },
    };

    return this.client.post<T>(url, data, paramsFormData);
  }

  @Final
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public put<T = any>(
    url: string,
    data: object = {},
    params: AxiosRequestConfig = {}
  ): Promise<AxiosResponse<T>> {
    return this.client.put<T>(url, data, params);
  }

  @Final
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public delete<T = any>(url: string, params: AxiosRequestConfig = {}): Promise<AxiosResponse<T>> {
    return this.client.delete<T>(url, params);
  }

  @Final
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public patch<T = any>(
    url: string,
    data: object = {},
    params: AxiosRequestConfig = {}
  ): Promise<AxiosResponse<T>> {
    return this.client.patch<T>(url, data, params);
  }

  @Final
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public request<T = any>(config: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.client.request(config);
  }
}
