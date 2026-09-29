export const REQUESTS = 'request-cargo';
export const CARGO = 'cargo';

export const EXCHANGE = 'exchange';
export const CARGO_EXCHANGE = 'cargo-exchange';

export const EXCHANGE_HISTORY = `${CARGO_EXCHANGE}/${EXCHANGE}/:rqUuid/status/history`;
export const EXCHANGE_EVALUATION = `${CARGO_EXCHANGE}/${EXCHANGE}/evaluation/:rqUuid`;

export const EXCHANGE_AVAILABLE = `${CARGO_EXCHANGE}/${EXCHANGE}/:type`;
export const EXCHANGE_DETAILED = `${CARGO_EXCHANGE}/${EXCHANGE}/:id`;

export const EXCHANGE_ACCEPT = `${CARGO_EXCHANGE}/${EXCHANGE}/:id/accept`;
export const EXCHANGE_DENY = `${CARGO_EXCHANGE}/${EXCHANGE}/:id/deny`;
export const EXCHANGE_UPDATE_STATUS = `${CARGO_EXCHANGE}/${EXCHANGE}/:id/status/:status`;

export const EXCHANGE_FILTERS = `${CARGO_EXCHANGE}/${EXCHANGE}/filters`;
export const EXCHANGE_FILTERS_SAVE = `${CARGO_EXCHANGE}/${EXCHANGE}/filter`;
