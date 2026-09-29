import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { ColumnProps } from 'antd/lib/table';
import { emptySign } from 'constants/constants.app';
import { SchedulerTableContentType } from './SchedulerTableContent';
import { formatBaseDate, formatTimeDate } from 'utils/formatTime';
import { convertToRubles } from 'utils/convertToRubles';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';
import { formatVolume } from 'utils/formatVolume';
import { formatDistanceValue } from 'utils/formatDistance';

import styles from 'modules/Engineers/Engineer.module.scss';

interface Params {
  setOrderId: (param: string) => void;
  setModalVisible:  (param: boolean) => void;
}

export const useTableFields: ({ setOrderId, setModalVisible }: Params) => ColumnProps<SchedulerTableContentType>[] = (({ setOrderId, setModalVisible }: Params) => {
  const { t } = useTranslation();

  return useMemo<ColumnProps<SchedulerTableContentType>[]>(
    () => [
      {
        dataIndex: 'humanReadableId',
        key: 'humanReadableId',
        title: t.SchedulerMonitor.humanReadableId,
        align: 'center',
        width: 220,
        render: humanReadableId => (
          <span>{humanReadableId}</span>
        ),
      },

      {
        dataIndex: 'status',
        key: 'status',
        title: t.SchedulerMonitor.status,
        width: 150,
        render: status => (
          <div className={styles.outer}>
            <span>{status}</span>
          </div>
        ),
      },

      {
        dataIndex: 'tariffType',
        key: 'tariffType',
        title: t.SchedulerMonitor.tariffType,
        width: 150,
        render: tariffType => (
          <div className={styles.outer}>
            <span>{tariffType || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'nextRequestDate',
        key: 'nextRequestDate',
        title: t.SchedulerMonitor.nextRequestDate,
        width: 150,
        render: nextRequestDate => (
          <div className={styles.outer}>
            <span>{formatBaseDate(nextRequestDate) || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'lastRequestDate',
        key: 'lastRequestDate',
        title: t.SchedulerMonitor.lastRequestDate,
        width: 150,
        render: lastRequestDate => (
          <div className={styles.outer}>
            <span>{formatBaseDate(lastRequestDate) || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'period',
        key: 'period',
        title: t.SchedulerMonitor.period,
        width: 220,
        render: (period) => {
          return (
            <div className={styles.outer}>
              <p>{period}</p>
            </div>
          );
        },
      },
      {
        dataIndex: 'countRequests',
        key: 'countRequests',
        title: t.SchedulerMonitor.countRequests,
        width: 150,
        render: countRequests => (
          <div className={styles.outer}>
            <span>{countRequests ?? emptySign}</span>
          </div>
        ),
      },
      {
        dataIndex: 'countRequestsInRoute',
        key: 'countRequestsInRoute',
        title: t.SchedulerMonitor.countRequestsInRoute,
        width: 150,
        render: countRequestsInRoute => (
          <div className={styles.outer}>
            <span>{countRequestsInRoute ?? emptySign}</span>
          </div>
        ),
      },
      {
        dataIndex: 'author',
        key: 'author',
        title: t.SchedulerMonitor.author,
        width: 190,
        render: author => (
          <div className={styles.outer}>
            <span>{author || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'authorMobilePhone',
        key: 'authorMobilePhone',
        title: t.SchedulerMonitor.authorMobilePhone,
        width: 190,
        render: authorMobilePhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(authorMobilePhone)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'carrier',
        key: 'carrier',
        title: t.SchedulerMonitor.carrier,
        width: 190,
        render: carrier => (
          <div className={styles.outer}>
            <span>{carrier || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'senderAddress',
        key: 'senderAddress',
        title: t.SchedulerMonitor.senderAddress,
        width: 190,
        render: senderAddress => (
          <div className={styles.outer}>
            <span>{senderAddress}</span>
          </div>
        ),
      },

      {
        dataIndex: 'sender',
        key: 'sender',
        title: t.SchedulerMonitor.sender,
        width: 190,
        render: sender => (
          <div className={styles.outer}>
            <span>{sender}</span>
          </div>
        ),
      },

      {
        dataIndex: 'senderPhone',
        key: 'senderPhone',
        title: t.SchedulerMonitor.senderPhone,
        width: 190,
        render: senderPhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(senderPhone)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'senderOrganization',
        key: 'senderOrganization',
        title: t.SchedulerMonitor.senderOrganization,
        width: 190,
        render: senderOrganization => (
          <div className={styles.outer}>
            <span>{senderOrganization || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipientAddress',
        key: 'recipientAddress',
        title: t.SchedulerMonitor.recipientAddress,
        width: 190,
        render: recipientAddress => (
          <div className={styles.outer}>
            <span>{recipientAddress}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipient',
        key: 'recipient',
        title: t.SchedulerMonitor.recipient,
        width: 190,
        render: recipient => (
          <div className={styles.outer}>
            <span>{recipient}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipientPhone',
        key: 'recipientPhone',
        title: t.SchedulerMonitor.recipientPhone,
        width: 190,
        render: recipientPhone => (
          <div className={styles.outer}>
            <span>{formatPhoneNumber(recipientPhone)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'recipientOrganization',
        key: 'recipientOrganization',
        title: t.SchedulerMonitor.recipientOrganization,
        width: 190,
        render: recipientOrganization => (
          <div className={styles.outer}>
            <span>{recipientOrganization || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'plannedRange',
        key: 'plannedRange',
        title: t.SchedulerMonitor.plannedRange,
        width: 190,
        render: plannedRange => (
          <div className={styles.outer}>
            <span>{formatDistanceValue(plannedRange) || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'plannedPrice',
        key: 'plannedPrice',
        title: t.SchedulerMonitor.plannedPrice,
        width: 190,
        render: plannedPrice => (
          <div className={styles.outer}>
            <span>{convertToRubles(plannedPrice).toString().replace('.', ',') || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'cargoType',
        key: 'cargoType',
        title: t.SchedulerMonitor.cargoType,
        width: 190,
        render: cargoType => (
          <div className={styles.outer}>
            <span>{cargoType || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'loaders',
        key: 'loaders',
        title: t.SchedulerMonitor.loaders,
        width: 190,
        render: loaders => (
          <div className={styles.outer}>
            <span>{loaders || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'weight',
        key: 'weight',
        title: t.SchedulerMonitor.weight,
        width: 190,
        render: weight => (
          <div className={styles.outer}>
            <span>{weight}</span>
          </div>
        ),
      },

      {
        dataIndex: 'volume',
        key: 'volume',
        title: t.SchedulerMonitor.volume,
        width: 190,
        render: volume => (
          <div className={styles.outer}>
            <span>{formatVolume(volume)}</span>
          </div>
        ),
      },

      {
        dataIndex: 'creationTime',
        key: 'creationTime',
        title: t.SchedulerMonitor.creationTime,
        width: 190,
        render: creationTime => (
          <div className={styles.outer}>
            <span>{formatTimeDate(creationTime) || emptySign}</span>
          </div>
        ),
      },

      {
        dataIndex: 'comment',
        key: 'comment',
        title: t.SchedulerMonitor.comment,
        width: 200,
        render: comment => (
          <div className={styles.outer}>
            <span>{comment || emptySign}</span>
          </div>
        ),
      },
    ],
    [t]
  );
});
