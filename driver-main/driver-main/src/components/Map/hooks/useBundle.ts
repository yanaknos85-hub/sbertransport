import { useEffect, useState } from 'react';
import { load } from '@2gis/mapgl';
import { Bundle } from '../types';

/** Хук для скачивания бандла 2gis */
export const useBundle = () => {
  const [bundle, setBundle] = useState<Bundle | undefined>();

  useEffect(() => {
    if (!bundle) {
      // eslint-disable-next-line no-console
      load().then(setBundle).catch(console.error);
    }
  }, [bundle]);

  return bundle;
};
