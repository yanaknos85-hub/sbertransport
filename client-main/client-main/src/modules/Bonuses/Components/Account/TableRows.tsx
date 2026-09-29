import React from 'react';
import cn from 'classnames';

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
      <div
        key={`${bonus?.id}${column.type}`}
        className={cn(styles.tableRowTd, [(styles as any)[column.style]])}
      >
        <span
          className={cn({
            [styles.tableColumnText]: bonus,
          })}
        >
          {bonus ? (column.type ? (bonus as any)[column.type] : index) : column.name}
        </span>
      </div>
    ))}
  </div>
);
