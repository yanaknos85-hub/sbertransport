import React from 'react';
import { Button } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as DeleteIcon } from 'shared/icons/delete.svg';
import { cargoTypeCategoryNameTitle, cargoTypeNameTitle } from 'types/Cargo';
import { CargoPersonalListItemType } from 'stores/CargoList/CargoList.interface';

const toCubicMeters = (volumeInCubicMillimeters: number): number => {
  return volumeInCubicMillimeters / 1e9;
};

export const useColumns: () => ColumnProps<CargoPersonalListItemType>[] = () => {
  const { [StoreNames.cargoListStore]: cargoListStore } = useAppStoreContext();
  const { setModalVisible, setCargoId } = cargoListStore;

  return [
    {
      title: 'Наименование',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Тип груза',
      dataIndex: 'type',
      key: 'type',
      render: (_, { type }) => (
        `${cargoTypeNameTitle[type]}`
      ),
    },
    {
      title: 'Категория',
      dataIndex: 'category',
      key: 'category',
      render: (_, { category }) => (
        `${cargoTypeCategoryNameTitle[category]}`
      ),
    },
    {
      title: 'Длина (мм)',
      dataIndex: 'length',
      key: 'length',
    },
    {
      title: 'Ширина (мм)',
      dataIndex: 'width',
      key: 'width',
    },
    {
      title: 'Высота (мм)',
      dataIndex: 'height',
      key: 'height',
    },
    {
      title: 'Вес (кг)',
      dataIndex: 'weight',
      key: 'weight',
    },
    {
      title: 'Объем (мм³)',
      dataIndex: 'volume',
      key: 'volume',
      render: (_, { volume }) => (
        toCubicMeters(volume).toFixed(3)
      ),
    },
    {
      key: 'deleteButton',
      render: (_, { id }) => (
        <Button
          type="text"
          onClick={() => {
            setCargoId && setCargoId(id);
            setModalVisible(true);
          }}
        >
          <DeleteIcon width={32} />
        </Button>
      ),
      width: 32,
    },
  ];
};
