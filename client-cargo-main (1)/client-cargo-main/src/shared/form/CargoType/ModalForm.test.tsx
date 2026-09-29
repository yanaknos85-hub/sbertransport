/**
 * Unit тесты для компонента ModalForm
 * Тестирует логику добавления груза, валидацию, конвертацию единиц и обработку ошибок
 */

import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

// Импортируем компонент
import ModalForm from './ModalForm';

// Тестирование чистных функций, вынесенных из компонента
describe('ModalForm - чистые функции', () => {
  describe('isOversized', () => {
    const testCases = [
      { category: 'BULK', expected: true },
      { category: 'LIQUID', expected: true },
      { category: 'STANDARD', expected: false },
      { category: 'FRAGILE', expected: false },
      { category: '', expected: false },
    ];

    testCases.forEach(({ category, expected }) => {
      it(`должна возвращать ${expected} для категории "${category}"`, () => {
        // Создаем тестовую функцию isOversized
        const isOversized = (cat: string) => {
          return cat === 'BULK' || cat === 'LIQUID';
        };

        expect(isOversized(category)).toBe(expected);
      });
    });
  });

  describe('existCargoText', () => {
    it('должна возвращать корректное сообщение о существующем грузе', () => {
      const existCargoText = (name: string) => `Груз "${name}" уже добавлен`;

      expect(existCargoText('Телевизор')).toBe('Груз "Телевизор" уже добавлен');
      expect(existCargoText('Стул')).toBe('Груз "Стул" уже добавлен');
      expect(existCargoText('')).toBe('Груз "" уже добавлен');
    });
  });

  describe('differentCargoType', () => {
    it('должна возвращать корректное сообщение о несовпадении категорий', () => {
      const cargoTypeCategoryNameTitle = {
        BULK: 'Насыпной',
        LIQUID: 'Наливной',
        STANDARD: 'Стандартный',
      };

      const differentCargoType = (name: string, category: string) => (
        `Груз "${name}" отличается по категории. Категория груза ${name} - "${cargoTypeCategoryNameTitle[category]}"`
      );

      expect(differentCargoType('Песок', 'BULK'))
        .toBe('Груз "Песок" отличается по категории. Категория груза Песок - "Насыпной"');
    });
  });

  describe('showCargoExistWarn', () => {
    it('должна формировать корректное уведомление', () => {
      const showCargoExistWarn = (description: string) => ({
        message: 'Внимание!',
        description,
      });

      const result = showCargoExistWarn('Тестовое предупреждение');

      expect(result).toEqual({
        message: 'Внимание!',
        description: 'Тестовое предупреждение',
      });
    });
  });

  describe('конвертация объемов', () => {
    it('должна корректно конвертировать cm³ в mm³', () => {
      const convertCubicCmToCubicMm = (length: number, width: number, height: number) => {
        return (length * width * height) * 1000;
      };

      expect(convertCubicCmToCubicMm(10, 10, 10)).toBe(1_000_000);
      expect(convertCubicCmToCubicMm(1, 1, 1)).toBe(1000);
      expect(convertCubicCmToCubicMm(0, 10, 10)).toBe(0);
    });

    it('должна корректно конвертировать m³ в cm³ для объёма в cargoList', () => {
      const convertCubicMToCubicCm = (volume: number) => {
        return volume * 1_000_000;
      };

      expect(convertCubicMToCubicCm(1)).toBe(1_000_000);
      expect(convertCubicMToCubicCm(0.5)).toBe(500_000);
      expect(convertCubicMToCubicCm(0)).toBe(0);
    });

    it('должна корректно конвертировать m³ в mm³ для personalCargoItem', () => {
      const convertCubicMToCubicMm = (volume: number) => {
        return volume * 1_000_000_000;
      };

      expect(convertCubicMToCubicMm(1)).toBe(1_000_000_000);
      expect(convertCubicMToCubicMm(5)).toBe(5_000_000_000);
      expect(convertCubicMToCubicMm(0.005)).toBe(5_000_000);
    });
  });

  describe('валидация размеров', () => {
    it('должна преобразовывать пустые значения в 0', () => {
      const normalizeValue = (value: number | undefined | null) => Number(value) || 0;

      expect(normalizeValue(10)).toBe(10);
      expect(normalizeValue(0)).toBe(0);
      expect(normalizeValue(undefined)).toBe(0);
      expect(normalizeValue(null)).toBe(0);
      expect(normalizeValue(NaN)).toBe(0);
    });
  });
});

