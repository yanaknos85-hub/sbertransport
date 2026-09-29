import { useMemo } from 'react';
import { useUsingSubTypeDirectory } from 'api/directories/directories.api';
import { TSelectOption } from 'types/vehicles';

interface TResult {
  options: TSelectOption[];
  isLoading: boolean;
}

export const useUsingSubTypeSelectOptions = (typeId: string | undefined): TResult => {
  const { data, isLoading } = useUsingSubTypeDirectory();

  const options: TSelectOption[] = useMemo(() => {
    if (!data) {
      return [];
    }

    // временная фильтрация на фронте
    const content = typeId ? data.content.filter(({ type }) => type.id === typeId) : data.content;
    return content.map(({ id, title }) => ({
      value: id,
      label: title,
    }));
  }, [data, typeId]);

  return { options, isLoading };
};
