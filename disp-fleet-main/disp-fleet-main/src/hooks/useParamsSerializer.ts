import { useEffect } from 'react';
import { useAppStore } from 'ioc';

/** Хук для установки функции сериализации параметров во все запросы axios */
const useParamsSerializer = () => {
  const { http } = useAppStore();

  useEffect(() => {
    http.setParamsSerializer({ arrayFormat: 'repeat' });
  }, [http]);
};

export default useParamsSerializer;
