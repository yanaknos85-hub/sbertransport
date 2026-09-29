/**
 * Unit тесты для API фильтров Exchange
 * Проверяют базовую логику без глубокой интеграции
 */

import { KEYS } from './types';
import { FilterValues, FilterResponse } from './filters';

describe('Exchange Filters API', () => {
  describe('Constants', () => {
    it('should have correct EXCHANGE_FILTERS key', () => {
      expect(KEYS.EXCHANGE_FILTERS).toBe('exchangeFilters');
    });
  });

  describe('Types', () => {
    it('should have FilterValues interface with addressFrom and addressTo', () => {
      const filterValues: FilterValues = {
        addressFrom: 'Москва',
        addressTo: 'Санкт-Петербург',
      };
      expect(filterValues.addressFrom).toBe('Москва');
      expect(filterValues.addressTo).toBe('Санкт-Петербург');
    });

    it('should have FilterResponse interface with addressFrom and addressTo', () => {
      const filterResponse: FilterResponse = {
        addressFrom: 'Москва',
        addressTo: 'Санкт-Петербург',
      };
      expect(filterResponse.addressFrom).toBe('Москва');
      expect(filterResponse.addressTo).toBe('Санкт-Петербург');
    });

    it('should allow undefined values', () => {
      const filterValues: FilterValues = {
        addressFrom: undefined,
        addressTo: undefined,
      };
      expect(filterValues.addressFrom).toBeUndefined();
      expect(filterValues.addressTo).toBeUndefined();
    });
  });
});
