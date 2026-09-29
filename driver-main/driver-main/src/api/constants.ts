import { _env } from 'utils';

export const X_CLIENT_TYPE = 'CLIENT';

export const NETWORK_LOOP = _env('REACT_APP_NETWORK_LOOP') || '';
export const IS_REMOTE = _env('REACT_APP_REMOTE') === 'TRUE';
export const IS_PROD = _env('PROD') === 'TRUE';
export const IS_DEV = _env('NODE_ENV') === 'development';

export const HOST = window.location.host;
export const IS_LOCAL = /localhost/.test(HOST);

const API_DOMAIN = 'api';
const API_PATH = 'api';
const API_SUBDOMAIN = HOST.replace(/^[^.]+\./g, '');

const EXCLUDED_DOMENS = ['sbertransport', 'nt', 'st', 'ift', 'psi', 'hf'];
const FIRST_DOMEN = HOST.split('.')[0];
const IS_EXECEPTION = EXCLUDED_DOMENS.includes(FIRST_DOMEN);

const IS_EXTERNAL = /sbertransport\.ru/.test(HOST);

// Хост в зависимости от стенда на котором запускается - заменяется на прямой адрес api
// client.ift.sbertransport.sigma.sbrf.ru -> заменяем api.ift.sbertransport.sigma.sbrf.ru
let apiUrl = `//${API_DOMAIN}.${API_SUBDOMAIN}/${API_PATH}`;

// Хост, начинающийся с названия домена из списка EXCLUDED_DOMENS - дополняется поддоменом api.
// sbertransport.sigma.sbrf.ru -> добавляем api.sbertransport.sigma.sbrf.ru
if (IS_EXECEPTION) {
  apiUrl = `//${API_DOMAIN}.${HOST}/${API_PATH}`;
}

// Хост ака Внешний, заканчивающийся на sbertransport.ru - не меняется!
// sbertransport.ru  -> не трогаем
// Локалхост также - не меняется и подчиняется правилам прокси дев сервера (scripts/devServer.js)
if (IS_EXTERNAL || IS_LOCAL) {
  apiUrl = `/${API_PATH}`;
}

export const API_URL = apiUrl;

export const MOCKED_API_PREFIX = 'mock/';

const WS_PROTOCOL = window.location.protocol.replace('http', 'ws');

export const WEBSOCKET_URL = IS_EXTERNAL || IS_LOCAL
  ? `${window.location.origin.replace('http', 'ws')}/${API_PATH}`
  : `${WS_PROTOCOL}//${API_DOMAIN}.${API_SUBDOMAIN}/${API_PATH}`;

export enum PingPong {
  Ping = 'PING',
  Pong = 'PONG',
}

export enum WEBSOCKET_CONNECTION_STATUS {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
}
