import { filterSession } from './filterStorage';
import type { Category } from '../../constants/Tabs';
import type { IFilter } from '../../interfaces/Filters.interface';
import moment from 'moment';
import { ignore } from 'utils';
import { OrderField } from '../../constants/Cargo/Cargo';

// Мокаем moment
jest.mock('moment', () => {
  return () => ({
    format: jest.fn(),
  });
});

// Мокаем sessionStorage глобально
const mockSessionStorage = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: jest.fn((key: string) => store[key] || null),
    setItem: jest.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: jest.fn((key: string) => {
      delete store[key];
    }),
    clear: jest.fn(() => {
      store = {};
    }),
    getStore: () => store,
  };
})();

Object.defineProperty(window, 'sessionStorage', {
  value: mockSessionStorage,
});

// Мокаем утилиту ignore
jest.mock('utils', () => ({
  ignore: jest.fn(),
}));


// Вспомогательная функция для создания корректных IFilter объектов
const createMockFilter = (overrides: Partial<IFilter> = {}): IFilter => {
  return {
    [OrderField.creationTime]: undefined,
    [OrderField.desiredTime]: undefined,
    ...overrides,
  } as IFilter;
};

// Создаем mock категории для тестов
const CATEGORY_1 = 'category1' as Category;
const CATEGORY_2 = 'category2' as Category;
const CATEGORY_3 = 'category3' as Category;

