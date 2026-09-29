import React from 'react';

export interface PageSetting {
  page: number;
  size: number;
}

export const usePagination = (initialPageSetting = { page: 0, size: 5 }): {
  pageSetting: PageSetting;
  setPageSetting(setting: PageSetting): void;
  onPaginationChange(page: number, size?: number | undefined): void;
  resetPagination(): void;
} => {
  const [pageSetting, setPageSetting] = React.useState<PageSetting>(initialPageSetting);

  const onPaginationChange = React.useCallback(
    (page: number, size?: number) => {
      setPageSetting({ page: page - 1, size: size || initialPageSetting.size });
    },
    [setPageSetting]
  );

  const resetPagination = () => {
    setPageSetting(initialPageSetting);
  };
  return {
    pageSetting,
    setPageSetting,
    onPaginationChange,
    resetPagination,
  };
};
