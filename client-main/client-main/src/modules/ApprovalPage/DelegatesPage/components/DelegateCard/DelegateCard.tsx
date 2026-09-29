import React, { FC } from 'react';
import { Button } from '@sber-sbertransport/ui-kit/src';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';
import { ReactComponent as Calendar } from 'shared/form/DatePicker/images/calendar.svg';
import { ReactComponent as EditIcon } from 'shared/icons/pen.svg';
import { ReactComponent as DeleteIcon } from 'shared/icons/delete.svg';
import { DelegateModel } from 'stores/Delegates/Delegates.interface';
import { TransportTypeTitles } from 'stores/TransportTypes/TransportTypes.interface';

import { Delegates, DelegatesCyrillic } from '../../Delegates.constants';
import { useDelegateList } from '../../hooks/useDelegatesList';
import DelegateAvatar from '../DelegateAvatar/DelegateAvatar';
import { useModal } from '../../context/modal.context';
import styles from './DelegateCard.module.scss';

interface DelegateCardProps {
  delegate: DelegateModel;
}

const DelegateCard: FC<DelegateCardProps> = ({ delegate }) => {
  const { fullDelegateNames } = useDelegateList();
  const { openEdit, openDelete } = useModal();

  const delegatePeriod
    = delegate.startDate && delegate.endDate
      ? `${moment(delegate.startDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)} - ${moment(delegate.endDate).format(
        DATE_FORMAT.BASE_REVERTED_DOTS
      )}`
      : DelegatesCyrillic[Delegates.noDelegateDates];

  return (
    <div className={styles.delegateCard}>
      <div className={styles.cardHeader}>
        <DelegateAvatar delegateId={delegate.id} />
        <div className={styles.employee}>
          <span className={styles.fullName}>{fullDelegateNames[delegate.delegateId]}</span>
          <span className={styles.personnelNumber}>{`(${delegate.delegateEmployee.personnelNumber})`}</span>
        </div>
        <div className={styles.actions}>
          <Button
            type="text"
            className={styles.button}
            onClick={() => openEdit(delegate.id)}
          >
            <EditIcon />
          </Button>
          <Button
            type="text"
            className={styles.button}
            onClick={() => openDelete(delegate.id)}
          >
            <DeleteIcon width={32} />
          </Button>
        </div>
      </div>
      <div className={styles.transportType}>{TransportTypeTitles[delegate.transportType]}</div>
      <div className={styles.period}>
        <Calendar />
        <span>{delegatePeriod}</span>
      </div>
    </div>
  );
};

export default DelegateCard;
