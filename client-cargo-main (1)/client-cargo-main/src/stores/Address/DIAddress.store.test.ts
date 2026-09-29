import { SYSTEM_MESSAGES } from 'constants/constants.app';

import { DIAddressStore } from './DIAddress.store';
import { AddressModel } from './models/Address.model';
import { AddressNewModel } from './models/AddressNew.model';

// Моки для зависимостей
const mockService = {
  getAddressList: jest.fn(),
  getFrequentList: jest.fn(),
  getFavoriteList: jest.fn(),
  createFavoriteAddress: jest.fn(),
  deleteFavoriteAddress: jest.fn(),
  deleteFrequentAddress: jest.fn(),
};

const mockLogger = {
  toMessage: jest.fn(),
};

const mockProcess = {
  processStatus: jest.fn(),
};

describe('DIAddressStore', () => {
  let store: DIAddressStore;

  beforeEach(() => {
    jest.clearAllMocks();
    store = new DIAddressStore();
    // Перезаписываем зависимости на моки
    (store as any).service = mockService;
    (store as any).logger = mockLogger;
    (store as any).process = mockProcess;
  });

  describe('addressExistLabels', () => {
    it('should return labels from favorite list', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
        {
          id: '2', label: 'Работа', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        } as AddressModel,
      ];

      // When
      const result = store.addressExistLabels;

      // Then
      expect(result).toEqual(['Дом', 'Работа']);
    });

    it('should filter out non-string labels', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
        {
          id: '2', label: undefined as any, country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        } as AddressModel,
        {
          id: '3', label: 123 as any, country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        } as AddressModel,
      ];

      // When
      const result = store.addressExistLabels;

      // Then
      expect(result).toEqual(['Дом']);
    });
  });

  describe('loadAddressList', () => {
    it('should load address list from service', async () => {
      // Given
      const mockAddresses = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        },
        {
          id: '2', label: 'Работа', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        },
      ];
      mockService.getAddressList.mockResolvedValue(mockAddresses);

      // When
      await store.loadAddressList();

      // Then
      expect(mockService.getAddressList).toHaveBeenCalled();
      expect(store.selfAddressList).toHaveLength(2);
      expect(store.selfAddressList[0]).toBeInstanceOf(AddressModel);
      expect(store.selfAddressList[0].id).toBe('1');
    });

    it('should handle empty response', async () => {
      // Given
      mockService.getAddressList.mockResolvedValue(undefined);

      // When
      await store.loadAddressList();

      // Then
      expect(store.selfAddressList).toEqual([]);
    });
  });

  describe('loadFavoriteList', () => {
    it('should load favorite list from service', async () => {
      // Given
      const mockFavorites = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        },
      ];
      mockService.getFavoriteList.mockResolvedValue(mockFavorites);

      // When
      await store.loadFavoriteList();

      // Then
      expect(mockService.getFavoriteList).toHaveBeenCalled();
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.selfFavoriteList[0]).toBeInstanceOf(AddressModel);
    });

    it('should handle empty response', async () => {
      // Given
      mockService.getFavoriteList.mockResolvedValue(undefined);

      // When
      await store.loadFavoriteList();

      // Then
      expect(store.selfFavoriteList).toEqual([]);
    });
  });

  describe('loadFrequentList', () => {
    it('should load frequent list from service', async () => {
      // Given
      const mockFrequents = [
        {
          id: '1', label: 'Частый адрес', country: 'Россия', city: 'Санкт-Петербург', street: 'Невский', house: '3',
        },
      ];
      mockService.getFrequentList.mockResolvedValue(mockFrequents);

      // When
      await store.loadFrequentList();

      // Then
      expect(mockService.getFrequentList).toHaveBeenCalled();
      expect(store.selfFrequentList).toHaveLength(1);
      expect(store.selfFrequentList[0]).toBeInstanceOf(AddressModel);
    });

    it('should handle empty response', async () => {
      // Given
      mockService.getFrequentList.mockResolvedValue(undefined);

      // When
      await store.loadFrequentList();

      // Then
      expect(store.selfFrequentList).toEqual([]);
    });
  });

  describe('addFavoriteAddress', () => {
    it('should not add address if label is not unique', async () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        label: 'Дом', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
      });

      // When
      await store.addFavoriteAddress(newItem);

      // Then
      expect(mockService.createFavoriteAddress).not.toHaveBeenCalled();
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', SYSTEM_MESSAGES.addressLableIsNotUniq);
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.isAddressChanged).toBeUndefined();
    });

    it('should not add address if address already exists', async () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        label: 'Дом 2', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
      });

      // When
      await store.addFavoriteAddress(newItem);

      // Then
      expect(mockService.createFavoriteAddress).not.toHaveBeenCalled();
      expect(mockLogger.toMessage).toHaveBeenCalledWith('error', SYSTEM_MESSAGES.addressIsNotUniq);
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.isAddressChanged).toBeUndefined();
    });

    it('should add new favorite address successfully', async () => {
      // Given
      const newItem = new AddressNewModel({
        label: 'Новый адрес', country: 'Россия', city: 'Москва', street: 'Тверская', house: '5',
      });
      const mockResult = {
        id: 'new-id', label: 'Новый адрес', country: 'Россия', city: 'Москва', street: 'Тверская', house: '5',
      };
      mockService.createFavoriteAddress.mockResolvedValue(mockResult);
      mockProcess.processStatus.mockReturnValue(200);

      // When
      await store.addFavoriteAddress(newItem);

      // Then
      expect(mockService.createFavoriteAddress).toHaveBeenCalledWith(newItem);
      expect(mockProcess.processStatus).toHaveBeenCalledWith(200, SYSTEM_MESSAGES.addressAddSuccess);
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.selfFavoriteList[0]).toBeInstanceOf(AddressModel);
      expect(store.selfFavoriteList[0].id).toBe('new-id');
      expect(store.selfFavoriteList[0].label).toBe('Новый адрес');
      expect(store.isAddressChanged).toBe(200);
    });

    it('should handle failed address creation', async () => {
      // Given
      const newItem = new AddressNewModel({
        label: 'Новый адрес', country: 'Россия', city: 'Москва', street: 'Тверская', house: '5',
      });
      mockService.createFavoriteAddress.mockResolvedValue(undefined);
      mockProcess.processStatus.mockReturnValue(200);

      // When
      await store.addFavoriteAddress(newItem);

      // Then
      expect(mockService.createFavoriteAddress).toHaveBeenCalled();
      expect(mockProcess.processStatus).not.toHaveBeenCalled();
      expect(store.selfFavoriteList).toHaveLength(0);
      expect(store.isAddressChanged).toBeUndefined();
    });
  });

  describe('deleteFrequentAddress', () => {
    it('should delete frequent address and update isAddressChanged', async () => {
      // Given
      store.selfFrequentList = [
        {
          id: '1', label: 'Адрес 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
        {
          id: '2', label: 'Адрес 2', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        } as AddressModel,
      ];
      mockService.deleteFrequentAddress.mockResolvedValue(true);
      mockProcess.processStatus.mockReturnValue(true);

      // When
      await store.deleteFrequentAddress('1');

      // Then
      expect(mockService.deleteFrequentAddress).toHaveBeenCalledWith('1');
      expect(mockProcess.processStatus).toHaveBeenCalledWith(true, SYSTEM_MESSAGES.addressDeleteSuccess);
      expect(store.selfFrequentList).toHaveLength(1);
      expect(store.selfFrequentList.find(x => x.id === '1')).toBeUndefined();
      expect(store.isAddressChanged).toBe(true);
    });

    it('should not delete if address not found', async () => {
      // Given
      store.selfFrequentList = [
        {
          id: '1', label: 'Адрес 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      mockService.deleteFrequentAddress.mockResolvedValue(true);

      // When
      await store.deleteFrequentAddress('999');

      // Then
      expect(mockService.deleteFrequentAddress).toHaveBeenCalledWith('999');
      expect(store.selfFrequentList).toHaveLength(1);
      expect(store.isAddressChanged).toBe(true);
    });

    it('should not delete if service returns falsy result', async () => {
      // Given
      store.selfFrequentList = [
        {
          id: '1', label: 'Адрес 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      mockService.deleteFrequentAddress.mockResolvedValue(undefined);
      mockProcess.processStatus.mockReturnValue(false);

      // When
      await store.deleteFrequentAddress('1');

      // Then
      expect(mockService.deleteFrequentAddress).toHaveBeenCalledWith('1');
      expect(mockProcess.processStatus).toHaveBeenCalledWith(undefined, SYSTEM_MESSAGES.addressDeleteSuccess);
      expect(store.selfFrequentList).toHaveLength(1);
      expect(store.isAddressChanged).toBe(false);
    });
  });

  describe('deleteFavoriteAddress', () => {
    it('should delete favorite address and update isAddressChanged', async () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Адрес 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
        {
          id: '2', label: 'Адрес 2', country: 'Россия', city: 'Москва', street: 'Пушкина', house: '2',
        } as AddressModel,
      ];
      mockService.deleteFavoriteAddress.mockResolvedValue(true);
      mockProcess.processStatus.mockReturnValue(true);

      // When
      await store.deleteFavoriteAddress('1');

      // Then
      expect(mockService.deleteFavoriteAddress).toHaveBeenCalledWith('1');
      expect(mockProcess.processStatus).toHaveBeenCalledWith(true, SYSTEM_MESSAGES.addressDeleteSuccess);
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.selfFavoriteList.find(x => x.id === '1')).toBeUndefined();
      expect(store.isAddressChanged).toBe(true);
    });

    it('should not delete if address not found', async () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Адрес 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      mockService.deleteFavoriteAddress.mockResolvedValue(true);

      // When
      await store.deleteFavoriteAddress('999');

      // Then
      expect(mockService.deleteFavoriteAddress).toHaveBeenCalledWith('999');
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.isAddressChanged).toBe(true);
    });

    it('should not delete if service returns falsy result', async () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Адрес 1', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      mockService.deleteFavoriteAddress.mockResolvedValue(undefined);
      mockProcess.processStatus.mockReturnValue(false);

      // When
      await store.deleteFavoriteAddress('1');

      // Then
      expect(mockService.deleteFavoriteAddress).toHaveBeenCalledWith('1');
      expect(mockProcess.processStatus).toHaveBeenCalledWith(undefined, SYSTEM_MESSAGES.addressDeleteSuccess);
      expect(store.selfFavoriteList).toHaveLength(1);
      expect(store.isAddressChanged).toBe(false);
    });
  });

  describe('checkLableUniqness', () => {
    it('should return true for unique label', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];

      // When
      const result = store.checkLableUniqness('Работа');

      // Then
      expect(result).toBe(true);
    });

    it('should return false for non-unique label', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];

      // When
      const result = store.checkLableUniqness('Дом');

      // Then
      expect(result).toBe(false);
    });

    it('should return true when favorite list is empty', () => {
      // Given
      store.selfFavoriteList = [];

      // When
      const result = store.checkLableUniqness('Любой');

      // Then
      expect(result).toBe(true);
    });
  });

  describe('isUnique', () => {
    it('should return false for unique address (different street)', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1',
          label: 'Дом',
          country: 'Россия',
          city: 'Москва',
          street: 'Ленина',
          house: '1',
          building: '2',
          structure: '3',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Пушкина',
        house: '1',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return true for duplicate address with same country, city, house, street, building, structure', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1',
          label: 'Дом',
          country: 'Россия',
          city: 'Москва',
          street: 'Ленина',
          house: '1',
          building: '2',
          structure: '3',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Ленина',
        house: '1',
        building: '2',
        structure: '3',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(true);
    });

    it('should return false if only country differs', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Казахстан',
        city: 'Москва',
        street: 'Ленина',
        house: '1',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return false if only city differs', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Санкт-Петербург',
        street: 'Ленина',
        house: '1',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return false if only house differs', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Ленина',
        house: '5',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return false if only street differs', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1', label: 'Дом', country: 'Россия', city: 'Москва', street: 'Ленина', house: '1',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Пушкина',
        house: '1',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return false if only building differs', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1',
          label: 'Дом',
          country: 'Россия',
          city: 'Москва',
          street: 'Ленина',
          house: '1',
          building: '2',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Ленина',
        house: '1',
        building: '3',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return false if only structure differs', () => {
      // Given
      store.selfFavoriteList = [
        {
          id: '1',
          label: 'Дом',
          country: 'Россия',
          city: 'Москва',
          street: 'Ленина',
          house: '1',
          structure: '2',
        } as AddressModel,
      ];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Ленина',
        house: '1',
        structure: '3',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });

    it('should return false when favorite list is empty', () => {
      // Given
      store.selfFavoriteList = [];
      const newItem = new AddressNewModel({
        country: 'Россия',
        city: 'Москва',
        street: 'Ленина',
        house: '1',
      });

      // When
      const result = store.isUnique(newItem);

      // Then
      expect(result).toBe(false);
    });
  });

  describe('initStore', () => {
    it('should load favorite and frequent lists', () => {
      // Given
      mockService.getFavoriteList.mockResolvedValue([]);
      mockService.getFrequentList.mockResolvedValue([]);

      // When
      store.initStore();

      // Then
      expect(mockService.getFavoriteList).toHaveBeenCalled();
      expect(mockService.getFrequentList).toHaveBeenCalled();
    });
  });
});
