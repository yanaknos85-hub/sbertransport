import { TEST_MODE } from 'constants/constants.app';
import { isTestMode } from '../isTestMode';

jest.mock('../isTestStand', () => ({
  isTestStand: jest.fn(),
}));

import { isTestStand } from '../isTestStand';

const mockIsTestStand = isTestStand as jest.Mock;

describe('isTestMode', () => {
  beforeEach(() => {
    localStorage.clear();
    mockIsTestStand.mockReset();
  });

  it('возвращает true при localStorage[TEST_MODE] === "true"', () => {
    localStorage.setItem(TEST_MODE, 'true');

    expect(isTestMode()).toBe(true);
    expect(mockIsTestStand).not.toHaveBeenCalled();
  });

  it('возвращает false при localStorage[TEST_MODE] === "false"', () => {
    localStorage.setItem(TEST_MODE, 'false');

    expect(isTestMode()).toBe(false);
    expect(mockIsTestStand).not.toHaveBeenCalled();
  });

  it('делегирует в isTestStand, когда значение в localStorage отсутствует', () => {
    mockIsTestStand.mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(mockIsTestStand).toHaveBeenCalledTimes(1);
  });

  it('делегирует в isTestStand, когда значение в localStorage отличается от "true"/"false"', () => {
    localStorage.setItem(TEST_MODE, '1');
    mockIsTestStand.mockReturnValue(false);

    expect(isTestMode()).toBe(false);
    expect(mockIsTestStand).toHaveBeenCalledTimes(1);
  });
});
