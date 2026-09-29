import { FCC } from 'types/global';
import { useBundle } from '../hooks/useBundle';
import {
  createContext, useContext, useMemo
} from 'react';

/** Хук для получения value контекста */
const useHook = (key: string) => {
  const bundle = useBundle();

  return useMemo(() => ({ bundle, key }), [bundle, key]);
};

export const Map2GISContext = createContext<ReturnType<typeof useHook>>({} as ReturnType<typeof useHook>);

/**
 * @returns bundle - бандл 2gis для построения карты
 */
export const use2GIS = () => {
  const value = useContext(Map2GISContext);

  if (!Object.keys(value).length) {
    throw new Error('use2GIS must be inside a Map2GISProvider with a value');
  }

  return value;
};

export const Map2GISProvider: FCC<{ APIKey: string }> = ({ APIKey, children }) => {
  const value = useHook(APIKey);

  return <Map2GISContext.Provider value={value}>{children}</Map2GISContext.Provider>;
};
