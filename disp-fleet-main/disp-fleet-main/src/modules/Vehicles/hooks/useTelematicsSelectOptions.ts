import { useMemo } from 'react';
import { useSelectSearch } from './useSelectSearch';
import { useTelematicsDirectory } from 'api/directories/directories.api';
import { TSelectOption } from 'types/vehicles';

interface TResult {
  options: TSelectOption[];
  isLoading: boolean;
  onSearch: (value: string) => void;
}

export const useTelematicsSelectOptions = (): TResult => {
  const { searchValue, onSearch } = useSelectSearch();
  const { data, isLoading } = useTelematicsDirectory(searchValue);

  const options: TSelectOption[] = useMemo(
    () => data
      ? data.content.map(({ id, title }) => ({
        value: id,
        label: title,
      }))
      : [],
    [data]
  );

  return {
    options,
    isLoading,
    onSearch,
  };
};
