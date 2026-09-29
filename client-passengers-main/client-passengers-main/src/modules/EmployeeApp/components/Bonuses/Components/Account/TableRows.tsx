/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-nested-ternary */
import React from 'react';

import { BonusesRequest } from 'stores/Limits/Limit.interface';

import styles from '../../assets/styles/Bonuses.module.scss';
import { ACCOUNT_TABLE_ROWS } from '../../Constants/Account.constants';

export const TableRows: ({ index, bonus }: { index?: number; bonus?: BonusesRequest }) => JSX.Element = ({
  index,
  bonus,
}: {
  index?: number;
  bonus?: BonusesRequest;
}): JSX.Element => (
  <div className={styles.tableRow}>
    {ACCOUNT_TABLE_ROWS.map(column => (
      <div key={`${bonus?.id}${column.type}`} className={styles.tableRowTd}>
        <span className={`${bonus && styles.tableColumnText} ${(styles as any)[column.style]}`}>
          {bonus ? (column.type ? (bonus as any)[column.type] : index) : column.name}
        </span>
      </div>
    ))}
  </div>
);
