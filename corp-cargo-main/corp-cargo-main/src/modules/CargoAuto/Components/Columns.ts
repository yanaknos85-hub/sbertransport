import { ColumnProps } from 'antd/lib/table';
import { CargoAuto, CategoryCargo } from 'stores/CargoAuto/CargoAuto.interface';

export type CargoAutoRecord = CargoAuto;

export const useColumns: () => ColumnProps<CargoAutoRecord>[] = () => [
  {
    title: 'Наименование автомобиля',
    dataIndex: 'name',
  },
  {
    title: 'Грузоподъемность, (кг)',
    dataIndex: 'capacity',
    render: capacity => capacity.capacity,
  },
  {
    title: 'Категория груза',
    dataIndex: 'cargoCategory',
    render: cargoCategory => CategoryCargo[cargoCategory],
  },
  {
    title: 'Объем, ' + '(м\u00B3)',
    dataIndex: 'volume',
  },
  {
    title: 'Длина, (м)',
    dataIndex: 'length',
  },
  {
    title: 'Ширина, (м)',
    dataIndex: 'width',
  },
  {
    title: 'Высота, (м)',
    dataIndex: 'height',
  },
];
