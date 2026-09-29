import { useState } from 'react';
import { SortFields } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';

export const useSortingSettings = () => {
  const defaultSorting = {
    property: SortFields.HUMAN_READABLE_ID,
    directionAsc: false,
  };
  const [sortSetting, setSortingSetting] = useState(defaultSorting);

  const handleDefaultSort = (): void => {
    setSortingSetting(defaultSorting);
  };

  return {
    sortSetting,
    setSortingSetting,
    handleDefaultSort,
  };
};
