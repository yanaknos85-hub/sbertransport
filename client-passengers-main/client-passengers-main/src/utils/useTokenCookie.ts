import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { jwtDecode } from './Misc';

const useToken = (): string | null => {
  const { authStore } = useAppStoreContext();
  return authStore.token;
};

export const useTokenCookieExpiration = (token: string, isSigma = !!1): number => {
  const { iat, exp }: any = jwtDecode(token);
  const expireTime = exp - iat - 30;

  if (isSigma) {
    const date = new Date();

    /* ПО ТРЕБОВАНИЮ, НА 120 МИН, ЭТО КОСТЫЛЬ И В ДАЛЬНЕЙШЕМ НАДО БУДЕТ МЕНЯТЬ(КОГДА БЕК ПРОСНЕТСЯ) */
    date.setTime(date.getTime() + expireTime * 8200);
    document.cookie = `token=${token};expires=${date.toUTCString()};path=/`;
  }

  return expireTime;
};

export default useToken;
