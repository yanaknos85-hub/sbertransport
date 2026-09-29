import { useState, Dispatch, SetStateAction } from 'react';
import {
  PersonalRegistryFilters,
  PersonalSearchResponse,
  SortFields
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { useSettingsContext } from 'stores/SettingsContext';
import { SortSettings } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { TripRegistrySorterResult } from './useColumns';

export const useSortingSettings = (
  responseData: PersonalSearchResponse,
  setFilterParams: Dispatch<SetStateAction<PersonalRegistryFilters | undefined>>
) => {
  const {
    sortSettings, saveSortSettings, deleteSortSettings,
  } = useSettingsContext().Personal;
  const [userSortSettings, setUserSortSettings] = useState<SortSettings | undefined>(sortSettings());

  const defaultSortSetting = {
    property: SortFields.CREATION_DATE,
    directionAsc: false,
  };

  const onDefaultSortClick = (): void => {
    saveSortSettings(defaultSortSetting);
    setUserSortSettings(defaultSortSetting);
    setFilterParams({
      sortSetting: defaultSortSetting,
      pageSetting: { page: responseData?.pageable?.pageNumber as number, size: 10 },
    });
  };

  const applySorter = (sorter: TripRegistrySorterResult) => {
    if (sorter.column) {
      saveSortSettings({
        property: sorter.column.sortProperty!,
        directionAsc: sorter.order === 'ascend',
      });
      setUserSortSettings({
        property: sorter.column.sortProperty!,
        directionAsc: sorter.order === 'ascend',
      });
    } else {
      deleteSortSettings();
      setUserSortSettings(undefined);
    }
  };

  return {
    userSortSettings,
    setUserSortSettings,
    onDefaultSortClick,
    applySorter,
  };
};
