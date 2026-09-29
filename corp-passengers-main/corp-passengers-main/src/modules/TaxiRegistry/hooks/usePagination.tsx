import { useCallback, useState } from 'react';
import { PageSetting } from '../types/types';

export const usePagination = (): {
  pageSetting: PageSetting;
  onPaginationChange: (page: number, size?: number | undefined) => void;
  setPageSetting(setting: PageSetting): void;
  initialPageSettings: PageSetting;
  resetPagination(): void;
} => {
  const initialPageSettings = { page: 0, size: 100 };

  const [pageSetting, setPageSetting] = useState<PageSetting>(initialPageSettings);
  const resetPagination = () => {
    setPageSetting(initialPageSettings);
  };
  const onPaginationChange = useCallback(
    (page: number, size?: number) => {
      setPageSetting({ page: page - 1, size: size || initialPageSettings.size });
    },
    [setPageSetting, initialPageSettings.size]
  );

  return {
    pageSetting,
    onPaginationChange,
    setPageSetting,
    initialPageSettings,
    resetPagination,
  };
};