describe('filterSession', () => {
  const STORAGE_KEY = 'orderExecutionFilter';
  
  beforeEach(() => {
    // Очищаем моки и sessionStorage перед каждым тестом
    mockSessionStorage.clear();
    jest.clearAllMocks();
  });

  describe('Получение данных(геттер)', () => {
    it('должен возвращать null при пустом sessionStorage', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      const result = filterSession.data;
      expect(result).toBeNull();
      expect(sessionStorage.getItem).toHaveBeenCalledWith(STORAGE_KEY);
    });

    it('должен возвращать распарсенные данные при их наличии', () => {
      const mockData: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'test',
          [OrderField.status]: 'active'
        }) 
      };
      mockSessionStorage.getItem.mockReturnValue(JSON.stringify(mockData));

      const result = filterSession.data;

      expect(result).toEqual(mockData);
      expect(sessionStorage.getItem).toHaveBeenCalledWith(STORAGE_KEY);
    });

    it('должен возвращать null при ошибке парсинга JSON', () => {
      mockSessionStorage.getItem.mockReturnValue('invalid json');

      const result = filterSession.data;

      expect(result).toBeNull();
      expect(sessionStorage.getItem).toHaveBeenCalledWith(STORAGE_KEY);
    });

    it('должен возвращать null при исключении в sessionStorage', () => {
      mockSessionStorage.getItem.mockImplementation(() => {
        throw new Error('Storage error');
      });

      const result = filterSession.data;

      expect(result).toBeNull();
    });
  });

  describe('Установка данных(сеттер)', () => {
    it('должен сохранять фильтр в sessionStorage', () => {
      const newFilter: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({
          [OrderField.senderName]: 'test',
          [OrderField.status]: 'active',
          humanReadableId: '2024-01-01'
        })
      };

      filterSession.data = newFilter;

      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify(newFilter)
      );
    });

    it('должен мерджить новые фильтры с существующими', () => {
      const existingData: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'old'
        }),
        [CATEGORY_2]: createMockFilter({ 
          [OrderField.status]: 'pending'
        })
      };
      const newFilter: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'new'
        }),
        [CATEGORY_3]: createMockFilter({ 
          humanReadableId: '2024-01-01'
        })
      };
      
      mockSessionStorage.getItem.mockReturnValue(JSON.stringify(existingData));

      filterSession.data = newFilter;

      const expectedData = {
        ...existingData,
        ...newFilter,
      };
      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify(expectedData)
      );
    });

    it('должен создавать новую запись если нет существующих данных', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      const newFilter: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'test'
        })
      };

      filterSession.data = newFilter;

      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify(newFilter)
      );
    });

    it('должен обрабатывать null значение', () => {
      const existingData: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'test'
        })
      };
      mockSessionStorage.getItem.mockReturnValue(JSON.stringify(existingData));

      filterSession.data = null;

      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify(existingData) // Должен сохранить существующие данные без изменений
      );
    });

    it('должен вызывать ignore() при ошибке записи', () => {
      mockSessionStorage.setItem.mockImplementation(() => {
        throw new Error('Storage write error');
      });
      const newFilter: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter() 
      };

      filterSession.data = newFilter;

      expect(ignore).toHaveBeenCalled();
    });

    it('должен корректно обрабатывать пустой объект', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      const newFilter: Partial<Record<Category, IFilter>> = {};
  
      filterSession.data = newFilter;
  
      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify({})
      );
    });

    it('должен сохранять пустой объект при отсутствии существующих данных', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      const newFilter: Partial<Record<Category, IFilter>> = {};
  
      filterSession.data = newFilter;
  
      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify({})
      );
    });
  
    it('должен сохранять существующие данные при передаче пустого объекта', () => {
      const existingData: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({
          [OrderField.senderName]: 'existing'
        })
      };
      mockSessionStorage.getItem.mockReturnValue(JSON.stringify(existingData));
      const newFilter: Partial<Record<Category, IFilter>> = {};
  
      filterSession.data = newFilter;
  
      // Должен сохранить существующие данные, так как пустой объект не перезаписывает их
      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify(existingData)
      );
    });

    it('должен сохранять несколько категорий с фильтрами', () => {
      const categories: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'test1', 
          [OrderField.status]: 'active' 
        }),
        [CATEGORY_2]: createMockFilter({ 
          [OrderField.senderName]: 'test2',
          humanReadableId: '2024-12-31'
        }),
        [CATEGORY_3]: createMockFilter({ 
          sortSetting: { property: 'date', directionAsc: false }
        }),
      };

      filterSession.data = categories;

      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        JSON.stringify(categories)
      );
    });

    it('должен перезаписывать существующие данные при последующем вызове', () => {
      const firstFilter: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_1]: createMockFilter({ 
          [OrderField.senderName]: 'first' 
        })
      };
      const secondFilter: Partial<Record<Category, IFilter>> = { 
        [CATEGORY_2]: createMockFilter({ 
          [OrderField.senderName]: 'second' 
        })
      };
    
      // Симулируем пустое хранилище для первого вызова
      mockSessionStorage.getItem.mockReturnValue(null);
      filterSession.data = firstFilter;
    
      // Теперь getItem должен возвращать то, что записалось
      mockSessionStorage.getItem.mockReturnValue(JSON.stringify(firstFilter));
      filterSession.data = secondFilter;
    
      expect(sessionStorage.setItem).toHaveBeenCalledTimes(2);
      
      // Второй вызов должен содержать мердж обоих фильтров
      const secondCall = (sessionStorage.setItem as jest.Mock).mock.calls[1];
      const savedData = JSON.parse(secondCall[1]);
      
      expect(savedData[CATEGORY_1][OrderField.senderName]).toBe('first');
      expect(savedData[CATEGORY_2][OrderField.senderName]).toBe('second');
    });

    it('должен корректно обрабатывать массивы в статусах', () => {
      mockSessionStorage.getItem.mockReturnValue(null); // Убеждаемся, что нет данных
      mockSessionStorage.clear(); // Очищаем хранилище
      
      const newFilter: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({
          [OrderField.status]: ['active', 'pending', 'completed']
        })
      };
    
      filterSession.data = newFilter;
      const setItemCalls = (sessionStorage.setItem as jest.Mock).mock.calls;
      expect(setItemCalls).toHaveLength(1);
      
      const [key, value] = setItemCalls[0];
      expect(key).toBe(STORAGE_KEY);
      
      const storedValue = JSON.parse(value);
      expect(storedValue[CATEGORY_1][OrderField.status]).toEqual(['active', 'pending', 'completed']);
    });
    
    it('должен корректно обрабатывать объекты дат', () => {
      mockSessionStorage.getItem.mockReturnValue(null); // Убеждаемся, что нет данных
      mockSessionStorage.clear(); // Очищаем хранилище
      
      const mockMoment = moment();
      const newFilter: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({
          [OrderField.creationTime]: {
            value: [mockMoment, mockMoment]
          },
          [OrderField.desiredTime]: {
            value: [mockMoment, mockMoment]
          }
        })
      };
    
      filterSession.data = newFilter;
      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        expect.any(String)
      );
      
      const setItemCalls = (sessionStorage.setItem as jest.Mock).mock.calls;
      const lastCall = setItemCalls[setItemCalls.length - 1];
      const storedValue = JSON.parse(lastCall[1]);
      
      expect(storedValue[CATEGORY_1][OrderField.creationTime]).toBeDefined();
      expect(storedValue[CATEGORY_1][OrderField.desiredTime]).toBeDefined();
      expect(storedValue[CATEGORY_1][OrderField.creationTime]).toHaveProperty('value');
      expect(storedValue[CATEGORY_1][OrderField.desiredTime]).toHaveProperty('value');
    });
  });

  describe('интеграционные тесты', () => {
    beforeEach(() => {
      // Очищаем хранилище, но не сбрасываем полностью моки
      mockSessionStorage.clear();
      // Только сбрасываем вызовы, но не реализацию
      (sessionStorage.getItem as jest.Mock).mockClear();
      (sessionStorage.setItem as jest.Mock).mockClear();
    });
  
    it('должен корректно сохранять и восстанавливать данные', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      
      const originalFilter: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({
          [OrderField.senderName]: 'integration test',
          [OrderField.status]: 'completed',
          pageSetting: { page: 2, size: 20 }
        }),
        [CATEGORY_2]: createMockFilter({
          humanReadableId: 'ORD-2024-001'
        })
      };
      filterSession.data = originalFilter;
      
      // Получаем данные из setItem
      const setItemCalls = (sessionStorage.setItem as jest.Mock).mock.calls;
      expect(setItemCalls).toHaveLength(1);
      const storedJson = setItemCalls[0][1];
      
      // Сбрасываем счетчики вызовов, но сохраняем реализацию моков
      (sessionStorage.getItem as jest.Mock).mockClear();
      (sessionStorage.setItem as jest.Mock).mockClear();
      
      // Настраиваем getItem на возврат сохраненного значения
      mockSessionStorage.getItem.mockReturnValue(storedJson);
      const restoredData = filterSession.data;
  
      expect(restoredData).toEqual(originalFilter);
      expect(sessionStorage.getItem).toHaveBeenCalledWith(STORAGE_KEY);
    });
  
    it('должен поддерживать сложные структуры фильтров', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      
      const complexFilter: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({
          [OrderField.senderName]: 'complex',
          [OrderField.status]: ['active', 'pending'],
          pageSetting: { page: 1, size: 50 },
          sortSetting: { property: OrderField.creationTime, directionAsc: false },
          customField: 'custom value'
        })
      };
  
      filterSession.data = complexFilter;
      
      // Получаем данные из setItem
      const setItemCalls = (sessionStorage.setItem as jest.Mock).mock.calls;
      expect(setItemCalls).toHaveLength(1);
      const storedString = setItemCalls[0][1];
      const storedValue = JSON.parse(storedString);
  
      expect(storedValue).toEqual(complexFilter);
      expect(storedValue[CATEGORY_1][OrderField.status]).toEqual(['active', 'pending']);
      expect(storedValue[CATEGORY_1].pageSetting?.size).toBe(50);
    });
  
    it('должен корректно работать с getStore мока', () => {
      mockSessionStorage.getItem.mockReturnValue(null);
      
      const testFilter: Partial<Record<Category, IFilter>> = {
        [CATEGORY_1]: createMockFilter({
          [OrderField.senderName]: 'test',
          [OrderField.status]: ['active', 'pending']
        })
      };
      filterSession.data = testFilter;
      
      // Проверяем, что setItem вызывался с правильными данными
      expect(sessionStorage.setItem).toHaveBeenCalledTimes(1);
      expect(sessionStorage.setItem).toHaveBeenCalledWith(
        STORAGE_KEY,
        expect.any(String)
      );
      
      // Проверяем, что данные сохранились через мок setItem
      const setItemCall = (sessionStorage.setItem as jest.Mock).mock.calls[0];
      const storedJson = setItemCall[1];
      
      // Парсим и проверяем
      const parsedData = JSON.parse(storedJson);
      expect(parsedData).toEqual(testFilter);
      
      // Также можно проверить через getItem
      mockSessionStorage.getItem.mockReturnValue(storedJson);
      const restored = filterSession.data;
      expect(restored).toEqual(testFilter);
    });
  
    it('должен сохранять данные между вызовами через getStore', () => {
      let store: Record<string, string> = {};
      
      (sessionStorage.setItem as jest.Mock).mockImplementation((key: string, value: string) => {
        store[key] = value;
      });
      
      (sessionStorage.getItem as jest.Mock).mockImplementation((key: string) => {
        return store[key] || null;
      });
      
      try {
        const testData: Partial<Record<Category, IFilter>> = {
          [CATEGORY_1]: createMockFilter({
            [OrderField.senderName]: 'persistent'
          })
        };
        filterSession.data = testData;
        
        // Проверяем, что данные сохранились
        const storedInStore = store[STORAGE_KEY];
        expect(storedInStore).toBeDefined();
        const restored = filterSession.data;
        
        expect(restored).toEqual(testData);
        expect(sessionStorage.setItem).toHaveBeenCalledWith(STORAGE_KEY, expect.any(String));
        expect(sessionStorage.getItem).toHaveBeenCalledWith(STORAGE_KEY);
      } finally {
        // Восстанавливаем оригинальные моки
        store = {};
      }
    });
  });
});