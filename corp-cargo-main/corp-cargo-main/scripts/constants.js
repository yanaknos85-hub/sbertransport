const { getMfShared } = require('@sber-sbertransport/tool-kit/build/mf/utils');

const exposes = require('./exposes');
const remotes = require('./remotes');

const { REACT_APP_NAME, REACT_APP_DEBUG, REACT_APP_REMOTE, REACT_APP_NETWORK_LOOP, REACT_APP_BASIC_AUTH } = process.env;
const IS_DEBUG = REACT_APP_DEBUG === 'TRUE';
const IS_REMOTE = REACT_APP_REMOTE === 'TRUE';
const IS_BASIC_AUTH = REACT_APP_BASIC_AUTH === 'TRUE';
const networkLoop = REACT_APP_NETWORK_LOOP;
const isBasicAuth = IS_BASIC_AUTH;

const devServerPort = 4002;

// Изменение входного пути webpack для дев разработки
const webpackDevEntryPath = 'app/dev/index.dev.ts';

if (!REACT_APP_NAME) {
  throw new Error('set REACT_APP_NAME in package.json!')
}

const shared = getMfShared(IS_REMOTE);

// Конфиг Module Federation
const MF_CONFIG = {
  name: REACT_APP_NAME,
  filename: `${REACT_APP_NAME}.js`,
  exposes,
  ...(!IS_REMOTE ? {
    remotes,
  } : {}),
  shared: {
    ...shared,
  }
}

module.exports = {
  IS_DEBUG,
  IS_REMOTE,
  networkLoop,
  isBasicAuth,
  devServerPort,
  MF_CONFIG,
  webpackDevEntryPath,
}
