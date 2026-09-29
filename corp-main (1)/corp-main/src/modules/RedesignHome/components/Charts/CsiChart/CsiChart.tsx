import React, { FC } from 'react';
import { Progress, Tooltip } from 'antd';
import cn from 'classnames';

import { IData } from 'modules/RedesignHome/types/Home.types';
import HelpIcon from 'shared/icons/help.svg';
import { formatNumber } from 'utils/formatNumber';
import { conicColorsNegative, conicColorsPositive, CSI_NORM } from 'modules/RedesignHome/constants/charts';

import styles from './CsiChart.module.scss';

interface IChartsData {
  csi?: IData[];
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const CsiChart: FC<IChartsData> = ({ csi }) => {
  const markHi = csi ? formatNumber(csi.find(el => el.type === 'MARKHI')?.value) : '-';
  const markLow = csi ? formatNumber(csi.find(el => el.type === 'MARKLOW')?.value) : '-';
  const markNot = csi ? formatNumber(csi.find(el => el.type === 'MARKNOT')?.value) : '-';
  const customerSatisfactionProc = csi ? formatNumber(csi.find(el => el.type === 'CUSTOMERSATISFACTIONPROC')?.value) : 0;
  const hasCustomerSatisfaction = customerSatisfactionProc !== 0;
  const conicColors = customerSatisfactionProc < CSI_NORM ? conicColorsNegative : conicColorsPositive;

  return (
    <div className={styles.wrapperBudget}>
      <div className={styles.header}>
        <div className={styles.header_title}>
          <p>CSI</p>
          <Tooltip title="Кол-во заявок с оценкой 4 и 5 звезд / Общее кол-во заявок с оценками, в %.">
            <img src={HelpIcon} alt="help" />
          </Tooltip>
        </div>
        <p>Индекс удовлетворенности клиентов</p>
      </div>
      <div className={styles.main}>
        <div className={styles.chart}>
          <div className={styles.mainChart}>
            <Progress
              percent={customerSatisfactionProc}
              type="dashboard"
              strokeColor={conicColors}
              format={() => (
                <div className={styles.chartCircle}>
                  <p className={cn(hasCustomerSatisfaction ? styles.numberAll : styles.notAll)}>
                    {hasCustomerSatisfaction ? `${customerSatisfactionProc}%` : 'Нет данных'}
                  </p>
                </div>
              )}
            />
          </div>
          <div className={styles.backgroundChart}>
            <Progress
              type="dashboard"
              percent={CSI_NORM}
              strokeColor="rgba(0, 0, 0, 0.1)"
              showInfo={false}
            />
          </div>
          <p>{`Норма ${CSI_NORM}%`}</p>
        </div>
        <div className={styles.detailedInformation}>
          <div className={styles.activeMarker}>
            <div>
              <p>Положительная</p>
            </div>
            <div>
              <p>{markHi}</p>
            </div>
          </div>
          <div className={styles.workingMarker}>
            <div>
              <p>Отрицательная</p>
            </div>
            <div>
              <p>{markLow}</p>
            </div>
          </div>
          <div className={styles.closeMarker}>
            <div>
              <p>Без оценки</p>
            </div>
            <div>
              <p>{markNot}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
