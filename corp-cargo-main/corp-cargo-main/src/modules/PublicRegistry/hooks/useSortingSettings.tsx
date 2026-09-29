import { Dispatch, SetStateAction, useState } from 'react';
import {
  PublicRegistryFilters,
  SearchResponse,
  SortSettings
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { SortFields } from 'api/register-search';
import { useSettingsContext } from 'stores/SettingsContext';
import { TripRegistrySorterResult } from './useColumns';

export const useSortingSettings = (
  responseData: SearchResponse,
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters | undefined>>
) => {
  const {
    sortSettings, saveSortSettings, deleteSortSettings,
  } = useSettingsContext().Public;
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
      pageSetting: { page: responseData.pageable.pageNumber, size: 100 },
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
