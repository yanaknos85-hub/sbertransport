import { TEST_MODE } from 'constants/constants.app';

export const isTestMode = (): boolean => {
  const testModeLS = localStorage.getItem(TEST_MODE);

  switch (testModeLS) {
    case 'true': return true;
    case 'false': return false;
    default: return window.location.origin.includes('localhost');
  }
};
