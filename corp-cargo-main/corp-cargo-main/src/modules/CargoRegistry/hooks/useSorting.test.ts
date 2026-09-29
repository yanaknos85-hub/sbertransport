// Мокаем все зависимости перед импортом хука
jest.mock('react', () => ({
  ...jest.requireActual('react'),
  useState: jest.fn(),
}));

jest.mock('stores/SettingsContext', () => ({
  Registry: {
    Cargo: 'Cargo',
  },
  useSettingsContext: jest.fn(),
}));

jest.mock('stores/CargoRegistry/CargoRegistry.interface', () => ({
  SortSetting: {} as const,
}));

jest.mock('api/register-search', () => ({
  SortFields: {
    CREATION_DATE: 'creationDate',
  },
}));

import { renderHook, act } from '@testing-library/react-hooks';
import { useSorting } from './useSorting';
import { Registry } from 'stores/SettingsContext';
import { useSettingsContext } from 'stores/SettingsContext';
import { useState } from 'react';

describe('useSorting', () => {
  const mockSetState = jest.fn();
  const mockSaveSortSettings = jest.fn();
  const mockSortSettings = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('должен возвращать объект с sortSetting, setSortSetting и handleDefaultSort', () => {
    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockReturnValue([undefined, mockSetState]);

    const { result } = renderHook(() => useSorting());

    expect(result.current).toHaveProperty('sortSetting');
    expect(result.current).toHaveProperty('setSortSetting');
    expect(result.current).toHaveProperty('handleDefaultSort');
  });

  test('должен вызывать useState с начальным значением из cargoSettings.sortSettings()', () => {
    const mockInitialSortSetting = { property: 'test', directionAsc: true };
    mockSortSettings.mockReturnValue(mockInitialSortSetting);

    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockImplementation((init) => [init, mockSetState]);

    renderHook(() => useSorting());

    expect(useState).toHaveBeenCalledWith(mockInitialSortSetting);
  });

  test('должен возвращать undefined для sortSetting если начальное состояние undefined', () => {
    mockSortSettings.mockReturnValue(undefined);

    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockReturnValue([undefined, mockSetState]);

    const { result } = renderHook(() => useSorting());

    expect(result.current.sortSetting).toBeUndefined();
  });

  test('должен возвращать sortSetting если начальное состояние задано', () => {
    const mockSortSetting = { property: 'test', directionAsc: true };
    mockSortSettings.mockReturnValue(mockSortSetting);

    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockReturnValue([mockSortSetting, mockSetState]);

    const { result } = renderHook(() => useSorting());

    expect(result.current.sortSetting).toBe(mockSortSetting);
  });

  test('должен вызывать cargoSettings.saveSortSettings и setSortSetting при вызове handleDefaultSort', () => {
    mockSortSettings.mockReturnValue({ property: 'test', directionAsc: true });

    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockImplementation((init) => [init, mockSetState]);

    const { result } = renderHook(() => useSorting());

    act(() => {
      result.current.handleDefaultSort();
    });

    const defaultSorting = {
      property: 'creationDate',
      directionAsc: false,
    };

    expect(mockSaveSortSettings).toHaveBeenCalledWith(defaultSorting);
    expect(mockSetState).toHaveBeenCalledWith(defaultSorting);
  });

  test('должен использовать правильные значения по умолчанию в handleDefaultSort', () => {
    mockSortSettings.mockReturnValue(undefined);

    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockImplementation((init) => [init, mockSetState]);

    const { result } = renderHook(() => useSorting());

    act(() => {
      result.current.handleDefaultSort();
    });

    expect(mockSaveSortSettings).toHaveBeenCalledWith({
      property: 'creationDate',
      directionAsc: false,
    });
    expect(mockSetState).toHaveBeenCalledWith({
      property: 'creationDate',
      directionAsc: false,
    });
  });

  test('должен возвращать функцию handleDefaultSort которая может быть вызвана многократно', () => {
    mockSortSettings.mockReturnValue({ property: 'test', directionAsc: true });

    (useSettingsContext as jest.Mock).mockReturnValue({
      [Registry.Cargo]: {
        sortSettings: mockSortSettings,
        saveSortSettings: mockSaveSortSettings,
      },
    });
    (useState as jest.Mock).mockImplementation((init) => [init, mockSetState]);

    const { result } = renderHook(() => useSorting());

    act(() => {
      result.current.handleDefaultSort();
    });

    act(() => {
      result.current.handleDefaultSort();
    });

    expect(mockSaveSortSettings).toHaveBeenCalledTimes(2);
    expect(mockSetState).toHaveBeenCalledTimes(2);
  });
});
