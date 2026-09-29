import { useMemo } from 'react';

export const useUserMenu = (): {
  toAdmin: string;
} => {
  const toAdmin = useMemo(() => {
    const url = window.location.href.split('/');
    const web = `webcorp.${url[2]}`;
    const http = 'http:';

    url[2] = web;

    return `${http}/${url[1]}/${url[2]}/admin`;
  }, []);

  return {
    toAdmin,
  };
};
