const { getMfShared } = require('@sber-sbertransport/tool-kit/build/mf/utils');
const path = require('path');
const remotes = require('./remotes');

const { REACT_APP_NAME = 'main', REACT_APP_DEBUG, REACT_APP_REMOTE, REACT_APP_NETWORK_LOOP, REACT_APP_BASIC_AUTH } = process.env;
const IS_DEBUG = REACT_APP_DEBUG === 'TRUE';
const IS_REMOTE = REACT_APP_REMOTE === 'TRUE';
const IS_BASIC_AUTH = REACT_APP_BASIC_AUTH === 'TRUE';
const networkLoop = REACT_APP_NETWORK_LOOP;
const isBasicAuth = IS_BASIC_AUTH;

const devServerPort = 3000;

if (!REACT_APP_NAME) {
  throw new Error('set REACT_APP_NAME in package.json!')
}

const shared = getMfShared(false);

// Дефолтный конфиг Module Federation.
// Все настройки прописываются тут!
const MF_DEFAULT_CONFIG = {
  name: REACT_APP_NAME,
  filename: `${REACT_APP_NAME}.js`,
  remotes: {
    ...remotes,
  },
  shared: {
    ...shared,
  }
}

// Конфиг Module Federation
// Этот файл создается и перезаписывается автоматически в корне проекта. Не надо его трогать руками!
// Отличие от дефолтного только в том, что в случае ПРОДа в нем прописываются ремоуты который затаскивааются из /apps.json
const mfConfigFile = 'mf.json';
const mfConfigPath = path.join(process.cwd(), mfConfigFile);
const MF_CONFIG = require(mfConfigPath);

module.exports = {
  IS_DEBUG,
  IS_REMOTE,
  networkLoop,
  isBasicAuth,
  devServerPort,
  mfConfigPath,
  MF_CONFIG,
  MF_DEFAULT_CONFIG,
}
