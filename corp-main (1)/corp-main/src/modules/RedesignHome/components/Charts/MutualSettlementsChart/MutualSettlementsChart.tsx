import React, { FC } from 'react';
import cn from 'classnames';
import { Tooltip } from 'antd';

import { ThinPieChart } from '../Budget/ThinPieChart/ThinPieChart';
import { IData, ISections } from 'modules/RedesignHome/types/Home.types';
import { calculatePercentage } from 'utils/calculatePercentage';
import HelpIcon from 'shared/icons/help.svg';
import { formatNumber } from 'utils/formatNumber';

import styles from './MutualSettlementsChart.module.scss';

interface IChartsData {
  payments?: IData[];
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const MutualSettlementsChart: FC<IChartsData> = ({ payments }) => {
  const payment = payments ? formatNumber(payments.find(el => el.type === 'PAYMENT')?.value) : '-';
  const nopayment = payments ? formatNumber(payments.find(el => el.type === 'NOPAYMENT')?.value) : '-';
  const all = payments ? formatNumber(payments.find(el => el.type === 'ALL')?.value) : 'Нет данных';
  const percentPayment = payments && calculatePercentage(payment, [nopayment]);
  const percentNopayment = payments && calculatePercentage(nopayment, [payment]);
  const sectionsColor = payments ? [
    { percentage: percentPayment, color: 'rgb(103, 210, 52)' },
    { percentage: percentNopayment, color: 'rgb(255, 154, 50)' }]
    : [{ percentage: 100, color: 'rgb(224, 224, 224)' }];

  return (
    <div className={styles.wrapperBudget}>
      <div className={styles.header}>
        <div className={styles.header_title}>
          <p>Взаиморасчеты</p>
          <Tooltip title="Статистика осуществления взаиморасчетов между Заказчиком и Контрагентами">
            <img src={HelpIcon} alt="help" />
          </Tooltip>
        </div>
        <p>Статистика оплаты счетов</p>
      </div>
      <div className={styles.main}>
        <div className={styles.chart}>
          <ThinPieChart
            sections={sectionsColor as ISections[]}
            gap={payments ? 2 : 0}
            size={152}
          >
            <div className={styles.chartCircle}>
              <p className={cn(payments && all !== 0 ? styles.numberAll : styles.notAll)}>{all === 0 ? 'Нет данных' : all}</p>
              {payments && all !== 0
              && <p className={styles.allCount}>Всего</p>}
            </div>
          </ThinPieChart>
        </div>
        <div className={styles.detailedInformation}>
          <div className={styles.activeMarker}>
            <div>
              <div className={styles.marker} />
              <p>Оплачено</p>
            </div>
            <div>
              <p>{payment}</p>
            </div>
          </div>
          <div className={styles.reservedMarker}>
            <div>
              <div className={styles.marker} />
              <p>Не оплачено</p>
            </div>
            <div>
              <p>{nopayment}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
