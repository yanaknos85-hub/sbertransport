import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import closedBoxIcon from '../../icons/closedBox.png';
import openBoxIcon from '../../icons/openBox.png';
import truckIcon from '../../icons/truck.png';

import styles from './styles.module.scss';

interface ParticipantBlockProps {
  sender: string;
  receiver: string;
  contractor: string;
}

interface Props {
  data: ParticipantBlockProps;
}

export const ParticipantBlock: FC<Props> = props => {
  const {
    sender, receiver, contractor,
  } = props.data;
  const { t: { Etrn } } = useTranslation();

  return (
    <div className={styles.block}>
      <div className={styles.title}>{Etrn.card.participants}</div>
      <div className={styles.row}>
        <div className={styles.field}>
          <img
            src={closedBoxIcon}
            alt="closedBox"
            width={24}
            height={24}
          />
          <span className={styles.label}>{Etrn.card.senderLabel}</span>
          <span className={styles.value}>{sender}</span>
        </div>
        <div className={styles.field}>
          <img
            src={openBoxIcon}
            alt="openBox"
            width={24}
            height={24}
          />
          <span className={styles.label}>{Etrn.card.receiverLabel}</span>
          <span className={styles.value}>{receiver}</span>
        </div>
        <div className={styles.field}>
          <img
            src={truckIcon}
            alt="truck"
            width={24}
            height={24}
          />
          <span className={styles.label}>{Etrn.card.carrierLabel}</span>
          <span className={styles.value}>{contractor}</span>
        </div>
      </div>
    </div>
  );
};

export default ParticipantBlock;
