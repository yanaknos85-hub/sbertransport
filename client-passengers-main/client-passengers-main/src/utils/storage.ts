/* eslint-disable react-hooks/rules-of-hooks */
import { useTokenCookieExpiration } from './useTokenCookie';

export enum TokenStorage {
  token = 'token',
  refreshToken = 'refreshToken',
  sudirLogoutTweet = 'sudirLogout',
  sudirReAuthAmt = 'sudirReAuthAmt',
}

const tokenStorage: Storage = (window as Window).localStorage;
const sessStorage: Storage = (window as Window).sessionStorage;

export const getToken = (): string => tokenStorage.getItem(TokenStorage.token) || '';

export const setToken = (token: string): void => {
  tokenStorage.setItem(TokenStorage.token, token);
};

export const removeToken = (): void => {
  tokenStorage.removeItem(TokenStorage.token);
};

export const getRefreshToken = (): string => tokenStorage.getItem(TokenStorage.refreshToken) || '';

export const setRefreshToken = (token: string): void => {
  tokenStorage.setItem(TokenStorage.refreshToken, token);
};

export const removeRefreshToken = (): void => {
  tokenStorage.removeItem(TokenStorage.refreshToken);
};

export const setSudirLogoutTweet = (): void => {
  tokenStorage.setItem(TokenStorage.sudirLogoutTweet, 'done');
};

export const getSudirLogoutTweet = (): string => tokenStorage.getItem(TokenStorage.sudirLogoutTweet) || '';

export const removeSudirLogoutTweet = (): void => {
  tokenStorage.removeItem(TokenStorage.sudirLogoutTweet);
};

export const setSudirReAuthAmt = (amt: number): void => {
  sessStorage.setItem(TokenStorage.sudirReAuthAmt, `${amt}`);
};

export const getSudirReAuthAmt = (): number => +(sessStorage.getItem(TokenStorage.sudirReAuthAmt) || 0);

export const removeSudirReAuthAmt = (): void => {
  sessStorage.removeItem(TokenStorage.sudirReAuthAmt);
};

/* Это все временное решение, пока бек не починит обновление/апи на своей стороне */
export const getExpireToken = (): string => {
  const token = document.cookie.match(new RegExp('(^| )token=([^;]+)'));
  return token ? token[2] : '';
};

export const setExpireToken = (
  token: string,
  firstJwt = !!1
): ((fallBack: () => void, sudirAuthFallBack: (refreshToken: string) => void) => void) => {
  const expireTime = useTokenCookieExpiration(token);

  return (fallBack: () => void, sudirAuthFallBack: (refreshToken: string) => void): void => {
    if (!firstJwt) {
      sudirAuthFallBack(getRefreshToken());
    }

    const expiredInterval = setInterval((): void => {
      if (getExpireToken() && getRefreshToken()) {
        sudirAuthFallBack(getRefreshToken());
      } else {
        fallBack();
        clearInterval(expiredInterval);
      }
    }, expireTime * 1000);
  };
};

// Не проходит SAST! <Client_DOM_Cookie_Poisoning>
// export const clearCookie = (): void => {
//   document.cookie.split(';').forEach((cookie: string): void => {
//     document.cookie = cookie.replace(/^ +/, '').replace(/=.*/, `=;expires=${new Date().toUTCString()};path=/`);
//   });
// };

export const clearTokenCookie = (): void => {
  document.cookie = `${TokenStorage.token}=;expires=${new Date().toUTCString()};path=/`;
};

export const clear = (): void => {
  removeSudirLogoutTweet();
  removeToken();
  removeRefreshToken();
  clearTokenCookie();
};
