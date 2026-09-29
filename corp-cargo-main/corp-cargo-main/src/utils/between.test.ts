import { inRange } from './between';

describe('inRange', () => {
  describe('основная функция inRange', () => {
    test('для числа внутри диапазона [1, 10] должно вернуть true', () => {
      expect(inRange(1, 10)(5)).toBe(true);
    });

    test('для числа на левой границе диапазона [1, 10] должно вернуть true', () => {
      expect(inRange(1, 10)(1)).toBe(true);
    });

    test('для числа на правой границе диапазона [1, 10] должно вернуть true', () => {
      expect(inRange(1, 10)(10)).toBe(true);
    });

    test('для числа меньше левой границы диапазона [1, 10] должно вернуть false', () => {
      expect(inRange(1, 10)(0)).toBe(false);
    });

    test('для числа больше правой границы диапазона [1, 10] должно вернуть false', () => {
      expect(inRange(1, 10)(11)).toBe(false);
    });

    test('для отрицательного диапазона [-10, -1] и числа внутри должно вернуть true', () => {
      expect(inRange(-10, -1)(-5)).toBe(true);
    });

    test('для отрицательного диапазона [-10, -1] и числа на границе должно вернуть true', () => {
      expect(inRange(-10, -1)(-10)).toBe(true);
    });

    test('для отрицательного диапазона [-10, -1] и числа вне диапазона должно вернуть false', () => {
      expect(inRange(-10, -1)(0)).toBe(false);
    });

    test('для диапазона [0, 0] и числа 0 должно вернуть true', () => {
      expect(inRange(0, 0)(0)).toBe(true);
    });

    test('для диапазона [0, 0] и числа 1 должно вернуть false', () => {
      expect(inRange(0, 0)(1)).toBe(false);
    });

    test('для диапазона [5, 5] и числа 5 должно вернуть true', () => {
      expect(inRange(5, 5)(5)).toBe(true);
    });

    test('для диапазона [5, 5] и числа 4 должно вернуть false', () => {
      expect(inRange(5, 5)(4)).toBe(false);
    });

    test('для дробных чисел в диапазоне [1.5, 3.5] и числа внутри должно вернуть true', () => {
      expect(inRange(1.5, 3.5)(2.5)).toBe(true);
    });

    test('для дробных чисел в диапазоне [1.5, 3.5] и числа на границе должно вернуть true', () => {
      expect(inRange(1.5, 3.5)(1.5)).toBe(true);
    });

    test('для дробных чисел в диапазоне [1.5, 3.5] и числа вне диапазона должно вернуть false', () => {
      expect(inRange(1.5, 3.5)(4)).toBe(false);
    });

    test('для диапазона [-5, 5] и отрицательного числа внутри должно вернуть true', () => {
      expect(inRange(-5, 5)(-3)).toBe(true);
    });

    test('для диапазона [-5, 5] и положительного числа внутри должно вернуть true', () => {
      expect(inRange(-5, 5)(3)).toBe(true);
    });

    test('для диапазона [-5, 5] и числа -6 должно вернуть false', () => {
      expect(inRange(-5, 5)(-6)).toBe(false);
    });

    test('для диапазона [-5, 5] и числа 6 должно вернуть false', () => {
      expect(inRange(-5, 5)(6)).toBe(false);
    });

    test('для очень большого диапазона [1000000, 2000000] и числа внутри должно вернуть true', () => {
      expect(inRange(1000000, 2000000)(1500000)).toBe(true);
    });

    test('для очень маленького диапазона [0.001, 0.002] и числа внутри должно вернуть true', () => {
      expect(inRange(0.001, 0.002)(0.0015)).toBe(true);
    });
  });

  describe('частичное применение функции', () => {
    test('можно создать повторно используемую функцию checkAge', () => {
      const checkAge = inRange(18, 65);
      expect(checkAge(25)).toBe(true);
      expect(checkAge(17)).toBe(false);
      expect(checkAge(66)).toBe(false);
    });

    test('можно создать функцию checkTemperature', () => {
      const checkTemperature = inRange(-20, 40);
      expect(checkTemperature(20)).toBe(true);
      expect(checkTemperature(-21)).toBe(false);
      expect(checkTemperature(41)).toBe(false);
    });
  });
});
