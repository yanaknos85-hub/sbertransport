import { useEffect, useRef, useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';

interface DownloadUrlHook {
  isLoading: boolean;
  download: (event: React.MouseEvent<HTMLElement, MouseEvent>) => void;
  error: string;
}

export const useDownloadUrl = (url: string): DownloadUrlHook => {
  const { http } = useAppStoreContext();

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const elRef = useRef<HTMLAnchorElement>();

  useEffect(() => {
    setError('');
    elRef.current = undefined;
  }, [url]);

  const download = (event: React.MouseEvent<HTMLElement, MouseEvent>) => {
    event.preventDefault();
    if (elRef.current) {
      elRef.current.click();
      return;
    }
    setIsLoading(true);
    http
      .get(url, { responseType: 'blob' })
      .then(response => {
        if (response.data) {
          const el = document.createElement('a');
          el.href = window.URL.createObjectURL(response.data as Blob | MediaSource);
          el.setAttribute('download', '');
          el.click();
          elRef.current = el;
        }
        setIsLoading(false);
      })
      .catch((e: Error) => {
        setError(e.message);
        setIsLoading(false);
      });
  };

  return {
    isLoading,
    download,
    error,
  };
};
