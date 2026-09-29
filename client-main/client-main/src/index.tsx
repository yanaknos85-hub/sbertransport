import { requestAppJson, parseAppJson } from 'mf/MFLinksLoader';
import { debug, error } from 'mf/debug';

const boot = () => import('./bootstrap');

if (process.env.NODE_ENV !== 'development' || process.env.REACT_APP_DEV_AS_PROD === 'TRUE') {
  requestAppJson()
    .then(parseAppJson)
    .then(debug)
    .then(boot)
    .catch(error);
} else {
  debug()
    .then(boot)
    .catch(error);
}

export {};
