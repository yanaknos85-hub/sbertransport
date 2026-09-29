import { useMemo } from 'react';

const viewStateFields = ['page', 'size', 'field', 'direction'];

const useFiltersCount = (query = {}, restrict: string[] = []) => {
  const filtersCount = useMemo(
    () => Object.entries(query).reduce((count, [key, value]) => {
      if ([...viewStateFields, ...restrict].includes(key) || (Array.isArray(value) && !value.length)) {
        return count;
      }

      if (value) {
        count += 1;
      }

      return count;
    }, 0),
    [query, restrict]
  );

  return { filtersCount };
};

export default useFiltersCount;
