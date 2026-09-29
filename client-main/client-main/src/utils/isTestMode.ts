import { TEST_MODE } from 'constants/constants.app';
import { isTestStand } from './isTestStand';

export const isTestMode = (): boolean => {
  const testModeLS = localStorage.getItem(TEST_MODE);

  switch (testModeLS) {
    case 'true': return true;
    case 'false': return false;
    default: return isTestStand();
  }
};
