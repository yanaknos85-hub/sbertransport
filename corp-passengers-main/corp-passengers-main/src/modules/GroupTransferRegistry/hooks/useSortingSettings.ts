import { useColumns } from './useColumns';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { SettingType, TransportTypes } from '../types/types';
import { IUiPreferences } from 'stores/Registry/Registry.interface';

export const useSortingSettings = (
  userId?: string
) => {
  const { [StoreNames.registryStore]: register } = useAppStoreContext();
  const defaultColumns = useColumns();
  const transformedDefaultColumns = defaultColumns && [
    ...defaultColumns.map((item, index) => [
      item.dataIndex,
      {
        active: true,
        index: index,
      },
    ]),
  ];

  const transformData = (data, userID, transportType) => {
    return {
      userID,
      nameForm: `registry_${transportType}`,
      controls: [
        {
          typeControl: 'table',
          value: `request_${transportType}`,
          settings: data
            .filter(([_, settings]) => settings.active)
            .map(([key, settings]) => ({
              nameSetting: 'column',
              valueSetting: key,
              sort: settings.index,
            })),
        },
      ],
    };
  };

  const onDefaultSettings = () => {
    register.setSaveSettings(transformedDefaultColumns as SettingType[]);
    const saveSettings = transformData(transformedDefaultColumns, userId, TransportTypes.GROUP_TRANSFER.toLowerCase());
    register.setUiPreferences(userId, saveSettings as IUiPreferences);
  };

  return {
    onDefaultSettings,
  };
};