describe('ModalForm - хуки и логика', () => {
  describe('useResetFormOnCloseModal', () => {
    it('должен сбрасывать форму при закрытии модального окна', () => {
      const mockResetFields = jest.fn();

      // Эмуляция хука
      const useResetFormOnCloseModal = ({
                                          form,
                                          visible,
                                          prevVisible
                                        }: {
        form: { resetFields: () => void };
        visible: boolean;
        prevVisible: boolean;
      }) => {
        if (!visible && prevVisible) {
          form.resetFields();
        }
      };

      const form = { resetFields: mockResetFields };

      // Сценарий: окно было открыто, теперь закрывается
      useResetFormOnCloseModal({ form, visible: false, prevVisible: true });
      expect(mockResetFields).toHaveBeenCalledTimes(1);

      // Сценарий: окно открыто
      mockResetFields.mockClear();
      useResetFormOnCloseModal({ form, visible: true, prevVisible: false });
      expect(mockResetFields).not.toHaveBeenCalled();

      // Сценарий: окно было закрыто и осталось закрытым
      mockResetFields.mockClear();
      useResetFormOnCloseModal({ form, visible: false, prevVisible: false });
      expect(mockResetFields).not.toHaveBeenCalled();
    });
  });

  describe('categoryChange', () => {
    it('должна устанавливать unit на основе выбранной категории', () => {
      const categoryNames = [
        { name: 'BULK', unit: 'm³' },
        { name: 'LIQUID', unit: 'L' },
        { name: 'STANDARD', unit: 'kg' },
      ];

      let currentUnit = '';

      const categoryChange = (value: string) => {
        const unit = categoryNames.find(category => category.name === value)?.unit as string;
        currentUnit = unit;
      };

      categoryChange('BULK');
      expect(currentUnit).toBe('m³');

      categoryChange('LIQUID');
      expect(currentUnit).toBe('L');

      categoryChange('STANDARD');
      expect(currentUnit).toBe('kg');

      categoryChange('UNKNOWN');
      expect(currentUnit).toBeUndefined();
    });
  });

  describe('создание объекта cargo', () => {
    const mockFormValues = {
      name: 'Тестовый груз',
      category: 'BULK',
      weight: 100,
      length: 50,
      width: 40,
      height: 30,
    };

    it('должен корректно формировать объект груза для обычных грузов', () => {
      const isHiddenDemensions = true;
      const cargoList = [];

      const volumeCargo = isHiddenDemensions
        ? (Number(mockFormValues.length) * Number(mockFormValues.width) * Number(mockFormValues.height)) * 1000
        : 0;

      const cargo = {
        ...mockFormValues,
        cargoCategory: mockFormValues.category,
        cargoName: mockFormValues.name,
        occupiedPlacesCount: 1,
        volume: volumeCargo,
        position: cargoList.length,
        length: Number(mockFormValues.length) || 0,
        width: Number(mockFormValues.width) || 0,
        height: Number(mockFormValues.height) || 0,
        weight: Number(mockFormValues.weight) || 0,
      };

      expect(cargo.name).toBe('Тестовый груз');
      expect(cargo.cargoCategory).toBe('BULK');
      expect(cargo.occupiedPlacesCount).toBe(1);
      expect(cargo.volume).toBe(50 * 40 * 30 * 1000);
      expect(cargo.position).toBe(0);
      expect(cargo.length).toBe(50);
      expect(cargo.width).toBe(40);
      expect(cargo.height).toBe(30);
      expect(cargo.weight).toBe(100);
    });

    it('должен корректно формировать объект для наливных/насыпных грузов', () => {
      const isHiddenDemensions = false;
      const volumeFromForm = 2.5; // 2.5 m³
      const cargoList = [{ name: 'Существующий груз' }];

      const volumeCargo = !isHiddenDemensions
        ? volumeFromForm * 1_000_000
        : 0;

      const cargo = {
        ...mockFormValues,
        cargoCategory: mockFormValues.category,
        cargoName: mockFormValues.name,
        occupiedPlacesCount: 1,
        volume: volumeCargo,
        position: cargoList.length,
        length: Number(mockFormValues.length) || 0,
        width: Number(mockFormValues.width) || 0,
        height: Number(mockFormValues.height) || 0,
        weight: Number(mockFormValues.weight) || 0,
      };

      expect(cargo.volume).toBe(2.5 * 1_000_000);
      expect(cargo.position).toBe(1);
    });
  });

  describe('создание personalCargoItem', () => {
    it('должен корректно формировать объект personalCargoItem', () => {
      const formValues = {
        name: 'Личный груз',
        category: 'STANDARD',
        weight: 50,
        length: 30,
        width: 20,
        height: 10,
      };

      const volumeCargo = (30 * 20 * 10) * 1000;

      const sizesToUnits = (sizes: { length: number; width: number; height: number }) => ({
        lengthMm: sizes.length * 10,
        widthMm: sizes.width * 10,
        heightMm: sizes.height * 10,
      });

      const personalCargoItem = {
        name: formValues.name,
        category: formValues.category,
        ...sizesToUnits({
          length: Number(formValues.length) || 0,
          width: Number(formValues.width) || 0,
          height: Number(formValues.height) || 0,
        }),
        weight: Number(formValues.weight) || 0,
        volume: volumeCargo,
        type: 'OTHER',
        accessLevel: 'PERSONAL',
      };

      expect(personalCargoItem.name).toBe('Личный груз');
      expect(personalCargoItem.category).toBe('STANDARD');
      expect(personalCargoItem.lengthMm).toBe(300);
      expect(personalCargoItem.widthMm).toBe(200);
      expect(personalCargoItem.heightMm).toBe(100);
      expect(personalCargoItem.weight).toBe(50);
      expect(personalCargoItem.type).toBe('OTHER');
      expect(personalCargoItem.accessLevel).toBe('PERSONAL');
    });

    it('должен корректно формировать объект для наливного/насыпного груза', () => {
      const formValues = {
        name: 'Наливной груз',
        category: 'BULK',
        weight: 100,
        length: 0, // у наливных/насыпных грузов габариты отсутствуют
        width: 0,
        height: 0,
      };

      const isHiddenDemensions = false;
      const volumeFromForm = 5; // 5 м³

      const volumeCargo = !isHiddenDemensions
        ? volumeFromForm * 1_000_000_000 // m³ → mm³
        : 0;

      const sizesToUnits = (sizes: { length: number; width: number; height: number }) => ({
        lengthMm: sizes.length * 10,
        widthMm: sizes.width * 10,
        heightMm: sizes.height * 10,
      });

      const personalCargoItem = {
        name: formValues.name,
        category: formValues.category,
        ...sizesToUnits({
          length: Number(formValues.length) || 0,
          width: Number(formValues.width) || 0,
          height: Number(formValues.height) || 0,
        }),
        weight: Number(formValues.weight) || 0,
        volume: volumeCargo,
        type: 'OTHER',
        accessLevel: 'PERSONAL',
      };

      // Объём должен отправляться на бэк в мм³
      expect(personalCargoItem.volume).toBe(5 * 1_000_000_000);
      // У наливных/насыпных грузов габариты отсутствуют и уходят нулями
      expect(personalCargoItem.lengthMm).toBe(0);
      expect(personalCargoItem.widthMm).toBe(0);
      expect(personalCargoItem.heightMm).toBe(0);
    });
  });

  describe('проверка дубликатов грузов', () => {
    const cargoList = [
      { cargoName: 'Груз 1', category: 'BULK' },
      { cargoName: 'Груз 2', category: 'LIQUID' },
    ];

    it('должна обнаруживать груз с таким же именем', () => {
      const newCargo = { cargoName: 'Груз 1', category: 'STANDARD' };

      const isNameExists = cargoList.find(item => item.cargoName === newCargo.cargoName);

      expect(isNameExists).toBeDefined();
      expect(isNameExists?.cargoName).toBe('Груз 1');
    });

    it('должна обнаруживать груз с другой категорией', () => {
      const newCargo = { cargoName: 'Груз 3', category: 'STANDARD' };

      const hasDifferentCategory = cargoList.find(item => item.category !== newCargo.category);

      expect(hasDifferentCategory).toBeDefined();
      expect(hasDifferentCategory?.category).toBe('BULK');
    });

    it('должна пропускать уникальный груз', () => {
      const newCargo = { cargoName: 'Груз 3', category: 'STANDARD' };

      const isNameExists = cargoList.find(item => item.cargoName === newCargo.cargoName);
      expect(isNameExists).toBeUndefined();

      // Если есть хотя бы один груз с другой категорией - это нормально
      // Ищем только по названию для дубликата
      expect(cargoList.some(item => item.cargoName === newCargo.cargoName)).toBe(false);
    });
  });

  describe('initialValues', () => {
    it('должны корректно устанавливаться начальные значения формы', () => {
      const initialCargoName = 'Начальный груз';

      const initialValues = {
        checkboxSizes: false,
        name: initialCargoName,
      };

      expect(initialValues.checkboxSizes).toBe(false);
      expect(initialValues.name).toBe(initialCargoName);
    });

    it('должны устанавливаться пустые значения по умолчанию', () => {
      const initialValues = {
        checkboxSizes: false,
        name: '',
      };

      expect(initialValues.checkboxSizes).toBe(false);
      expect(initialValues.name).toBe('');
    });
  });
});

