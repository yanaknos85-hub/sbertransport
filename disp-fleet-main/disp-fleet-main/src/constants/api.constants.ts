export const IMPORT = 'import';
export const EXCEL = 'EXCEL';

export const SEARCH = 'search';
export const APPROVE = 'approve';
export const DECLINE = 'decline';
export const CANCEL = 'cancel';
export const FINISH = 'finish';
export const RATE = 'rate';
export const EDIT = 'edit';

const HOST = window.location.host;
const WS_PROTOCOL = window.location.protocol.replace('http', 'ws');

export const IS_LOCAL = /localhost/.test(HOST);
export const IS_EXTERNAL = /sbertransport\.ru/.test(HOST);

export const API_DOMAIN = 'api';
export const API_PATH = 'api';
export const API_SUBDOMAIN = HOST.replace(/^[^.]+\./g, '');

export const WEBSOCKET_URL = IS_EXTERNAL || IS_LOCAL ? `/${API_PATH}` : `${WS_PROTOCOL}//${API_DOMAIN}.${API_SUBDOMAIN}/${API_PATH}`;
export const PURE_API_URL = IS_EXTERNAL || IS_LOCAL ? '' : `//${API_DOMAIN}.${API_SUBDOMAIN}`;
