import { useState, useRef, useEffect } from 'react';

interface TResult {
  searchValue: string;
  onSearch: (value: string) => void;
}

export const useSelectSearch = (): TResult => {
  const [searchValue, setSearchValue] = useState<string>('');

  const timerRef = useRef<ReturnType<typeof setTimeout>>();

  const onSearch = (value: string) => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }
    timerRef.current = setTimeout(() => setSearchValue(value), 800);
  };

  useEffect(
    () => {
      return () => {
        if (timerRef.current) {
          clearTimeout(timerRef.current);
        }
      };
    },
    []
  );

  return {
    searchValue,
    onSearch,
  };
};
