import { useCallback, useEffect, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';

const TRIPS_SETTINGS = 'PASS_TRIPS_SETTINGS';

export interface TripsSettings {
  map: {
    isVisible: boolean;
  };
  table: {
    isVisible: boolean;
    size: number;
  };
}

const getDefaultTripsSettings = (localStorageSettings: TripsSettings): TripsSettings => ({
  map: {
    isVisible: localStorageSettings.map?.isVisible ?? true,
  },
  table: {
    isVisible: localStorageSettings.table?.isVisible ?? true,
    size: localStorageSettings.table?.size ?? 10,
  },
});

const useHook = () => {
  const [settings, setSettings] = useState<TripsSettings>(
    getDefaultTripsSettings(JSON.parse(localStorage.getItem(TRIPS_SETTINGS) || '{}'))
  );

  const setIsMapVisible = useCallback((isVisible: boolean) => {
    setSettings(prev => ({
      ...prev,
      map: {
        ...prev.map,
        isVisible,
      },
    }));
  }, []);

  const setIsTableVisible = useCallback((isVisible: boolean) => {
    setSettings(prev => ({
      ...prev,
      table: {
        ...prev.table,
        isVisible,
      },
    }));
  }, []);

  const setTableSize = useCallback((size: number) => {
    setSettings(prev => ({
      ...prev,
      table: {
        ...prev.table,
        size,
      },
    }));
  }, []);

  useEffect(() => {
    localStorage.setItem(TRIPS_SETTINGS, JSON.stringify(settings));
  }, [settings]);

  return {
    settings,
    setIsMapVisible,
    setIsTableVisible,
    setTableSize,
  };
};

export const [useTripsSettings, TripsSettingsProvider] = createCallableCtx(useHook, {
  name: 'TripsSettingsProvider',
});
