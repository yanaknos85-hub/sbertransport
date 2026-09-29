import { useEffect } from 'react';
import { useAppStoreContext } from './useAppStoreContext';
import { X_CLIENT_TYPE } from 'constants/constants.app';

/** Хук для установки заголовка X-Client-Type во все запросы аксиос */
const useClientType = () => {
  const { http } = useAppStoreContext();

  useEffect(() => {
    http.changeClientType(X_CLIENT_TYPE);
  }, [http]);
};

export default useClientType;
