import { OrUndefined } from '@sber-sbertransport/mf-core';

import { Progress, Tooltip } from 'antd';
import { observer } from 'mobx-react';
import React from 'react';
import { Bonuses } from 'stores/Limits/Limit.interface';

import { BonusesSvg } from '../../assets/images/svg';
import styles from '../../assets/styles/Bonuses.module.scss';
import { TransactionsTable } from '../../Components/Account/TransactionsTable';
import { ACCOUNT_TABLE_TEXTS } from '../../Constants/Account.constants';
import { Button } from 'shared/components/Button/Button';

export const SecondBloc: ({ bonuses }: { bonuses: OrUndefined<Bonuses> }) => JSX.Element = observer(
  ({ bonuses }: { bonuses: OrUndefined<Bonuses> }) => {
    const calcPercent: OrUndefined<number> = bonuses && 100 / (bonuses.sum / bonuses.balance);
    const calcBalance: OrUndefined<string> = bonuses && (bonuses.balance / 100).toLocaleString();

    return (
      <div className={styles.secondBloc}>
        <div className={styles.bonusContent}>
          <div className={styles.bonusHeader}>
            <div className={styles.bonusIcon}>{BonusesSvg().Bonus()}</div>
            <div className={styles.progressBar}>
              <span className={styles.bonusText}>{ACCOUNT_TABLE_TEXTS.bonusText}</span>
              <Progress
                percent={calcPercent ?? 0}
                size="small"
                strokeColor="#19B150"
                showInfo={false}
              />
              <span className={styles.bonusCash}>
                {calcBalance ?? 0}
                {' '}
                ₽
              </span>
            </div>
          </div>
          <Tooltip title={ACCOUNT_TABLE_TEXTS.devExtra} trigger="click">
            <Button
              type="primary"
              className={styles.button}
            >
              {ACCOUNT_TABLE_TEXTS.submit}
            </Button>
          </Tooltip>

        </div>
        <TransactionsTable bonuses={bonuses?.requests} />
      </div>
    );
  }
);
