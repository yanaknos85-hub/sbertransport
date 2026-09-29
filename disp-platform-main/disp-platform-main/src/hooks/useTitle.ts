import { useEffect } from 'react';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const useTitle = (title: string, deps?: any[]): void => {
  useEffect(() => {
    document.title = title;
  }, [title, deps]);
};
