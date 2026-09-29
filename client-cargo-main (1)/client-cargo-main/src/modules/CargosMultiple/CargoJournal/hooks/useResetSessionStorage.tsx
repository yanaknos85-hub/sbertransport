import { useEffect } from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';

export const useResetSessionStorage = () => {
  const history = History();

  useEffect(() => {
    return () => {
      const allowedPaths = [
        'client/cargo/single/list/active',
        'client/cargo/regular/list/active',
        'client/cargo/regular/list/final',
        'client/cargo/single/list/final',
      ];

      const currentLocation = history.location?.pathname || window.location.pathname;
      const isStayingInAllowedPaths = allowedPaths.some(path => currentLocation.includes(path)
      );

      if (!isStayingInAllowedPaths) {
        sessionStorage.removeItem('journalFilters');
        sessionStorage.setItem('count', '0');
      }
    };
  }, [history]);
};
