import React from 'react';
import { TripRequestModel } from 'stores/Trip/models';
import { TicketItem } from './TicketItem';
import styles from './ticketsContainer.module.scss';

interface TicketItemProps {
  request: TripRequestModel;
}

export const TicketsContainer = ({ request }: TicketItemProps) => {
  const totalTicketsAmount = request?.transportCompensation?.reduce((acc, ticketItem) => acc + (ticketItem?.ticketsCount || 1), 0);

  return (
    <div className={styles.ticketsWrapper}>
      <p className={styles.ticketsMainTitle}>
        Билеты&nbsp;
        <span>{totalTicketsAmount}</span>
      </p>
      <div className={styles.ticketsContainer}>
        {request?.transportCompensation?.map(compensation => (
          <TicketItem key={compensation.id} compensation={compensation} />
        ))}
      </div>
    </div>
  );
};
