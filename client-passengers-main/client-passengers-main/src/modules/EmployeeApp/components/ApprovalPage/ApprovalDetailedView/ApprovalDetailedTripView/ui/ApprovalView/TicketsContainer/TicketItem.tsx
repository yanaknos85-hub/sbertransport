import React from 'react';
import { TransportCompensation } from 'stores/Trip/Trip.interface';

import { formatRublesWithoutRemainder } from 'utils';
import { UUID } from 'utils/io-ts';
import { TransportCompensation as TransportCompensationInfo } from '../../TransportCompensation/TransportCompensation';

import styles from './ticketsContainer.module.scss';

interface TicketItemProps {
  compensation: TransportCompensation;
}

export const TicketItem = ({ compensation }: TicketItemProps) => {
  return (
    <div className={styles.ticketsItemsWrapper}>
      <div className={styles.ticketsItemWrapper}>
        <div className={styles.ticketsItem}>
          <span className={styles.ticketsItemTitle}>
            {compensation.compensationType?.rusName}
          </span>
          <div className={styles.ticketsItemInfo}>
            <div className={styles.ticketsItemInfoTransportTypeWrapper}>
              {compensation.transportType && <div className={styles[compensation.transportType?.name]} />}
              <div className={styles.ticketsItemMainInfo}>
                {compensation?.transportType?.rusName}
              </div>
            </div>
            <div className={styles.ticketsItemCostWrapper}>
              <div className={styles.ticketsItemLine} />
              <div className={styles.ticketsItemCost}>
                <span>Стоимость</span>
                <span>{formatRublesWithoutRemainder(Number(compensation.ticketsCost) * Number(compensation.ticketsCount))}</span>
              </div>
            </div>
          </div>
        </div>
        {compensation.attachedDocumentId && (
        <TransportCompensationInfo
          document={{
            id: compensation.attachedDocumentId as UUID,
            fileFormat: compensation.compensationDocumentDTO?.fileFormat,
            fileName: compensation.compensationDocumentDTO?.fileName,
          }}
        />
        )}
      </div>
    </div>
  );
};
