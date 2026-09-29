import type { SortSetting } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import { useState } from 'react';

export const useSavedSortSettings = (): [
  SortSetting | undefined,
  (sortSettings: SortSetting) => void
] => {
  const [sortSettings, setSortSettings] = useState<SortSetting>();

  const saveSettings = (sorting: SortSetting) => {
    setSortSettings(sorting);
  };

  return [sortSettings, saveSettings];
};
