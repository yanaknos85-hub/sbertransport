import { TEST_MODE } from 'constants/app.constants';
import { isTestStand } from './isTestStand';

/**
 * Проверяет режим тестирования (локально или через флаг в localStorage)
 * @returns true если включен тестовый режим
 */
export const isTestMode = (): boolean => {
  const testModeLS = localStorage.getItem(TEST_MODE);

  switch (testModeLS) {
    case 'true': return true;
    case 'false': return false;
    default: return isTestStand();
  }
};
