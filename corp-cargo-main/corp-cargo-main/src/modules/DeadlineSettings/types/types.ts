import { ColumnProps } from 'antd/lib/table';
import { DeadlineSettings } from '../hooks/useColumns';

export interface DeadlineColumns {
  taxi: ColumnProps<DeadlineSettings>[];
  personnel: ColumnProps<DeadlineSettings>[];
  carsharing: ColumnProps<DeadlineSettings>[];
  public: ColumnProps<DeadlineSettings>[];
  personnelLimit: ColumnProps<DeadlineSettings>[];
  depLimit: ColumnProps<DeadlineSettings>[];
  cargoDedicated: ColumnProps<DeadlineSettings>[];
  cargoCourier: ColumnProps<DeadlineSettings>[];
  cargoInterregional: ColumnProps<DeadlineSettings>[];
}

export interface TableData {
  taxi: DeadlineSettings[];
  personnel: DeadlineSettings[];
  carsharing: DeadlineSettings[];
  public: DeadlineSettings[];
  personnelLimit: DeadlineSettings[];
  depLimit: DeadlineSettings[];
  cargoDedicated: DeadlineSettings[];
  cargoCourier: DeadlineSettings[];
  cargoInterregional: DeadlineSettings[];
}
