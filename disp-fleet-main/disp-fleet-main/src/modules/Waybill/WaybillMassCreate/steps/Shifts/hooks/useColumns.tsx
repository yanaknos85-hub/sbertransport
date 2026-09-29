import React, { useMemo, useCallback } from 'react';
import moment from 'moment';
import { Button, Tooltip } from 'antd';
import type { ColumnType } from 'antd/lib/table';
import { EditOutlined } from '@ant-design/icons';

import { DATE_FORMAT } from 'constants/app.constants';
import { Shift as ShiftType, TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';
import { useShiftEditApi } from 'context/ShiftEdit.context';
import styles from '../Shifts.module.scss';

interface UseColumnsParams {
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  errorShiftIds: Set<string>;
}

export const useColumns = ({ firstTitleResult, errorShiftIds }: UseColumnsParams) => {
  const shiftEditApi = useShiftEditApi();
  const showErrorColumn = errorShiftIds.size > 0;

  const handleEdit = useCallback(
    (shiftId: ShiftType['id']) => shiftEditApi.openShift?.(shiftId),
    [shiftEditApi]
  );

  // Получает текст ошибки для указанной смены по её ID
  const getErrorText = useCallback(
    (shiftId: string): string => {
      if (!firstTitleResult) return '';
      const item = firstTitleResult.find(result => result.shiftId === shiftId);
      return item?.errorText ?? '';
    },
    [firstTitleResult]
  );

  const columns: ColumnType<ShiftType>[] = useMemo(() => {
    const cols: ColumnType<ShiftType>[] = [];

    // Столбец с индикатором ошибки (показываем только если есть ошибки)
    if (showErrorColumn) {
      cols.push({
        title: '',
        key: 'errorIndicator',
        width: 24,
        className: styles.errorIndicatorColumn,
        render: (record: ShiftType) => {
          if (!errorShiftIds.has(record.id)) return null;
          const errorText = getErrorText(record.id);
          return (
            <Tooltip title={errorText || 'Ошибка'}>
              <span className={styles.errorIcon}>!</span>
            </Tooltip>
          );
        },
      });
    }

    cols.push(
      {
        title: 'Водитель',
        dataIndex: 'driverName',
        key: 'driverName',
        width: 200,
        sorter: (a: ShiftType, b: ShiftType) => {
          if (!a.driverName && !b.driverName) return 0;
          if (!a.driverName) return -1;
          if (!b.driverName) return 1;
          return a.driverName.localeCompare(b.driverName, 'ru-RU');
        },
      },
      {
        title: 'Марка',
        dataIndex: 'vehicleBrand',
        key: 'vehicleBrand',
        width: 120,
      },
      {
        title: 'Модель',
        dataIndex: 'vehicleModel',
        key: 'vehicleModel',
        width: 120,
      },
      {
        title: 'Госномер',
        dataIndex: 'vehicleStateNumber',
        key: 'vehicleStateNumber',
        width: 120,
      },
      {
        title: 'Начало смены',
        dataIndex: 'startDate',
        key: 'startDate',
        width: 150,
        render: (value: string) => moment.utc(value).local().format(DATE_FORMAT.BASE_REVERTED_DOTS),
      },
      {
        title: 'Окончание смены',
        dataIndex: 'endDate',
        key: 'endDate',
        width: 150,
        render: (value: string) => moment.utc(value).local().format(DATE_FORMAT.BASE_REVERTED_DOTS),
      }
    );

    // Столбец с иконкой карандаша для строк с ошибкой
    if (showErrorColumn) {
      cols.push({
        title: '',
        key: 'edit',
        width: 40,
        fixed: 'right',
        render: (record: ShiftType) => {
          if (!errorShiftIds.has(record.id)) return null;
          return (
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record.id)}
            />
          );
        },
      });
    }

    return cols;
  }, [errorShiftIds, getErrorText, showErrorColumn, handleEdit]);

  return {
    columns, showErrorColumn,
  };
};
