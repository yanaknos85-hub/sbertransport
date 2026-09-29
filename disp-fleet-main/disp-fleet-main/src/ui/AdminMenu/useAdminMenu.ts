import { useMemo } from 'react';

export const useAdminMenu = (): {
  toClient: string;
} => {
  const toClient = useMemo(() => {
    // example http://webcorp.test.transportonline.info/employees
    const url = window.location.origin.split('.').slice(1).join('.');
    const http = 'https:';

    return `${http}//${url}/`;
  }, []);

  return {
    toClient,
  };
};
