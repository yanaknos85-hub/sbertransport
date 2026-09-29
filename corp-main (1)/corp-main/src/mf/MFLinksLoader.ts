/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { MF_LINKS_LOADER_URL } from './constants';

const requestAppJson = async () => {
  try {
    const res = await fetch(MF_LINKS_LOADER_URL, {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache',
    });

    return res.text().then(data => data
      .replace(/\s/g, '')
      .replace(/'/g, '"')
    );
  } catch (err) {
    return Promise.reject(err);
  }
};

const parseAppJson = data => {
  try {
    const remotes = JSON.parse(data);
    // @ts-ignore
    window[process.env.REACT_APP_MF_LINK] = remotes;
    return Promise.resolve(remotes);
  } catch (err) {
    return Promise.reject(err);
  }
};

const success = data => {
  console.log('Remotes is loaded', data);
};

export {
  requestAppJson,
  parseAppJson,
  success
};
