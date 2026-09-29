import { useMemo } from 'react';
import { useAppStore, StoreNames } from 'stores';
import { SDO_CLIENT_URL } from 'constants/constants.app';

export const useAdminMenu = (): {
  toClient: string;
} => {
  const {
    [StoreNames.configStore]: configStore,
    [StoreNames.authStore]: authStore,
  } = useAppStore();
  const IS_SDO = configStore.env.IS_SDO;

  const toClient = useMemo(() => {
    const url = window.location.origin.split('.').slice(1).join('.');

    if (IS_SDO) {
      if (!url) return SDO_CLIENT_URL;

      const finalUrl = new URL(`${window.location.protocol}//client-ext.${url}`);
      finalUrl.searchParams.set('refreshToken', authStore.refreshToken);

      return finalUrl.toString();
    }

    // example http://webcorp.test.transportonline.info/employees
    const http = 'https:';

    return `${http}//${url}/`;
  }, [IS_SDO, authStore.refreshToken]);

  return {
    toClient,
  };
};
