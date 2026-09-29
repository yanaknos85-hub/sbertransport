import React, { useMemo } from 'react';
import { ColumnProps } from 'antd/es/table';
import moment from 'moment';
import { EtrnSignatureResponseType } from 'modules/Planner/Components/EtrnSignature/types';
import { useTranslation } from 'i18n';
import { HeaderWithIcon } from 'modules/Planner/Components/EtrnSignature/components/HeaderWithIcon';
import { ETRN_TOOLTIPS, EtrnStatusNames, getStatusTone } from 'modules/Planner/Components/EtrnSignature/constants';
import { DATE_FORMAT, emptySign } from 'constants/constants.app';

import styles from 'modules/Planner/Components/EtrnSignature/styles.module.scss';

interface UseTableFieldsParams {
  onOpenCard: (cardId: string) => void;
}

export const useTableFields = ({ onOpenCard }: UseTableFieldsParams) => {
  const { t } = useTranslation();

  return useMemo<ColumnProps<EtrnSignatureResponseType>[]>(() => [
    {
      dataIndex: 'humanReadableId',
      key: 'humanReadableId',
      title: <HeaderWithIcon title={t.Planner.Etrn.numberEtrn} tooltip={ETRN_TOOLTIPS.number} />,
      align: 'center',
      width: 150,
      render: (value, record) => (
        <div
          className={styles.etrnTable__link}
          onClick={() => onOpenCard?.(record.id)}
          style={{ cursor: 'pointer' }}
        >
          {record.humanReadableId}
        </div>
      ),
    },
    {
      dataIndex: 'status',
      key: 'status',
      title: t.Planner.Etrn.status,
      align: 'center',
      width: 280,
      render: (value, record) => {
        const tone = getStatusTone(record.status);
        const classMap = { green: styles.etrnTable__statusGreen, gray: styles.etrnTable__statusGray };
        return (
          <span className={`${styles.etrnTable__statusBadge} ${classMap[tone]}`}>
            {EtrnStatusNames[record.status]}
          </span>
        );
      },
    },
    {
      dataIndex: 'sla',
      key: 'sla',
      title: <HeaderWithIcon title={t.Planner.Etrn.sla} tooltip={ETRN_TOOLTIPS.sla} />,
      align: 'center',
      width: 180,
      render: (value) => (
        <div>
          {value
            ? moment(value).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)
            : emptySign}
        </div>
      ),
    },
    {
      dataIndex: 'currentTitle',
      key: 'currentTitle',
      title: <HeaderWithIcon
        title={t.Planner.Etrn.currentTitle}
        tooltip={ETRN_TOOLTIPS.currentTitle}
      />,
      align: 'center',
      width: 150,
      render: (value) => (
        <div>
          {value
            ? value
            : emptySign}
        </div>
      ),
    },
    {
      dataIndex: 'senderName',
      key: 'senderName',
      title: t.Planner.Etrn.senderName,
      align: 'center',
      width: 200,
      render: (value) => (
        <div>
          {value
            ? value
            : emptySign}
        </div>
      ),
    },
    {
      dataIndex: 'receiverName',
      key: 'receiverName',
      title: t.Planner.Etrn.receiverName,
      align: 'center',
      width: 200,
      render: (value) => (
        <div>
          {value
            ? value
            : emptySign}
        </div>
      ),
    },
    {
      dataIndex: 'carrierName',
      key: 'carrierName',
      title: t.Planner.Etrn.carrierName,
      align: 'center',
      width: 200,
      render: (value) => (
        <div>
          {value
            ? value
            : emptySign}
        </div>
      ),
    },
  ], [t, onOpenCard]);
};