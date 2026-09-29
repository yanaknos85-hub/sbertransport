const path = require('path');

const { REACT_APP_NAME = 'main', REACT_APP_DEBUG, REACT_APP_NETWORK_LOOP, REACT_APP_BASIC_AUTH } = process.env;
const IS_DEBUG = REACT_APP_DEBUG === 'TRUE';
const IS_BASIC_AUTH = REACT_APP_BASIC_AUTH === 'TRUE';
const networkLoop = REACT_APP_NETWORK_LOOP;
const isBasicAuth = IS_BASIC_AUTH;

const devServerPort = 8000;

if (!REACT_APP_NAME) {
  throw new Error('set REACT_APP_NAME in package.json!')
}

module.exports = {
  IS_DEBUG,
  networkLoop,
  isBasicAuth,
  devServerPort,
}
