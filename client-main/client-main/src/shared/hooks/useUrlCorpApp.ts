import { useMemo } from 'react';

import { StoreNames, useAppStore } from 'stores';
import { CORP_URL, HOST_NAME } from 'constants/constants.app';

export const useUrlCorpApp = () => {
  const { [StoreNames.configStore]: configStore } = useAppStore();
  const IS_SDO = configStore.env.IS_SDO;

  const urlOfCorpApp = useMemo(() => {
    if (IS_SDO) return CORP_URL.SDO_DEFAULT;

    if (HOST_NAME.includes('sbertransport.delta')) return CORP_URL.ADMIN_DELTA;
    if (HOST_NAME.includes('sbertransport.ca')) return CORP_URL.ADMIN_ALFA;
    if (HOST_NAME.includes('sbertransport.sigma')) return CORP_URL.ADMIN_SIGMA;

    return CORP_URL.ADMIN_DEFAULT;
  }, [IS_SDO]);

  return { urlOfCorpApp };
};
