import { useState } from 'react';
import { ColumnProps } from 'antd/lib/table';
import { CargoCategory, CargoType } from 'stores/CargoType/CargoType.interface';
import { useCargoTypeNames } from 'api/cargo-type-names';
import { useCargoCategoryNames } from 'api/cargo-category-names';

export type CargoTypeRecord = CargoType;

const getVolume = (value = 0) => {
  const MIN_VALUE = 0.001;
  const resValue = value / (1000 * 1000 * 1000);
  const val = resValue < MIN_VALUE ? MIN_VALUE : (resValue).toFixed(3);
  return `${val}`;
};

export const useColumns: () => ColumnProps<CargoTypeRecord>[] = () => {
  const { data: cargoTypeNames } = useCargoTypeNames();
  const { data: cargoCategoryNames } = useCargoCategoryNames();
  const { categoryNames } = cargoCategoryNames;

  return [
    {
      title: 'Краткое наименование SKU', //  toDo: сделать транслейты как было
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Тип груза',
      dataIndex: 'type',
      key: 'type',
      render: type => cargoTypeNames.byName[type].value,
    },
    {
      title: 'Категория',
      dataIndex: 'category',
      key: 'category',
      render: (type) => {
        return categoryNames && (
          categoryNames?.find(category => category.name === type)?.value
        );
      },
    },
    {
      title: 'Ширина единицы товара (мм)',
      dataIndex: 'width',
      key: 'width',
    },
    {
      title: 'Длина единицы товара (мм)',
      dataIndex: 'length',
      key: 'length',
    },
    {
      title: 'Высота единицы товара (мм)',
      dataIndex: 'height',
      key: 'height',
    },
    {
      title: 'Вес БРУТТО единицы товара (кг)',
      dataIndex: 'weight',
      key: 'weight',
    },
    {
      title: 'Объём (м3)',
      dataIndex: 'volume',
      key: 'volume',
      // todo необходимо согласовать единицы измерения габаритов грузов в корпе и клиенте
      render: (v) => {
        return  getVolume(v)
      },
    },
  ];
};
