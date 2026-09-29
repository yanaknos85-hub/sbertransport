import { useMemo } from 'react';
import { useModelsDirectory } from 'api/directories/directories.api';
import { TSelectOption } from 'types/vehicles';
import { useSelectSearch } from './useSelectSearch';

interface TResult {
  options: TSelectOption[];
  isLoading: boolean;
  onSearch: (value: string) => void;
}

export const useModelsSelectOptions = (brandId: string | undefined): TResult => {
  const { searchValue, onSearch } = useSelectSearch();
  const { data, isLoading } = useModelsDirectory(searchValue);

  const options: TSelectOption[] = useMemo(() => {
    if (!data) {
      return [];
    }

    // временная фильтрация на фронте
    const content = brandId ? data.content.filter(({ brand }) => brand.id === brandId) : data.content;

    return content.map(({ id, title }) => ({
      value: id,
      label: title,
    }));
  }, [data, brandId]);

  return {
    options,
    isLoading,
    onSearch,
  };
};
