import { UNSAFE_createBrowserHistory as createBrowserHistory } from 'react-router';

export interface ENVConfig {
  /** Является ли ДЗО */
  IS_SDO: boolean;
  /** Ссылка на приложения водителей */
  DRIVER_APPS_URL: string;
}

export interface IConfigStore {
  isBasicAuth: boolean;
  isMockedAuth: boolean;
  isMockedApi: boolean;
  env: ENVConfig;
  history: ReturnType<typeof createBrowserHistory>;
  setConfig(config: Partial<IConfigStore>): void;
}
