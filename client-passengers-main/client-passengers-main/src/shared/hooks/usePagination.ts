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
  const initialSetting = { page: 0, size: 30 };
  const [pageSetting, setPageSetting] = React.useState<PageSetting>(initialSetting);

  const onPaginationChange = React.useCallback(
    (page: number, size?: number) => {
      setPageSetting({ page: page - 1, size: size || initialSetting.size });
    },
    [setPageSetting]
  );

  const resetPagination = () => {
    setPageSetting(initialSetting);
  };
  return {
    pageSetting,
    setPageSetting,
    onPaginationChange,
    resetPagination,
  };
};
