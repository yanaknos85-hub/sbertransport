import { useMemo } from 'react';

import { useUsingAccessiblePositionIdDirectory } from 'api/directories/directories.api';
import { UsingAccessiblePositionIdItem } from 'api/directories/directories.types';
import { TSelectOption } from 'types/vehicles';

interface TResult {
  options: TSelectOption[];
  isLoading: boolean;
}

export const useUsingAccessiblePositionIdSelectOptions = (): TResult => {
  const { data, isLoading } = useUsingAccessiblePositionIdDirectory();

  const options: TSelectOption[] = useMemo(() => {
    if (!data) {
      return [];
    }

    const content = data as unknown as UsingAccessiblePositionIdItem[];

    return content.map(({ id, title }) => ({
      value: id,
      label: title,
    }));
  }, [data]);

  return { options, isLoading };
};
