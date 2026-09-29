import { useState } from 'react';
import { useSettingsContext } from 'stores/SettingsContext';
import { SortFields } from 'api/register-search';

interface SortSetting {
  sortField: SortFields;
  sortDirection: boolean;
}

export const useSavedSortSettings = (): [
  SortSetting,
  (sortSettings: SortSetting) => void,
  () => void
] => {
  const {
    sortSettings: currentSorting, saveSortSettings, deleteSortSettings,
  } = useSettingsContext().Taxi;
  const [sortSettings, setSortSettings] = useState<SortSetting>(
    currentSorting() as unknown as SortSetting
  );

  const saveSettings = (sorting: SortSetting) => {
    setSortSettings(sorting);
    saveSortSettings({
      property: sorting?.sortField as string,
      directionAsc: sorting?.sortDirection as boolean,
    });
  };

  return [sortSettings, saveSettings, deleteSortSettings];
};
