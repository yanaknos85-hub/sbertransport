import React, { FC } from 'react';
import cn from 'classnames';
import { Tooltip } from 'antd';

import { IData, ISections } from 'modules/RedesignHome/types/Home.types';

import { ThinPieChart } from '../Budget/ThinPieChart/ThinPieChart';
import { calculatePercentage } from 'utils/calculatePercentage';
import HelpIcon from 'shared/icons/help.svg';
import { formatNumber } from 'utils/formatNumber';

import styles from './TotalNumberChart.module.scss';

interface IChartsData {
  totalCounts?: IData[];
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const TotalNumberChart: FC<IChartsData> = ({ totalCounts }) => {
  const done = totalCounts ? formatNumber(totalCounts.find(el => el.type === 'DONE')?.value) : '-';
  const canceled = totalCounts ? formatNumber(totalCounts.find(el => el.type === 'CANCELED')?.value) : '-';
  const inwork = totalCounts ? formatNumber(totalCounts.find(el => el.type === 'INWORK')?.value) : '-';
  const all = totalCounts ? formatNumber(totalCounts.find(el => el.type === 'ALL')?.value) : 'Нет данных';
  const percentDone = totalCounts && calculatePercentage(done, [canceled, inwork]);
  const percentCanceled = totalCounts && calculatePercentage(canceled, [done, inwork]);
  const percentInwork = totalCounts && calculatePercentage(inwork, [canceled, done]);
  const sectionsColor = totalCounts ? [
    { percentage: percentDone, color: 'rgb(50, 191, 84)' },
    { percentage: percentInwork, color: 'rgb(11, 147, 198)' },
    { percentage: percentCanceled, color: 'rgb(255, 154, 50)' }]
    : [{ percentage: 100, color: 'rgb(224, 224, 224)' }];

  return (
    <div className={styles.wrapperBudget}>
      <div className={styles.header}>
        <div className={styles.header_title}>
          <p>Общее количество</p>
          <Tooltip title={`Количество заявок: Выполнено: количество заявок в статусе "Поездка завершена" "Выплата произведена"`}>
            <img src={HelpIcon} alt="help" />
          </Tooltip>
        </div>
        <p>Статистика выполнения заявок</p>
      </div>
      <div className={styles.main}>
        <div className={styles.chart}>
          <ThinPieChart
            sections={sectionsColor as ISections[]}
            gap={totalCounts ? 2.3 : 0}
            size={134}
          >
            <div className={styles.chartCircle}>
              <p className={cn(totalCounts && all !== 0 ? styles.numberAll : styles.notAll)}>{all === 0 ? 'Нет данных' : all}</p>
            </div>
          </ThinPieChart>
        </div>
        <div className={styles.detailedInformation}>
          <div className={styles.activeMarker}>
            <div>
              <div className={styles.marker} />
              <p>Выполнено</p>
            </div>
            <div>
              <p>{done}</p>
            </div>
          </div>
          <div className={styles.reservedMarker}>
            <div>
              <div className={styles.marker} />
              <p>В работе</p>
            </div>
            <div>
              <p>{inwork}</p>
            </div>
          </div>
          <div className={styles.workingMarker}>
            <div>
              <div className={styles.marker} />
              <p>Отменено</p>
            </div>
            <div>
              <p>{canceled}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
