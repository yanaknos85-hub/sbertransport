import React from 'react';
import { OrUndefined } from '@sber-sbertransport/mf-core';
import { BonusesRequest } from 'stores/Limits/Limit.interface';

import { requestsVariation } from 'utils/Bonuses/RequestsVariation';

import styles from '../../assets/styles/Bonuses.module.scss';
import { ACCOUNT_TABLE_TEXTS } from '../../Constants/Account.constants';
import { TableRows } from './TableRows';

export const TransactionsTable: ({ bonuses }: { bonuses: OrUndefined<BonusesRequest[]> }) => JSX.Element = ({
  bonuses,
}: {
  bonuses: OrUndefined<BonusesRequest[]>;
}): JSX.Element => (
  <div className={styles.table}>
    <span className={styles.tableTitle}>{ACCOUNT_TABLE_TEXTS.tableTitle}</span>
    <div className={styles.tableContent}>
      {bonuses?.map((bonus: BonusesRequest, index: number) => (
        <div key={+index}>
          {index < 1 && <TableRows key={`_${+index}`} />}
          <TableRows bonus={requestsVariation(bonus)} index={index + 1} />
        </div>
      ))}
    </div>
  </div>
);
