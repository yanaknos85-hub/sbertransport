import React from 'react';

export interface PageSetting {
  page: number;
  size: number;
}
export const usePagination = (): {
  pageSetting: PageSetting;
  setPageSetting(setting: PageSetting): void;
  onPaginationChange(page: number, size?: number | undefined): void;
  resetPagination(): void;
} => {
  const initilaSetting = { page: 0, size: 5 };
  const [pageSetting, setPageSetting] = React.useState<PageSetting>(initilaSetting);

  const onPaginationChange = React.useCallback(
    (page: number, size?: number) => {
      setPageSetting({ page: page - 1, size: size || initilaSetting.size });
    },
    [setPageSetting]
  );

  const resetPagination = () => {
    setPageSetting(initilaSetting);
  };
  return {
    pageSetting,
    setPageSetting,
    onPaginationChange,
    resetPagination,
  };
};
