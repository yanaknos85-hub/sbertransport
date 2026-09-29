import { AxiosRequestConfig, AxiosResponse, Canceler } from 'axios';
import { IStringifyOptions } from 'qs';

declare module 'axios' {
  interface AxiosRequestConfig {
    urlParams?: Record<string, string>;
    unstoppable?: boolean;
    hush?: number[];
    notifyOnError?: boolean;
  }
}

export interface IServerErrorResponseData {
  error: string;
  reason: string;
  message: string;
  status: number;
  timestamp: string;
}

export interface IHttpService {
  changeClientType(clientType: string): void;
  setParamsSerializer(options?: IStringifyOptions): void;
  // eslint-disable-next-line @stylistic/max-len
  setRequestInterceptor: (middleware: (config: AxiosRequestConfig) => Promise<AxiosRequestConfig>) => void;
  get<T>(url: string, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  post<T>(url: string, data: Record<string, any>, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  postFormData<T>(
    url: string,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    data: Record<string, any>,
    params?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  put<T>(url: string, data: Record<string, any>, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  delete<T>(url: string, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  patch<T>(url: string, data: Record<string, any>, params?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  request<T = any>(config: AxiosRequestConfig): Promise<AxiosResponse<T>>;
}

export type RequestCanceler = Canceler;
export type RequestCancelerSetter = (cancel: RequestCanceler) => void;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type RequestParams = Record<string, any>;
export type Requester<T> = (params: RequestParams, cancelerSetter?: RequestCancelerSetter) => Promise<T>;
