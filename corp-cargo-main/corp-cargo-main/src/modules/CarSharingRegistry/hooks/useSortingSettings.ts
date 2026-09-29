import { useSettingsContext } from 'stores/SettingsContext';
import { SortFields } from 'api/register-search';
import { useSavedSortSettings } from '../utils';

export const useSortingSettings = () => {
  const [sortSetting, setSortingSetting] = useSavedSortSettings();

  const { saveSortSettings, deleteSortSettings } = useSettingsContext().CarSharing;

  const handleDefaultSort = (): void => {
    const defaultSorting = {
      property: SortFields.CREATION_DATE,
      directionAsc: false,
    };
    saveSortSettings(defaultSorting);
    setSortingSetting(defaultSorting);
  };

  return {
    sortSetting,
    setSortingSetting,
    deleteSortSettings,
    handleDefaultSort,
  };
};
