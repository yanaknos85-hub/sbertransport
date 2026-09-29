import { useMemo } from 'react';
import { useBrandsDirectory } from 'api/directories/directories.api';
import { TSelectOption } from 'types/vehicles';
import { useSelectSearch } from './useSelectSearch';

interface TResult {
  options: TSelectOption[];
  isLoading: boolean;
  onSearch: (value: string) => void;
}

export const useBrandsSelectOptions = (): TResult => {
  const { searchValue, onSearch } = useSelectSearch();
  const { data, isLoading } = useBrandsDirectory(searchValue, { page: 0, size: 500 });

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
