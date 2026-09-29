import { getRoundedParams } from './getRoundedParams';
import { DefaultValues } from 'constants/constants.app';

describe('getRoundedParams', () => {
  describe('обработка weight', () => {
    test('для числа с дробной частью должен округлить до 3 знаков', () => {
      expect(getRoundedParams({ weight: 123.456 })).toEqual({
        weight: 123.456,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для целого числа должен вернуть его', () => {
      expect(getRoundedParams({ weight: 100 })).toEqual({
        weight: 100,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 0 должен вернуть "-" (так как 0 falsy)', () => {
      expect(getRoundedParams({ weight: 0 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для undefined должен вернуть "-"', () => {
      expect(getRoundedParams({ weight: undefined })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для null должен вернуть "-"', () => {
      expect(getRoundedParams({ weight: null })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для очень большого числа должен округлить до 3 знаков', () => {
      expect(getRoundedParams({ weight: 10000.999 })).toEqual({
        weight: 10000.999,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });
  });

  describe('обработка volume', () => {
    test('для 0 должен вернуть "-" (resVolume = 0, но 0 || "-" = "-")', () => {
      expect(getRoundedParams({ volume: 0 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 1000000 (1 м³) должен вернуть 1', () => {
      expect(getRoundedParams({ volume: 1000000 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 1,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 1500000 (1.5 м³) должен вернуть 1.5', () => {
      expect(getRoundedParams({ volume: 1500000 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 1.5,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 1234567 должен вернуть 1.235 с округлением', () => {
      expect(getRoundedParams({ volume: 1234567 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 1.235,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 500 (меньше MIN_VALUE) должен вернуть 0.001', () => {
      expect(getRoundedParams({ volume: 500 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 0.001,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 999 (меньше MIN_VALUE) должен вернуть 0.001', () => {
      expect(getRoundedParams({ volume: 999 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 0.001,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для 1001 (примерно MIN_VALUE * CUBE_MM_TO_METERS) должен вернуть 0.001', () => {
      expect(getRoundedParams({ volume: 1001 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 0.001,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для undefined должен вернуть "-"', () => {
      expect(getRoundedParams({ volume: undefined })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для null должен вернуть 0.001 (null / CUBE_MM_TO_METERS = 0 < MIN_VALUE)', () => {
      expect(getRoundedParams({ volume: null })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 0.001,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для очень большого объема должен вернуть правильное значение', () => {
      expect(getRoundedParams({ volume: 1000000000 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 1000,
        distance: DefaultValues.emptyValueInTable,
      });
    });
  });

  describe('обработка distance', () => {
    test('для числа с дробной частью должен округлить до 1 знака', () => {
      expect(getRoundedParams({ distance: 123.45 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: 123.5,
      });
    });

    test('для целого числа должен вернуть его', () => {
      expect(getRoundedParams({ distance: 100 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: 100,
      });
    });

    test('для 0 должен вернуть "-" (так как 0 falsy)', () => {
      expect(getRoundedParams({ distance: 0 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для undefined должен вернуть "-"', () => {
      expect(getRoundedParams({ distance: undefined })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для null должен вернуть "-"', () => {
      expect(getRoundedParams({ distance: null })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для очень большого расстояния должен округлить до 1 знака', () => {
      expect(getRoundedParams({ distance: 9999.99 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: 10000,
      });
    });
  });

  describe('комбинированные случаи', () => {
    test('для всех параметров заданных должен вернуть округлённые значения', () => {
      expect(getRoundedParams({ weight: 123.456, volume: 1500000, distance: 123.45 })).toEqual({
        weight: 123.456,
        volume: 1.5,
        distance: 123.5,
      });
    });

    test('для weight и volume должен вернуть их значения', () => {
      expect(getRoundedParams({ weight: 100, volume: 1000000 })).toEqual({
        weight: 100,
        volume: 1,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для weight и distance должен вернуть их значения', () => {
      expect(getRoundedParams({ weight: 100, distance: 100 })).toEqual({
        weight: 100,
        volume: DefaultValues.emptyValueInTable,
        distance: 100,
      });
    });

    test('для volume и distance должен вернуть их значения', () => {
      expect(getRoundedParams({ volume: 1000000, distance: 100 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 1,
        distance: 100,
      });
    });

    test('для пустого объекта должен вернуть все "-"', () => {
      expect(getRoundedParams({})).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для undefined volume и weight должен вернуть "-"', () => {
      expect(getRoundedParams({ volume: undefined, weight: undefined })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для null weight и distance должен вернуть "-"', () => {
      expect(getRoundedParams({ weight: null, distance: null })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });
  });

  describe('граничные случаи', () => {
    test('для volume = 1 (минимальное значение) должен вернуть 0.001', () => {
      expect(getRoundedParams({ volume: 1 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: 0.001,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для weight = 0.001 должен вернуть 0.001', () => {
      expect(getRoundedParams({ weight: 0.001 })).toEqual({
        weight: 0.001,
        volume: DefaultValues.emptyValueInTable,
        distance: DefaultValues.emptyValueInTable,
      });
    });

    test('для distance = 0.04 должен вернуть 0', () => {
      expect(getRoundedParams({ distance: 0.04 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: 0,
      });
    });

    test('для distance = 0.05 должен вернуть 0.1 (округление)', () => {
      expect(getRoundedParams({ distance: 0.05 })).toEqual({
        weight: DefaultValues.emptyValueInTable,
        volume: DefaultValues.emptyValueInTable,
        distance: 0.1,
      });
    });
  });
});
