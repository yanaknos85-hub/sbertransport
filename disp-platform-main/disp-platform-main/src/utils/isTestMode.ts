import { TEST_MODE } from 'constants/app.constants';
import { isTestStand } from './isTestStand';

/**
 * Проверяет, находится ли приложение в тестовом режиме
 * @returns true если включен тестовый режим, false в противном случае
 */
export const isTestMode = (): boolean => {
  const testModeLS = localStorage.getItem(TEST_MODE);

  switch (testModeLS) {
    case 'true': return true;
    case 'false': return false;
    default: return isTestStand();
  }
};
