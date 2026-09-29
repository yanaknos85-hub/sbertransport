import * as t from 'io-ts';
import { useCallback, useState } from 'react';

const PageSetting = t.strict({
  page: t.number,
  size: t.number,
});
type PageSetting = t.TypeOf<typeof PageSetting>;

export const usePagination = (
  isEnlarged?: boolean
): {
    pageSetting: PageSetting;
    onPaginationChange: (page: number, size?: number | undefined) => void;
    setPageSetting(setting: PageSetting): void;
    initialPageSettings: PageSetting;
    resetPagination(): void;
  } => {
  const initialPageSettings = { page: 0, size: isEnlarged ? 100 : 10 };

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
