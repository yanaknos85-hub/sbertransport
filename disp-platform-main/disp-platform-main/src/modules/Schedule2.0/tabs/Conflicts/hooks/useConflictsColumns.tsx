import React, { useMemo } from 'react';

import { Tooltip } from 'antd';
import { ColumnType } from 'antd/lib/table';
import moment from 'moment';

import { ShiftConflictReason, SHIFT_CONFLICT_REASONS_TEXTS } from 'api/shift-conflicts/shift-conflicts.constants';
import { ShiftConflict } from 'api/shift-conflicts/shift-conflicts.types';

import { useTranslation } from 'i18n';

import { useSelectedShift } from 'modules/Schedule2.0/context/selectedShift.context';

import Flex from 'components/Flex/Flex';

import { useModals } from '../../../context/modal.context';

import { ReactComponent as DeleteIcon } from 'assets/icons/delete.svg';
import { ReactComponent as EditIcon } from 'assets/icons/edit.svg';
import { ReactComponent as ErrorIcon } from 'assets/icons/error.svg';

import styles from '../Conflicts.module.scss';

export const useConflictsColumns = (): ColumnType<ShiftConflict>[] => {
  const { t } = useTranslation();
  const { handleOpenDeleteConflict, handleOpenCreate } = useModals();
  const { setConflictShift } = useSelectedShift();

  const columns = useMemo(
    (): ColumnType<ShiftConflict>[] => [
      {
        key: 'errorIndicator',
        width: 18,
        render: (_, record) => (
          <Flex alignItems="center">
            <Tooltip title={SHIFT_CONFLICT_REASONS_TEXTS[record.conflictReason] ?? record.conflictReason}>
              <ErrorIcon className={styles.errorIcon} />
            </Tooltip>
          </Flex>
        ),
        fixed: 'left' as const,
      },
      {
        title: t.Shifts.personnelNumber,
        dataIndex: 'personnelNumber',
        key: 'personnelNumber',
        width: 140,
        render: (value, record) => {
          const shouldHighlight = record.conflictReason === ShiftConflictReason.DRIVER_NOT_FOUND;
          return (
            <div className={shouldHighlight ? styles.error : undefined}>
              {value}
            </div>
          );
        },
      },
      {
        title: t.Shifts.stateNumber,
        dataIndex: 'stateNumber',
        key: 'stateNumber',
        width: 120,
        render: (value, record) => {
          const shouldHighlight = record.conflictReason === ShiftConflictReason.VEHICLE_NOT_FOUND;
          return (
            <div className={shouldHighlight ? styles.error : undefined}>
              {value}
            </div>
          );
        },
      },
      {
        title: t.Shifts.routeNumber,
        dataIndex: 'routeId',
        key: 'routeId',
        width: 140,
      },
      {
        title: t.Shifts.conflictStartDate,
        dataIndex: 'startDate',
        key: 'startDate',
        width: 150,
        render: value => value ? moment.utc(value).local().format('DD.MM.YYYY') : undefined,
      },
      {
        title: t.Shifts.conflictEndDate,
        dataIndex: 'endDate',
        key: 'endDate',
        render: value => value ? moment.utc(value).local().format('DD.MM.YYYY') : undefined,
      },
      {
        key: 'actions',
        width: 50,
        render: (_, record) => (
          <Flex alignItems="center" justifyContent="center">
            <EditIcon
              className={styles.editIcon}
              onClick={() => {
                setConflictShift(record);
                handleOpenCreate();
              }}
            />
            <DeleteIcon
              className={styles.deleteIcon}
              onClick={() => handleOpenDeleteConflict(record)}
            />
          </Flex>
        ),
        fixed: 'right' as const,
      },
    ],
    [t, handleOpenDeleteConflict, handleOpenCreate, setConflictShift]
  );

  return columns;
};
