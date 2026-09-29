import { useMemo } from 'react';
import { useUsingTypeDirectory } from 'api/directories/directories.api';
import { TSelectOption } from 'types/vehicles';

interface TResult {
  options: TSelectOption[];
  isLoading: boolean;
}

export const useUsingTypeSelectOptions = (): TResult => {
  const { data, isLoading } = useUsingTypeDirectory();

  const options: TSelectOption[] = useMemo(
    () => data
      ? data.content.map(({ id, title }) => ({
        value: id,
        label: title,
      }))
      : [],
    [data]
  );

  return { options, isLoading };
};
