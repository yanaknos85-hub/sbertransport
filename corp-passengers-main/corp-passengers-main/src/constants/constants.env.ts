import { _env } from 'utils';

export const APP_NAME = _env('REACT_APP_NAME');

export const IS_REMOTE = _env('REACT_APP_REMOTE') === 'TRUE';

export const IS_DEV = _env('NODE_ENV') === 'development';

export const NETWORK_LOOP = _env('REACT_APP_NETWORK_LOOP') || '';

export const BASIC_AUTH = _env('REACT_APP_BASIC_AUTH') || '';
export const IS_BASIC_AUTH = BASIC_AUTH === 'TRUE';

export const MOCKED_API = _env('REACT_APP_MOCKED_API') || '';
export const IS_MOCKED_API = MOCKED_API === 'TRUE';

export const MOCKED_AUTH = _env('REACT_APP_MOCKED_AUTH') || '';
export const IS_MOCKED_AUTH = MOCKED_AUTH === 'TRUE';

export const MOCKED_API_PREFIX = IS_MOCKED_API ? 'mock/' : '';
export const MOCKED_AUTH_PREFIX = IS_MOCKED_AUTH ? 'mock/' : '';