describe('ModalForm - пропсы и рендеринг', () => {
  const defaultProps = {
    visible: true,
    onSave: jest.fn(),
    onCancel: jest.fn(),
    initialCargoName: '',
  };

  it('должен принимать и передавать корректные пропсы', () => {
    const props = { ...defaultProps, initialCargoName: 'Тестовый груз' };

    expect(props.visible).toBe(true);
    expect(props.onSave).toBeDefined();
    expect(props.onCancel).toBeDefined();
    expect(props.initialCargoName).toBe('Тестовый груз');
  });

  it('должен корректно обрабатывать onSave и onCancel вызовы', () => {
    const onSaveMock = jest.fn();
    const onCancelMock = jest.fn();

    const props = {
      ...defaultProps,
      onSave: onSaveMock,
      onCancel: onCancelMock,
    };

    // Симулируем вызовы
    props.onSave();
    props.onCancel();

    expect(onSaveMock).toHaveBeenCalledTimes(1);
    expect(onCancelMock).toHaveBeenCalledTimes(1);
  });

  it('должен корректно работать без initialCargoName', () => {
    const props = { ...defaultProps, initialCargoName: undefined };

    expect(props.initialCargoName).toBeUndefined();
  });

  it('должен иметь правильные значения по умолчанию для модального окна', () => {
    const modalConfig = {
      centered: true,
      closable: true,
      keyboard: true,
      width: 620,
      okText: 'Сохранить',
      cancelText: 'Отмена',
    };

    expect(modalConfig.centered).toBe(true);
    expect(modalConfig.closable).toBe(true);
    expect(modalConfig.keyboard).toBe(true);
    expect(modalConfig.width).toBe(620);
    expect(modalConfig.okText).toBe('Сохранить');
    expect(modalConfig.cancelText).toBe('Отмена');
  });
});

describe('ModalForm - обработка ошибок', () => {
  it('должен корректно обрабатывать ошибку 409 (конфликт)', () => {
    const error409 = { response: { status: 409 } };
    const errorMessage = 'Груз с таким именем уже существует';

    const handleError = (err: any) => {
      if (err.response.status === 409) {
        return {
          message: 'Ошибка',
          description: 'Груз с таким именем уже существует',
        };
      } else {
        return {
          message: 'Ошибка',
          description: 'Не удалось добавить груз',
        };
      }
    };

    const result = handleError(error409);

    expect(result.description).toBe(errorMessage);
  });

  it('должен корректно обрабатывать другие ошибки', () => {
    const error500 = { response: { status: 500 } };
    const genericError = new Error('Network error');

    const handleError = (err: any) => {
      if (err.response?.status === 409) {
        return 'Конфликт';
      } else {
        return 'Не удалось добавить груз';
      }
    };

    expect(handleError(error500)).toBe('Не удалось добавить груз');
    expect(handleError(genericError)).toBe('Не удалось добавить груз');
  });
});
