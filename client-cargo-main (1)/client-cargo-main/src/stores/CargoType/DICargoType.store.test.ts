import { CargoTypeCategoryNameEnum, CargoTypeNameEnum } from './CargoType.interface';
import { DICargoTypeStore } from './DICargoType.store';

// Моки для зависимостей
const mockService = {
  searchCargoType: jest.fn(),
  postCargoType: jest.fn(),
};

describe('DICargoTypeStore', () => {
  let store: DICargoTypeStore;

  beforeEach(() => {
    jest.clearAllMocks();
    store = new DICargoTypeStore();
    // Перезаписываем service на мок
    (store as any).service = mockService;
  });

  describe('clearState', () => {
    it('should reset all observable properties', () => {
      // Given
      store.cargoType = {
        id: '1', name: 'Test', type: CargoTypeNameEnum.OTHER, category: CargoTypeCategoryNameEnum.REGULAR, length: 10, width: 10, height: 10, weight: 10, volume: 10,
      };
      store.cargoTypes = {
        1: {
          id: '1', name: 'Test', type: CargoTypeNameEnum.OTHER, category: CargoTypeCategoryNameEnum.REGULAR, length: 10, width: 10, height: 10, weight: 10, volume: 10,
        },
      };
      store.cargoTypeAutocompleteList = [{
        id: '1', name: 'Test', type: CargoTypeNameEnum.OTHER, category: CargoTypeCategoryNameEnum.REGULAR, length: 10, width: 10, height: 10, weight: 10, volume: 10,
      }];

      // When
      store.clearState();

      // Then
      expect(store.cargoType).toBeNull();
      expect(store.cargoTypes).toEqual({});
      expect(store.cargoTypeAutocompleteList).toEqual([]);
    });
  });

  describe('calculateCargo', () => {
    it('should convert dimensions from mm to cm and volume from mm3 to cm3', () => {
      // Given
      const cargo = {
        id: '1',
        name: 'Test',
        type: 'OTHER',
        category: 'REGULAR',
        length: 100, // 10 cm
        width: 50, // 5 cm
        height: 30, // 3 cm
        weight: 1000, // 1 kg
        volume: 150000, // 150 cm3 (150000 mm3)
      };

      // When
      const result = (DICargoTypeStore as any).calculateCargo(cargo);

      // Then
      expect(result).toEqual({
        id: '1',
        name: 'Test',
        type: 'OTHER',
        category: 'REGULAR',
        length: 10, // 100 / 10
        width: 5, // 50 / 10
        height: 3, // 30 / 10
        weight: 1000,
        volume: 150, // 150000 / 1000
      });
    });
  });

  describe('onCargoTypeSelect', () => {
    it('should select cargo type and convert dimensions', async () => {
      // Given
      const cargo = {
        id: '1',
        name: 'Test Cargo',
        type: CargoTypeNameEnum.OTHER,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 100,
        width: 50,
        height: 30,
        weight: 1000,
        volume: 150000,
      };
      store.cargoTypeAutocompleteList = [cargo];

      // When
      await (store as any).onCargoTypeSelect('Test Cargo');

      // Then
      expect(store.cargoType).toEqual({
        id: '1',
        name: 'Test Cargo',
        type: 'OTHER',
        category: 'REGULAR',
        length: 10, // converted
        width: 5, // converted
        height: 3, // converted
        weight: 1000,
        volume: 150, // converted
      });
      expect(store.cargoTypes['1']).toEqual(store.cargoType);
    });

    it('should not clear autocomplete list when dontClear is true', async () => {
      // Given
      const cargo = {
        id: '1',
        name: 'Test 1',
        type: CargoTypeNameEnum.OTHER,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 100,
        width: 100,
        height: 100,
        weight: 100,
        volume: 100000,
      };
      store.cargoTypeAutocompleteList = [cargo];

      // When
      await (store as any).onCargoTypeSelect('Test', true);

      // Then
      expect(store.cargoTypeAutocompleteList).toEqual([cargo]);
    });

    it('should not set cargoType if name not found', async () => {
      // Given
      store.cargoTypeAutocompleteList = [];

      // When
      await (store as any).onCargoTypeSelect('NonExistent');

      // Then
      expect(store.cargoType).toBeNull();
      expect(store.cargoTypes).toEqual({});
      expect(store.cargoTypeAutocompleteList).toEqual([]);
    });
  });

  describe('searchCargoType', () => {
    it('should search cargo types and set autocomplete list', async () => {
      // Given
      const mockResponse = [
        {
          id: '1', name: 'Test 1', type: 'OTHER', category: 'REGULAR', length: 10, width: 10, height: 10, weight: 10, volume: 10,
        },
        {
          id: '2', name: 'Test 2', type: 'TECHNIQUE', category: 'REGULAR', length: 20, width: 20, height: 20, weight: 20, volume: 20,
        },
      ];
      mockService.searchCargoType.mockResolvedValue(mockResponse);

      // When
      store.searchCargoType('test', 'org123');
      await new Promise(resolve => setTimeout(resolve, 550));

      // Then
      expect(mockService.searchCargoType).toHaveBeenCalledWith('test', 'org123');
      expect(store.cargoTypeAutocompleteList).toEqual(mockResponse);
    });

    it('should debounce search calls', async () => {
      // Given
      jest.useFakeTimers();
      mockService.searchCargoType.mockResolvedValue([]);

      // When
      store.searchCargoType('test1', 'org123');
      store.searchCargoType('test2', 'org123');
      jest.advanceTimersByTime(500);

      // Then
      expect(mockService.searchCargoType).toHaveBeenCalledTimes(1);
      expect(mockService.searchCargoType).toHaveBeenCalledWith('test2', 'org123');
      jest.useRealTimers();
    });
  });

  describe('postCargoType', () => {
    it('should call service postCargoType with correct params', async () => {
      // Given
      const data = {
        name: 'New Cargo',
        type: CargoTypeNameEnum.OTHER,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 10,
        width: 5,
        height: 3,
        weight: 100,
        volume: 150,
      };
      const organizationId = 'org123';

      mockService.postCargoType.mockResolvedValue({ id: 'new-id' });

      // When
      await store.postCargoType(data, organizationId);

      // Then
      expect(mockService.postCargoType).toHaveBeenCalledWith(data, organizationId);
    });
  });

  describe('clearAutocompleteList (private)', () => {
    it('should clear autocomplete list', () => {
      // Given
      const cargo1 = {
        id: '1',
        name: 'Test 1',
        type: CargoTypeNameEnum.OTHER,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 100,
        width: 100,
        height: 100,
        weight: 100,
        volume: 100000,
      };
      store.cargoTypeAutocompleteList = [cargo1];

      // When
      (store as any).clearAutocompleteList();

      // Then
      expect(store.cargoTypeAutocompleteList).toEqual([]);
    });
  });

  describe('edge cases', () => {
    it('should handle empty cargo type autocomplete list in onCargoTypeSelect', async () => {
      // Given
      store.cargoTypeAutocompleteList = [];

      // When
      await (store as any).onCargoTypeSelect('Any Name');

      // Then
      expect(store.cargoType).toBeNull();
      expect(store.cargoTypes).toEqual({});
    });

    it('should handle multiple selections with dontClear', async () => {
      // Given
      const cargo1 = {
        id: '1',
        name: 'Test 1',
        type: CargoTypeNameEnum.OTHER,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 100,
        width: 100,
        height: 100,
        weight: 100,
        volume: 100000,
      };
      const cargo2 = {
        id: '2',
        name: 'Test 2',
        type: CargoTypeNameEnum.TECHNIQUE,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 200,
        width: 200,
        height: 200,
        weight: 200,
        volume: 800000,
      };
      store.cargoTypeAutocompleteList = [cargo1, cargo2];

      // When
      await (store as any).onCargoTypeSelect('Test 1', true);
      await (store as any).onCargoTypeSelect('Test 2', true);

      // Then
      expect(store.cargoType).toEqual({
        id: '2',
        name: 'Test 2',
        type: 'TECHNIQUE',
        category: 'REGULAR',
        length: 20,
        width: 20,
        height: 20,
        weight: 200,
        volume: 800,
      });
      expect(store.cargoTypes['1']).toBeDefined();
      expect(store.cargoTypes['2']).toBeDefined();
    });

    it('should preserve cargoTypes when selecting different cargo types', async () => {
      // Given
      const cargo1 = {
        id: '1',
        name: 'Test 1',
        type: CargoTypeNameEnum.OTHER,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 100,
        width: 100,
        height: 100,
        weight: 100,
        volume: 100000,
      };
      const cargo2 = {
        id: '2',
        name: 'Test 2',
        type: CargoTypeNameEnum.TECHNIQUE,
        category: CargoTypeCategoryNameEnum.REGULAR,
        length: 200,
        width: 200,
        height: 200,
        weight: 200,
        volume: 800000,
      };
      store.cargoTypeAutocompleteList = [cargo1];

      // When
      await (store as any).onCargoTypeSelect('Test 1', true);
      store.cargoTypeAutocompleteList = [cargo2];
      await (store as any).onCargoTypeSelect('Test 2', true);

      // Then
      expect(store.cargoType).toEqual(expect.objectContaining({ id: '2' }));
      expect(store.cargoTypes['1']).toBeDefined();
      expect(store.cargoTypes['2']).toBeDefined();
    });
  });
});
