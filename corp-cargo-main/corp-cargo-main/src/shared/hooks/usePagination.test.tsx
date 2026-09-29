import { renderHook, act } from '@testing-library/react-hooks';
import { usePagination } from './usePagination';
import { PageSetting } from 'stores/CargoRegistry/CargoRegistry.interface';

const defaultPageSettings: PageSetting = {
  page: 0,
  size: 10,
};

describe('usePagination', () => {
  describe('Тесты начального состояния', () => {
    it('использует defaultPageSettings когда initialPageSettings не передан', () => {
      const { result } = renderHook(() => usePagination());

      expect(result.current.pageSetting).toEqual(defaultPageSettings);
    });

    it('использует переданный initialPageSettings', () => {
      const customSettings: PageSetting = {
        page: 2,
        size: 50,
      };

      const { result } = renderHook(() => usePagination(customSettings));

      expect(result.current.pageSetting).toEqual(customSettings);
    });
  });

  describe('Тесты setPageSetting', () => {
    it('обновляет pageSetting при вызове setPageSetting', () => {
      const { result } = renderHook(() => usePagination());

      const newSettings: PageSetting = {
        page: 5,
        size: 20,
      };

      act(() => {
        result.current.setPageSetting(newSettings);
      });

      expect(result.current.pageSetting).toEqual(newSettings);
    });

    it('обновляет pageSetting на основе предыдущего значения', () => {
      const customSettings: PageSetting = {
        page: 3,
        size: 25,
      };

      const { result } = renderHook(() => usePagination(customSettings));

      expect(result.current.pageSetting).toEqual(customSettings);

      const updatedSettings: PageSetting = {
        page: 7,
        size: 25,
      };

      act(() => {
        result.current.setPageSetting(updatedSettings);
      });

      expect(result.current.pageSetting).toEqual(updatedSettings);
    });
  });

  describe('Тесты useEffect при изменении initialPageSettings', () => {
    it('обновляет pageSetting при изменении initialPageSettings', () => {
      const customSettings1: PageSetting = {
        page: 1,
        size: 15,
      };

      const customSettings2: PageSetting = {
        page: 3,
        size: 30,
      };

      const { result, rerender } = renderHook(
        (props) => usePagination(props.initialSettings),
        {
          initialProps: { initialSettings: customSettings1 },
        }
      );

      expect(result.current.pageSetting).toEqual(customSettings1);

      rerender({ initialSettings: customSettings2 });

      expect(result.current.pageSetting).toEqual(customSettings2);
    });
  });

  describe('Тесты возвращаемых значений', () => {
    it('возвращает структуру с pageSetting и setPageSetting', () => {
      const { result } = renderHook(() => usePagination());

      expect(result.current).toHaveProperty('pageSetting');
      expect(result.current).toHaveProperty('setPageSetting');

      expect(typeof result.current.setPageSetting).toBe('function');
    });

    it('pageSetting имеет правильную структуру PageSetting', () => {
      const { result } = renderHook(() => usePagination());

      expect(result.current.pageSetting).toHaveProperty('page');
      expect(result.current.pageSetting).toHaveProperty('size');
      expect(typeof result.current.pageSetting.page).toBe('number');
      expect(typeof result.current.pageSetting.size).toBe('number');
    });
  });
});
