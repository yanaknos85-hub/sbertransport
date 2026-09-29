import { RegisterSearchQuery } from 'api/register-search';
import { useState } from 'react';
import { useSettingsContext } from 'stores/SettingsContext';
import { SortSetting } from '../types/types';

export const useSavedSortSettings = (): [
  RegisterSearchQuery['sortSetting'],
  (sortSettings: RegisterSearchQuery['sortSetting']) => void,
  () => void
] => {
  const {
    sortSettings: currentSorting, saveSortSettings, deleteSortSettings,
  } = useSettingsContext().Taxi;
  const [sortSettings, setSortSettings] = useState<RegisterSearchQuery['sortSetting']>(
    currentSorting() as RegisterSearchQuery['sortSetting']
  );

  const saveSettings = (sorting: RegisterSearchQuery['sortSetting']) => {
    setSortSettings(sorting);
    // @ts-ignore
    saveSortSettings(sorting as SortSetting);
  };

  return [sortSettings, saveSettings, deleteSortSettings];
};
