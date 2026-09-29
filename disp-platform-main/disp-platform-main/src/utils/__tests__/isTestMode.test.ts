/**
 * @jest-environment jsdom
 */
import { TEST_MODE } from 'constants/app.constants';
import { isTestMode } from '../isTestMode';
import { isTestStand } from '../isTestStand';

// Mock isTestStand module
jest.mock('../isTestStand', () => ({
  isTestStand: jest.fn(),
}));

describe('isTestMode', () => {
  const originalLocalStorage = global.localStorage;

  beforeEach(() => {
    // Mock localStorage
    Object.defineProperty(global, 'localStorage', {
      value: {
        getItem: jest.fn(),
        setItem: jest.fn(),
        removeItem: jest.fn(),
        clear: jest.fn(),
      },
      writable: true,
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    global.localStorage = originalLocalStorage;
  });

  test('should return true when localStorage contains "true"', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('true');

    expect(isTestMode()).toBe(true);
    expect(localStorage.getItem).toHaveBeenCalledWith(TEST_MODE);
  });

  test('should return false when localStorage contains "false"', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('false');

    expect(isTestMode()).toBe(false);
    expect(localStorage.getItem).toHaveBeenCalledWith(TEST_MODE);
  });

  test('should call isTestStand when localStorage returns null', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue(null);
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should call isTestStand when localStorage returns undefined', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue(undefined);
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should call isTestStand when localStorage returns empty string', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('');
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should use isTestStand result when localStorage is null and isTestStand returns false', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue(null);
    (isTestStand as jest.Mock).mockReturnValue(false);

    expect(isTestMode()).toBe(false);
  });

  test('should use isTestStand result when localStorage is undefined and isTestStand returns false', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue(undefined);
    (isTestStand as jest.Mock).mockReturnValue(false);

    expect(isTestMode()).toBe(false);
  });

  test('should handle case sensitivity - "TRUE" should fall through to isTestStand', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('TRUE');
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should handle case sensitivity - "FALSE" should fall through to isTestStand', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('FALSE');
    (isTestStand as jest.Mock).mockReturnValue(false);

    expect(isTestMode()).toBe(false);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should handle case sensitivity - "True" should fall through to isTestStand', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('True');
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should handle numeric string "0" - should fall through to isTestStand', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('0');
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should handle numeric string "1" - should fall through to isTestStand', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue('1');
    (isTestStand as jest.Mock).mockReturnValue(true);

    expect(isTestMode()).toBe(true);
    expect(isTestStand).toHaveBeenCalled();
  });

  test('should call isTestStand exactly once per function call', () => {
    (localStorage.getItem as jest.Mock).mockReturnValue(null);
    (isTestStand as jest.Mock).mockReturnValue(true);

    isTestMode();
    isTestMode();
    isTestMode();

    expect(isTestStand).toHaveBeenCalledTimes(3);
  });

  describe('integration with isTestStand', () => {
    const originalLocation = window.location;

    beforeEach(() => {
      Object.defineProperty(window, 'location', {
        value: { origin: '' },
        writable: true,
      });
    });

    afterEach(() => {
      // @ts-ignore
      window.location = originalLocation;
    });

    test('should return true when in test mode localStorage and isTestStand also returns true', () => {
      (localStorage.getItem as jest.Mock).mockReturnValue('true');
      (isTestStand as jest.Mock).mockReturnValue(true);

      expect(isTestMode()).toBe(true);
    });

    test('should return false when localStorage is "false" but isTestStand returns true', () => {
      (localStorage.getItem as jest.Mock).mockReturnValue('false');
      (isTestStand as jest.Mock).mockReturnValue(true);

      expect(isTestMode()).toBe(false);
      // localStorage value should take precedence
      expect(isTestStand).not.toHaveBeenCalled();
    });

    test('should delegate to isTestStand when localStorage value is not "true" or "false"', () => {
      (localStorage.getItem as jest.Mock).mockReturnValue('some-other-value');
      (isTestStand as jest.Mock).mockReturnValue(true);

      expect(isTestMode()).toBe(true);
      expect(isTestStand).toHaveBeenCalled();
    });
  });
});
