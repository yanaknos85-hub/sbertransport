import { useMemo } from 'react';

const useFilterCount = (query = {}, restrict: string[] = []) => {
  const filterCount = useMemo(() => (
    Object.entries(query).reduce((count, [key, value]) => {
      const hasValue = value && (!Array.isArray(value) || value.length);
      const isRestricted = ['page', 'size', 'field', 'direction', ...restrict].includes(key);

      if (hasValue && !isRestricted) {
        count += 1;
      }

      return count;
    }, 0)
  ), [query, restrict]);

  return { filterCount };
};

export default useFilterCount;
