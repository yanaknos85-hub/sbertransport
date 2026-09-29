import { injectable } from 'inversify';
import { action, observable } from 'mobx';
import { UNSAFE_createBrowserHistory as createBrowserHistory } from 'react-router';
import { IConfigStore } from './Config.interface';
import type { ENVConfig } from './Config.interface';
import { Toast } from 'antd-mobile';

const ENV_CONFIG_FILE_PATH = '/env.json';

const defaultEnvConfig: ENVConfig = {
  IS_SDO: false,
  DRIVER_APPS_URL: 'https://apps.sbertransport.ru',
};

@injectable()
export class DIConfigStore implements IConfigStore {
  @observable
    isBasicAuth = false;

  @observable
    isMockedAuth = false;

  @observable
    isMockedApi = false;

  @observable
    history = createBrowserHistory({ window });

  @observable
    env: ENVConfig = {} as ENVConfig;

  constructor() {
    this.fillEnvConfig();
    Toast.config({ duration: 5 * 1000 });
  }

  private fillEnvConfig() {
    fetch(ENV_CONFIG_FILE_PATH)
      .then(response => response.json())
      .then(this.parseEnv.bind(this))
      .catch(this.setDefaultConfig.bind(this));
  }

  private parseEnv(response: ENVConfig) {
    this.env = response;
  }

  private setDefaultConfig() {
    // eslint-disable-next-line no-console
    console.info('Не удалось загрузить env.json, установлен дефолтный конфиг');
    this.env = defaultEnvConfig;
  }

  @action
    setConfig = (config: Omit<Partial<IConfigStore>, 'history'>): void => {
      this.isBasicAuth = config.isBasicAuth || this.isBasicAuth;
      this.isMockedAuth = config.isMockedAuth || this.isMockedAuth;
      this.isMockedApi = config.isMockedApi || this.isMockedApi;
    };
}
