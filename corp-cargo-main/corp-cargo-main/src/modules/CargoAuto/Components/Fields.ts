import { useMemo } from 'react';
import { ModelFormFieldType, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import { preventDefault } from 'utils';
import { useAutoCapacity, useAutoCargoType } from 'api/cargo-auto';
import styles from '../CargoAuto.module.scss';

export const useFields: () => ModelFormFieldProps[] = () => {
  const { data: capacity } = useAutoCapacity();
  const { data: category } = useAutoCargoType();

  const fieldsValidation = () => ({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    validator(_: any, value: string) {
      if (Number.isNaN(value)) {
        return Promise.reject(new Error('Пожалуйста, укажите числовое значение'));
      }
      if (value !== null && Number(value) === 0) {
        return Promise.reject(new Error('Значение не может быть равно нулю'));
      }
      return Promise.resolve();
    },
  });

  return useMemo<ModelFormFieldProps[]>(
    () => [
      {
        name: 'name',
        fieldType: ModelFormFieldType.TEXT,
        onInputKeyDown: preventDefault,
        description: 'Наименование автомобиля',
        required: true,
        notTrim: true,
        editable: true,
        allowClear: true,
        className: styles.input,
      },
      {
        name: 'capacity',
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        description: 'Грузоподъемность, (кг)',
        options: capacity.map(capacity => ({
          label: capacity.capacity,
          value: JSON.stringify(capacity),
        })),
        required: true,
        editable: true,
      },
      {
        name: 'cargoCategory',
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        description: 'Категория груза',
        options: category.map(category => ({
          label: category.description,
          value: category.value,
        })),
        required: true,
        editable: true,
      },
      {
        name: 'volume',
        fieldType: ModelFormFieldType.NUMBER,
        onInputKeyDown: preventDefault,
        description: 'Объем, ' + '(м\u00B3)',
        required: true,
        editable: true,
        allowClear: true,
        min: 0,
        max: 120,
        rules: [fieldsValidation],
      },
      {
        name: 'width',
        fieldType: ModelFormFieldType.NUMBER,
        onInputKeyDown: preventDefault,
        description: 'Ширина кузова (м)',
        required: true,
        editable: true,
        allowClear: true,
        min: 0,
        max: 3,
        rules: [fieldsValidation],
      },
      {
        name: 'length',
        fieldType: ModelFormFieldType.NUMBER,
        onInputKeyDown: preventDefault,
        description: 'Длина кузова (м)',
        required: true,
        editable: true,
        allowClear: true,
        min: 0,
        max: 14,
        rules: [fieldsValidation],
      },
      {
        name: 'height',
        fieldType: ModelFormFieldType.NUMBER,
        onInputKeyDown: preventDefault,
        description: 'Высота кузова (м)',
        required: true,
        editable: true,
        allowClear: true,
        min: 0,
        max: 3,
        rules: [fieldsValidation],
      },
    ],
    [capacity, category]
  );
};
