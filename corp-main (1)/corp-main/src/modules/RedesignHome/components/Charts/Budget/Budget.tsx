import React, { FC } from 'react';

import { ThinPieChart } from './ThinPieChart/ThinPieChart';
import InDevelopment from 'shared/images/inDevelopment.png';

import styles from './Budget.module.scss';

interface IChartsData {

}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const Budget: FC<IChartsData> = () => {
  // доработать как будет доделана ручка metrics, сейчас блок бюджета будет не рабочий
  // const sectionsColor = [
  //   { percentage: 60, color: 'rgb(203, 234, 16)' },
  //   { percentage: 20, color: 'rgb(50, 191, 84)' },
  //   { percentage: 20, color: 'rgb(11, 147, 198)' }];

  return (
    <div className={styles.wrapperBudget}>
      <div className={styles.header}>
        <p>Бюджет</p>
        <p>Лимит сервиса</p>
      </div>
      <div className={styles.main}>
        <div className={styles.chart}>
          <ThinPieChart
            sections={[{ percentage: 100, color: 'rgb(224, 224, 224)' }]}
            gap={0}
            size={152}
          >
            <div className={styles.chartCircle}>
              {/* <p>{formatRublesWithoutRemainder()}</p> */}
              <p className={styles.notAll}>Нет данных</p>
              {/* <p>Всего</p> */}
            </div>
          </ThinPieChart>
        </div>
        <div className={styles.detailedInformation}>
          <div className={styles.workingMarker}>
            <div>
              <div className={styles.marker} />
              <p>Факт</p>
            </div>
            <div>
              <p>-</p>
            </div>
          </div>
          <div className={styles.activeMarker}>
            <div>
              <div className={styles.marker} />
              <p>Остаток</p>
            </div>
            <div>
              <p>-</p>
            </div>
          </div>
          <div className={styles.reservedMarker}>
            <div>
              <div className={styles.marker} />
              <p>В резерве</p>
            </div>
            <div>
              <p>-</p>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.inDevelopment_wrapper}>
        <div className={styles.inDevelopment}>
          <img src={InDevelopment} alt="inDevelopment" />
          <span>Раздел находится в разработке</span>
        </div>
      </div>
    </div>
  );
};
