import React from 'react';

import { cargoTypeCategoryNameTitle } from 'stores/CargoType/CargoType.interface';

import { ValidationRules } from '../../fieldValidationRules';
import { FieldType } from '../Field/Field';

export const useFields = (
  categoryChange: (value: string) => void,
  isOversized: (value: string) => boolean,
  setIsHiddenDemensions: (value: boolean) => void,
  unit
) => {
  const options = Object.entries(cargoTypeCategoryNameTitle)
    .map(([key, value]) => ({ value: key, label: value }));

  return {
    name: {
      type: FieldType.input,
      label: 'Название',
      name: 'name',
      rules: [ValidationRules.general.required, ValidationRules.general.maxLength(100)],
      params: {
        placeholder: 'Введите название груза',
        onInput: (e: React.ChangeEvent <HTMLInputElement>) => { return e.target.value = e.target.value.charAt(0).toUpperCase() + e.target.value.slice(1); },
      },
    },
    category: {
      type: FieldType.select,
      label: `Категория`,
      name: 'category',
      rules: [ValidationRules.general.required],
      params: {
        controls: false,
        options: options,
        onSelect: (value: string) => {
          categoryChange(value);
          if (isOversized(value)) {
            setIsHiddenDemensions(false);
          } else {
            setIsHiddenDemensions(true);
          }
        },
      },
    },
    length: {
      type: FieldType.input,
      label: 'Длина, см',
      name: 'length',
      rules: [ValidationRules.general.required, ValidationRules.general.greaterThanZero()],
      params: {
        controls: false,
        decimalSeparator: ',',
      },
    },
    width: {
      type: FieldType.input,
      label: 'Ширина, см',
      name: 'width',
      rules: [ValidationRules.general.required, ValidationRules.general.greaterThanZero()],
      params: {
        controls: false,
        decimalSeparator: ',',
      },
    },
    height: {
      type: FieldType.input,
      label: 'Высота, см',
      name: 'height',
      rules: [ValidationRules.general.required, ValidationRules.general.greaterThanZero()],
      params: {
        controls: false,
        decimalSeparator: ',',
      },
    },
    weight: {
      type: FieldType.input,
      label: 'Вес, кг',
      name: 'weight',
      rules: [ValidationRules.general.required, ValidationRules.general.greaterThanZero()],
      params: {
        controls: false,
        decimalSeparator: ',',
      },
    },
    volume: {
      type: FieldType.input,
      label: `Объём ${unit}`,
      name: 'volume',
      rules: [ValidationRules.general.required, ValidationRules.general.greaterThanZero()],
      params: {
        controls: false,
        decimalSeparator: ',',
      },
    },
  };
};
