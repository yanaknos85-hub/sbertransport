import { TEST_MODE } from 'constants/app.constants';
import { useEffect } from 'react';

export const useTestMode = () => {
  useEffect(() => {
    const testMode = new URLSearchParams(window.location.search).get('testMode');

    if (testMode === 'true') {
      localStorage.setItem(TEST_MODE, 'true');
    } else if (testMode === 'false') {
      localStorage.setItem(TEST_MODE, 'false');
    }
  }, []);
};
