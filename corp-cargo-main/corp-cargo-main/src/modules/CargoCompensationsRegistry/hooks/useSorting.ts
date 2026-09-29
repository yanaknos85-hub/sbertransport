import { useState, Dispatch, SetStateAction } from 'react';
import { Registry, useSettingsContext } from 'stores/SettingsContext';
import { SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { SortFields } from 'api/register-search';

export const useSorting = (): {
  sortSetting: SortSetting | undefined;
  setSortSetting: Dispatch<SetStateAction<SortSetting | undefined>>;
  handleDefaultSort: () => void;
} => {
  const { [Registry.Cargo]: cargoSettings } = useSettingsContext();
  const [sortSetting, setSortSetting] = useState(cargoSettings.sortSettings());

  const handleDefaultSort = (): void => {
    const defaultSorting = {
      property: SortFields.CREATION_DATE,
      directionAsc: false,
    };
    cargoSettings.saveSortSettings(defaultSorting);
    setSortSetting(defaultSorting);
  };

  return {
    sortSetting, setSortSetting, handleDefaultSort,
  };
};
