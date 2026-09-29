import {
  useEffect, useState, Dispatch, SetStateAction
} from 'react';
import { PageSetting } from 'stores/CargoRegistry/CargoRegistry.interface';

const defaultPageSettings: PageSetting = {
  page: 0,
  size: 10,
};

export const usePagination = (
  initialPageSettings = defaultPageSettings
): {
  pageSetting: PageSetting;
  setPageSetting: Dispatch<SetStateAction<PageSetting>>;
} => {
  const [pageSetting, setPageSetting] = useState<PageSetting>(initialPageSettings);
  useEffect(() => {
    setPageSetting({ ...initialPageSettings });
  }, [setPageSetting, initialPageSettings]);

  return {
    pageSetting,
    setPageSetting,
  };
};
