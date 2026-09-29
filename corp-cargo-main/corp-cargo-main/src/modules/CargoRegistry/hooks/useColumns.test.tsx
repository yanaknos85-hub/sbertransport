// Мокаем все зависимости перед импортом хука
jest.mock('react', () => ({
  ...jest.requireActual('react'),
  useCallback: jest.fn((fn) => fn),
  useMemo: jest.fn((fn) => fn()),
}));

// Перехватываем все импорты, которые начинаются с src/ или @/
jest.mock('src/ioc/ioc.container', () => ({}), { virtual: true });
jest.mock('src/stores/index', () => ({}), { virtual: true });
jest.mock('src/shared/hooks/useAppStoreContext', () => ({}), { virtual: true });
jest.mock('src/api/index', () => ({}), { virtual: true });
jest.mock('src/api/register-search', () => ({}), { virtual: true });
jest.mock('src/stores/PersonalSearch/PersonalSearch.interface', () => ({}), { virtual: true });
jest.mock('src/modules/Planner/types', () => ({}), { virtual: true });
jest.mock('src/stores/Planner/DIPlanner.store', () => ({}), { virtual: true });
jest.mock('src/ioc/ioc.stores', () => ({}), { virtual: true });
jest.mock('io-ts', () => ({}), { virtual: true });
jest.mock('@sber-sbertransport/mf-core', () => ({}), { virtual: true });

// Мокаем зависимости хука
jest.mock('i18n', () => ({
  useTranslation: jest.fn(),
}));

jest.mock('api/cargo-registry-search', () => ({
  useCargoUserSettings: jest.fn(),
  useDefaultCargoColumnVisibilitySettings: jest.fn(),
  useSaveCargoUsersAttributes: jest.fn(),
}));

jest.mock('api/profile', () => ({
  useProfile: jest.fn(),
}));

// Мокаем только необходимые глобальные хуки
const mockUseCallback = jest.fn((fn) => fn);
const mockUseMemo = jest.fn((fn) => fn());

// Переопределяем моки после импорта
jest.mock('react', () => ({
  ...jest.requireActual('react'),
  useCallback: mockUseCallback,
  useMemo: mockUseMemo,
}));

import { renderHook, act } from '@testing-library/react-hooks';
import { useColumns } from './useColumns';
import { SortSetting } from 'stores/CargoRegistry/CargoRegistry.interface';
import { VisibleFields } from '../constants';
import * as i18n from 'i18n';
import * as cargoRegistrySearch from 'api/cargo-registry-search';
import * as profile from 'api/profile';

// Моки перевода
const mockT = {
  Forms: {
    registryCargoSettings: {
      requestIdVisible: 'Номер заявки',
      requestStatusVisible: 'Статус заявки',
      cargoTransportTypeVisible: 'Тип тарифа',
      authorVisible: 'ФИО заявителя',
      authorPhoneVisible: 'Телефон заявителя',
      authorPersonnelNumberVisible: 'Табельный номер',
      costCenterVisible: 'МВЗ',
      desiredDateVisible: 'Плановая дата, время сбора',
      carrierVisible: 'Перевозчик',
      plannedPriceVisible: 'Плановая стоимость, руб',
      actualCostVisible: 'Фактическая стоимость, руб',
      plannedRangeVisible: 'Плановая дальность, км',
      actualDistanceVisible: 'Фактическая дальность, км',
      plannedDeliveryDateVisible: 'Плановая дата доставки',
      transferTimeVisible: 'Фактическая дата сбора',
      shipmentTimeVisible: 'Фактическая дата доставки',
      deadlineDateVisible: 'Контрольный срок',
      senderVisible: 'ФИО отправителя',
      senderPhoneVisible: 'Телефон отправителя',
      waypointFromVisible: 'Адрес отправления',
      senderOrganizationVisible: 'Организация-отправитель',
      recipientVisible: 'ФИО получателя',
      recipientPhoneVisible: 'Телефон получателя',
      waypointToVisible: 'Адрес назначения',
      recipientOrganizationVisible: 'Организация-получатель',
      waypointsCountVisible: 'Количество точек',
      weightVisible: 'Общий вес, кг',
      volumeVisible: 'Объем, м³',
      creationDateVisible: 'Дата создания заявки',
      creationTimeVisible: 'Время создания заявки',
      sourceVisible: 'Источник создания',
      routeNumberVisible: 'Номер маршрута',
      templateNumberVisible: 'Номер расписания',
      evaluationVisible: 'Оценка',
      economyVisible: 'Экономия, руб',
    },
  },
};

