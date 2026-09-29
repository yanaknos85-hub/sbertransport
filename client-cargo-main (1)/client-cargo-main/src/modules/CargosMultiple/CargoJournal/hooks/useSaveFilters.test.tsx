import { renderHook, act } from '@testing-library/react-hooks';
import moment from 'moment';

import { useSaveFilters } from './useSaveFilters';

// Мокаем sessionStorage
const createMockSessionStorage = () => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
};

describe('useSaveFilters', () => {
  const mockSessionStorage = createMockSessionStorage();

  beforeAll(() => {
    Object.defineProperty(window, 'sessionStorage', {
      value: mockSessionStorage,
      writable: true,
    });
  });

  beforeEach(() => {
    mockSessionStorage.clear();
    jest.clearAllMocks();
  });

  describe('setFilterState', () => {
    it('сохраняет фильтры в sessionStorage', () => {
      const { result } = renderHook(() => useSaveFilters());

      const filters = {
        requestHumanId: '123',
        desiredDate: [moment().toISOString(), moment().toISOString()],
      };

      act(() => {
        result.current.setFilterState(filters);
      });

      const saved = mockSessionStorage.getItem('journalFilters');
      expect(saved).toBeTruthy();
      const parsed = JSON.parse(saved!);
      expect(parsed.requestHumanId).toBe('123');
      // Даты сохраняются как строки ISO
      expect(parsed.desiredDate).toHaveLength(2);
      expect(typeof parsed.desiredDate?.[0]).toBe('string');
      expect(typeof parsed.desiredDate?.[1]).toBe('string');
    });

    it('должен вызвать debounced сохранение при повторных вызовах', () => {
      const { result } = renderHook(() => useSaveFilters());

      const filters1 = { requestHumanId: '123' };
      const filters2 = { requestHumanId: '456' };

      act(() => {
        result.current.setFilterState(filters1);
        result.current.setFilterState(filters2);
      });

      // Должно сохраниться последнее значение
      const saved = mockSessionStorage.getItem('journalFilters');
      expect(JSON.parse(saved!)).toEqual(filters2);
    });
  });

  describe('getFilterState', () => {
    it('возвращает пустой объект если sessionStorage пуст', () => {
      const { result } = renderHook(() => useSaveFilters());

      const state = result.current.getFilterState();
      expect(state).toEqual({});
    });

    it('восстанавливает фильтры из sessionStorage', () => {
      const savedFilters = {
        requestHumanId: '123',
        desiredDate: [new Date().toISOString(), new Date().toISOString()],
      };
      mockSessionStorage.setItem('journalFilters', JSON.stringify(savedFilters));

      const { result } = renderHook(() => useSaveFilters());

      const state = result.current.getFilterState();
      expect(state.requestHumanId).toBe('123');
      expect(moment.isMoment(state.desiredDate?.[0])).toBe(true);
      expect(moment.isMoment(state.desiredDate?.[1])).toBe(true);
    });

    it('правильно парсит shipmentTime как moment-объекты', () => {
      const savedFilters = {
        shipmentTime: [new Date().toISOString(), new Date().toISOString()],
      };
      mockSessionStorage.setItem('journalFilters', JSON.stringify(savedFilters));

      const { result } = renderHook(() => useSaveFilters());

      const state = result.current.getFilterState();
      expect(moment.isMoment(state.shipmentTime?.[0])).toBe(true);
      expect(moment.isMoment(state.shipmentTime?.[1])).toBe(true);
    });
  });

  describe('resetFilerState', () => {
    it('удаляет фильтры из sessionStorage и сбрасывает счётчик', () => {
      mockSessionStorage.setItem('journalFilters', JSON.stringify({ requestHumanId: '123' }));
      mockSessionStorage.setItem('count', JSON.stringify(3));

      const { result } = renderHook(() => useSaveFilters());

      act(() => {
        result.current.resetFilerState();
      });

      expect(mockSessionStorage.getItem('journalFilters')).toBeNull();
      expect(mockSessionStorage.getItem('count')).toBe('0');
    });
  });

  describe('filledCountStorage', () => {
    it('считает количество заполненных полей', () => {
      mockSessionStorage.setItem(
        'journalFilters',
        JSON.stringify({
          requestHumanId: '123',
          desiredDate: [moment(), moment()],
          transportTypeEnum: [],
        })
      );

      const { result } = renderHook(() => useSaveFilters());

      const count = result.current.filledCountStorage();
      expect(count).toBe(2); // requestHumanId + desiredDate (transportTypeEnum пустой)
    });

    it('игнорирует служебные поля sortSetting и pageSetting', () => {
      mockSessionStorage.setItem(
        'journalFilters',
        JSON.stringify({
          requestHumanId: '123',
          sortSetting: { directionAsc: true, property: 'creationDate' },
          pageSetting: { page: 0, size: 20 },
        })
      );

      const { result } = renderHook(() => useSaveFilters());

      const count = result.current.filledCountStorage();
      expect(count).toBe(1); // только requestHumanId
    });

    it('возвращает 0 если sessionStorage пуст', () => {
      const { result } = renderHook(() => useSaveFilters());

      const count = result.current.filledCountStorage();
      expect(count).toBe(0);
    });
  });

  describe('saveCount', () => {
    it('сохраняет количество заполненных полей', () => {
      const { result } = renderHook(() => useSaveFilters());

      const filters = {
        requestHumanId: '123',
        desiredDate: [moment().toISOString(), moment().toISOString()],
      };

      act(() => {
        result.current.saveCount(filters);
      });

      const count = mockSessionStorage.getItem('count');
      expect(count).toBe('2');
    });
  });

  describe('cleanup', () => {
    it('отменяет debounce при размонтировании', () => {
      const { result, unmount } = renderHook(() => useSaveFilters());

      // Вызываем setFilterState
      act(() => {
        result.current.setFilterState({ requestHumanId: '123' });
      });

      // Размонтируем хук
      unmount();

      // Debounce должен быть отменён (функция.cancel вызвана)
      // Проверяем, что ничего не упало (нет ошибок при unmount)
    });
  });
});
