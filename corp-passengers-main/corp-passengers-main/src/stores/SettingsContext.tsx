import React from 'react';
import * as R from 'ramda';

interface SortSetting {
  property: string;
  directionAsc: boolean;
}

interface RegistrySettings {
  sortSettings(): SortSetting | undefined;
  saveSortSettings(sortSettings: SortSetting): void;
  deleteSortSettings(): void;
}

interface AllRegistrySettings {
  deleteAllRegistrySortSettings(): void | undefined;
}

export enum Registry {
  Repair = 'Repair',
  Personal = 'Personal',
  Public = 'Public',
  Taxi = 'Taxi',
  CarSharing = 'CarSharing',
  Cargo = 'Cargo',
  All = 'All',
}

export const sortSettings = (registry: Registry): SortSetting | undefined => {
  const stringSettings = sessionStorage.getItem(registry);
  let settingsObject: SortSetting | undefined;
  if (stringSettings) {
    settingsObject = JSON.parse(stringSettings);
  }
  return settingsObject;
};

export const saveSortSettings = (registry: Registry, sorting: SortSetting): void => {
  sessionStorage.setItem(registry, JSON.stringify({ ...sorting }));
};

const deleteSortSettings = (registry: Registry) => {
  sessionStorage.removeItem(registry);
};

export const deleteAllRegistrySortSettings = (): void => {
  Object.values(Registry).forEach(value => sessionStorage.removeItem(value));
};

export type RegistrySettingsContext = Record<Registry, RegistrySettings & AllRegistrySettings>;

export const defaultSettings = R.mergeAll(
  Object.values(Registry).map(key => (
    key === Registry.All ? {
      [key]: {
        deleteAllRegistrySortSettings: () => deleteAllRegistrySortSettings(),
      },
    } : {
      [key]: {
        saveSortSettings: (sorting: SortSetting): void => saveSortSettings(key, sorting),
        sortSettings: () => sortSettings(key),
        deleteSortSettings: () => deleteSortSettings(key),
      },
    })
  )
) as RegistrySettingsContext;

export const SettingsContext = React.createContext<RegistrySettingsContext>(defaultSettings);

export const useSettingsContext = (): RegistrySettingsContext => {
  const context = React.useContext(SettingsContext);

  if (context === undefined) {
    throw new Error('useSettingsContext must be used within a SettingsProvider');
  }

  return context;
};