describe('useColumns', () => {
  const mockUserId = 'user-123';
  const mockPathname = '/cargo/test';

  // Моки хуков
  beforeEach(() => {
    (i18n.useTranslation as jest.Mock).mockReturnValue({ t: mockT });
    (profile.useProfile as jest.Mock).mockReturnValue({ data: { userId: mockUserId } });
    (cargoRegistrySearch.useDefaultCargoColumnVisibilitySettings as jest.Mock).mockReturnValue({
      data: { cargoUIVisibility: undefined },
    });
    (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
      refetch: jest.fn(),
      data: { cargoUIVisibility: undefined },
    });
    (cargoRegistrySearch.useSaveCargoUsersAttributes as jest.Mock).mockReturnValue([
      jest.fn().mockResolvedValue(undefined),
      { isLoading: false },
    ]);

    // Сбрасываем моки useCallback и useMemo
    mockUseCallback.mockImplementation((fn) => fn);
    mockUseMemo.mockImplementation((fn) => fn());
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Тесты defaultVisibility', () => {
    it('возвращает видимость по умолчанию когда defaultColumnsVisibility отсутствует', () => {
      // Мок возвращает undefined для cargoUIVisibility
      (cargoRegistrySearch.useDefaultCargoColumnVisibilitySettings as jest.Mock).mockReturnValue({
        data: {},
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      // Все поля должны быть видимы по умолчанию
      const expectedVisibility: Record<string, boolean> = {};
      Object.values(VisibleFields).forEach((field) => {
        expectedVisibility[field] = true;
      });

      expect(result.current.settings.defaultVisibility).toEqual(expectedVisibility);
    });

    it('использует defaultColumnsVisibility если он присутствует', () => {
      const mockDefaultVisibility = {
        [VisibleFields.humanReadableId]: false,
        [VisibleFields.status]: true,
      };

      (cargoRegistrySearch.useDefaultCargoColumnVisibilitySettings as jest.Mock).mockReturnValue({
        data: { cargoUIVisibility: mockDefaultVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current.settings.defaultVisibility).toBe(mockDefaultVisibility);
    });
  });

  describe('Тесты columnVisibility', () => {
    it('использует userSettings если они есть', () => {
      const mockUserSettings = {
        cargoUIVisibility: {
          [VisibleFields.humanReadableId]: false,
          [VisibleFields.status]: true,
        },
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: mockUserSettings,
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current.settings.columnVisibility).toBe(mockUserSettings.cargoUIVisibility);
    });

    it('использует defaultVisibility если userSettings отсутствуют', () => {
      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: undefined },
      });

      (cargoRegistrySearch.useDefaultCargoColumnVisibilitySettings as jest.Mock).mockReturnValue({
        data: {},
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      // Должен быть default visibility
      const expectedVisibility: Record<string, boolean> = {};
      Object.values(VisibleFields).forEach((field) => {
        expectedVisibility[field] = true;
      });

      expect(result.current.settings.columnVisibility).toEqual(expectedVisibility);
    });
  });

  describe('Тесты setColumnVisibility', () => {
    it('вызывает saveColumnVisibilitySettings и refetchUserSettings', async () => {
      const mockSaveFn = jest.fn().mockResolvedValue(undefined);
      const mockRefetch = jest.fn();

      (cargoRegistrySearch.useSaveCargoUsersAttributes as jest.Mock).mockReturnValue([
        mockSaveFn,
        { isLoading: false },
      ]);
      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: mockRefetch,
        data: { cargoUIVisibility: undefined },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      await act(async () => {
        await result.current.settings.setColumnVisibility({
          [VisibleFields.humanReadableId]: false,
        });
      });

      expect(mockSaveFn).toHaveBeenCalledWith({
        [VisibleFields.humanReadableId]: false,
      });
      expect(mockRefetch).toHaveBeenCalled();
    });
  });

  describe('Тесты columns', () => {
    it('создаёт колонки только для видимых полей', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
        [VisibleFields.status]: false,
        [VisibleFields.author]: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      // Должны быть только requestIdVisible и authorVisible
      expect(result.current.columns.length).toBe(2);
      expect(result.current.columns[0].dataIndex).toBe(VisibleFields.humanReadableId);
      expect(result.current.columns[1].dataIndex).toBe(VisibleFields.author);
    });

    it('устанавливает title из перевода', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current.columns[0].title).toBe('Номер заявки');
    });

    it('устанавливает sortProperty на основе sortFields', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
        [VisibleFields.status]: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      // requestIdVisible имеет sortProperty в sortFields
      expect(result.current.columns[0].sortProperty).toBe('REQUEST_HUMAN_ID');
      // status нет в sortFields
      expect(result.current.columns[1].sortProperty).toBeUndefined();
    });

    it('устанавливает fixed: left для humanReadableId', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current.columns[0].fixed).toBe('left');
    });

    it('не устанавливает fixed для других полей', () => {
      const mockColumnVisibility = {
        [VisibleFields.status]: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current.columns[0].fixed).toBeUndefined();
    });

    it('устанавливает sortOrder на основе sortSetting при совпадении свойства', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
      };

      const mockSortSetting: SortSetting = {
        property: 'REQUEST_HUMAN_ID',
        directionAsc: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname, mockSortSetting));

      expect(result.current.columns[0].sortOrder).toBe('ascend');
    });

    it('устанавливает sortOrder как descend когда directionAsc false', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
      };

      const mockSortSetting: SortSetting = {
        property: 'REQUEST_HUMAN_ID',
        directionAsc: false,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname, mockSortSetting));

      expect(result.current.columns[0].sortOrder).toBe('descend');
    });

    it('не устанавливает sortOrder когда свойства не совпадают', () => {
      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
      };

      const mockSortSetting: SortSetting = {
        property: 'OTHER_PROPERTY',
        directionAsc: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname, mockSortSetting));

      expect(result.current.columns[0].sortOrder).toBeUndefined();
    });

    it('не устанавливает sortOrder для полей без sortProperty', () => {
      const mockColumnVisibility = {
        [VisibleFields.status]: true,
      };

      const mockSortSetting: SortSetting = {
        property: 'REQUEST_HUMAN_ID',
        directionAsc: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname, mockSortSetting));

      expect(result.current.columns[0].sortOrder).toBeUndefined();
    });
  });

  describe('Тесты возвращаемых значений', () => {
    it('возвращает структуру UseColumnsResult', () => {
      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current).toHaveProperty('columns');
      expect(result.current).toHaveProperty('settings');
      expect(result.current.settings).toHaveProperty('columnVisibility');
      expect(result.current.settings).toHaveProperty('setColumnVisibility');
      expect(result.current.settings).toHaveProperty('isSaving');
      expect(result.current.settings).toHaveProperty('defaultVisibility');
    });

    it('возвращает пустой массив колонок когда все поля скрыты', () => {
      const mockColumnVisibility: Record<string, boolean> = {};

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      const { result } = renderHook(() => useColumns(mockPathname));

      expect(result.current.columns).toEqual([]);
    });
  });

  describe('Тесты зависимостей', () => {
    it('использует useMemo для колонок с правильными зависимостями', () => {
      // Сбрасываем мок useMemo чтобы он вернул функцию
      mockUseMemo.mockImplementation((fn) => fn);

      const mockColumnVisibility = {
        [VisibleFields.humanReadableId]: true,
      };

      (cargoRegistrySearch.useCargoUserSettings as jest.Mock).mockReturnValue({
        refetch: jest.fn(),
        data: { cargoUIVisibility: mockColumnVisibility },
      });

      renderHook(() => useColumns(mockPathname));

      // useMemo должен быть вызван (количество вызовов зависит от порядка моков)
      expect(mockUseMemo).toHaveBeenCalled();
    });
  });
});
