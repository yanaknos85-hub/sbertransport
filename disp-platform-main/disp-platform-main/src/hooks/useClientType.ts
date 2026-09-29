import { useAppStore } from 'ioc';
import { useEffect } from 'react';

const X_CLIENT_TYPE = 'DISPATCHER';

/** Хук для установки заголовка X-Client-Type во все запросы axios */
const useClientType = () => {
  const { http } = useAppStore();

  useEffect(() => {
    http.changeClientType(X_CLIENT_TYPE);
  }, [http]);
};

export default useClientType;
