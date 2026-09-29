import { useEffect } from 'react';

export const useTitle = (title: string, deps?: any[]): void => {
  useEffect(() => {
    document.title = title;
  }, [title, deps]);
};
