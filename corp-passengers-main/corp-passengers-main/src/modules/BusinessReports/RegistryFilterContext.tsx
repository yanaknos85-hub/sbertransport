/* eslint-disable @stylistic/comma-dangle */
import * as React from 'react';

interface RegistryFiltersContext<T> {
  filterValues: T;
  setFilterValues: React.Dispatch<React.SetStateAction<T>>;
}

interface RegistryFiltersProvider<T> {
  children?: React.ReactNode | undefined;
  defaultFilters?: T;
}

const RegistryFiltersContext = React.createContext<RegistryFiltersContext<unknown> | null>(null);

export const useRegistryFilters = <T,>(): RegistryFiltersContext<T> => {
  const ctx = React.useContext(RegistryFiltersContext);

  if (!ctx) {
    throw new Error('Context not provided');
  }

  return ctx as RegistryFiltersContext<T>;
};

export const RegistryFiltersProvider = <T,>({ children, defaultFilters }: RegistryFiltersProvider<T>): JSX.Element => {
  const [filterValues, setFilterValues] = React.useState<T>(defaultFilters as T);

  const registryFields = { filterValues, setFilterValues } as RegistryFiltersContext<T>;

  return (
    <RegistryFiltersContext.Provider value={registryFields as RegistryFiltersContext<unknown>}>
      {children}
    </RegistryFiltersContext.Provider>
  );
};
