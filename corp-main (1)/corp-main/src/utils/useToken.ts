import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { jwtDecode } from './Misc';

export const useToken = (): string | null => {
  const { authStore } = useAppStoreContext();
  return authStore.token;
};

export const tokenCookieExpiration = (token: string, isSigma = !!1): number => {
  // @ts-ignore
  const { iat, exp } = jwtDecode(token);
  const expireTime = exp - iat - 30;

  if (isSigma) {
    const date = new Date();

    /* ПО ТРЕБОВАНИЮ, НА 120 МИН, ЭТО КОСТЫЛЬ И В ДАЛЬНЕЙШЕМ НАДО БУДЕТ МЕНЯТЬ(КОГДА БЕК ПРОСНЕТСЯ) */
    date.setTime(date.getTime() + expireTime * 8200);
    document.cookie = `token=${token};expires=${date.toUTCString()};path=/`;
  }

  return expireTime;
};
