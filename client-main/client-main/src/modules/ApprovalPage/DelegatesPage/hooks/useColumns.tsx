import React from 'react';
import { Button } from 'antd';
import { ColumnProps } from 'antd/lib/table';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import { DelegateModel } from 'stores/Delegates/Delegates.interface';
import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';
import { ReactComponent as Calendar } from 'shared/form/DatePicker/images/calendar.svg';
import { ReactComponent as EditIcon } from 'shared/icons/pen.svg';
import { ReactComponent as DeleteIcon } from 'shared/icons/delete.svg';

import { useModal } from '../context/modal.context';
import { useDelegateList } from './useDelegatesList';
import { Delegates, DelegatesCyrillic } from '../Delegates.constants';
import DelegateAvatar from '../components/DelegateAvatar/DelegateAvatar';
import styles from '../Delegates.module.scss';

type DelegateRecord = Pick<DelegateModel, 'delegateId' | 'id' | 'startDate' | 'endDate' | 'delegateEmployee'>;

export const useColumns: () => ColumnProps<DelegateRecord>[] = () => {
  const { fullDelegateNames } = useDelegateList();
  const { openEdit, openDelete } = useModal();

  return [
    {
      key: 'avatar',
      render: (_, { delegateId }) => <DelegateAvatar delegateId={delegateId} />,
      width: 48,
    },
    {
      title: 'ФИО делегата',
      dataIndex: 'delegateId',
      key: 'delegateId',
      render: (delegateId, delegate) => (
        <div className={styles.employeeCell}>
          <span className={styles.fullNameCell}>{fullDelegateNames[delegateId]}</span>
          <span className={styles.personnelNumber}>{`(${delegate.delegateEmployee.personnelNumber})`}</span>
        </div>
      ),
    },
    {
      title: 'Вид транспорта',
      dataIndex: 'transportType',
      key: 'transportType',
      render: transportType => <div>{TransportTypeTitles[transportType]}</div>,
    },
    {
      title: 'Период делегирования',
      dataIndex: 'startDate',
      key: 'startDate',
      render: (_, { startDate, endDate }) => {
        const delegatePeriod
          = startDate && endDate
            ? `${moment(startDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)} - ${moment(endDate).format(
              DATE_FORMAT.BASE_REVERTED_DOTS
            )}`
            : DelegatesCyrillic[Delegates.noDelegateDates];

        return (
          <div className={styles.periodCell}>
            <Calendar />
            <span>{delegatePeriod}</span>
          </div>
        );
      },
    },
    {
      key: 'editButton',
      render: (_, { id }) => (
        <Button
          type="text"
          className={styles.buttonCell}
          onClick={() => openEdit(id)}
        >
          <EditIcon />
        </Button>
      ),
      width: 32,
    },
    {
      key: 'deleteButton',
      render: (_, { id }) => (
        <Button
          type="text"
          className={styles.buttonCell}
          onClick={() => openDelete(id)}
        >
          <DeleteIcon width={32} />
        </Button>
      ),
      width: 32,
    },
  ];
};
