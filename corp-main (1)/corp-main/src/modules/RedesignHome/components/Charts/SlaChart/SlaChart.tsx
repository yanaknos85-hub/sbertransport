import React, { FC } from 'react';
import { Progress, Tooltip } from 'antd';
import cn from 'classnames';
import HelpIcon from 'shared/icons/help.svg';

import { IData } from 'modules/RedesignHome/types/Home.types';
import { formatNumber } from 'utils/formatNumber';
import { conicColorsNegative, conicColorsPositive, SLA_NORM } from 'modules/RedesignHome/constants/charts';

import styles from './SlaChart.module.scss';

interface IChartsData {
  sla?: IData[];
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const SlaChart: FC<IChartsData> = ({ sla }) => {
  const withOutViolation = sla ? formatNumber(sla.find(el => el.type === 'WITHOUTVIOLATION')?.value) : '-';
  const withViolation = sla ? formatNumber(sla.find(el => el.type === 'WITHVIOLATION')?.value) : '-';
  const canceled = sla ? formatNumber(sla.find(el => el.type === 'CANCELED')?.value) : '-';
  const withOutViolationProc = sla ? formatNumber(sla.find(el => el.type === 'WITHOUTVIOLATIONPROC')?.value) : 0;

  const hasWithOutViolationProc = withOutViolationProc !== 0;
  const conicColors = withOutViolationProc < SLA_NORM ? conicColorsNegative : conicColorsPositive;

  return (
    <div className={styles.wrapperBudget}>
      <div className={styles.header}>
        <div className={styles.header_title}>
          <p>SLA</p>
          <Tooltip title="Кол-во заявок, выполненных без нарушения ⁣⁣контрольных сроков / Общее кол-во заявок, в %">
            <img src={HelpIcon} alt="help" />
          </Tooltip>
        </div>
        <p>Уровень исполнения обязательств</p>
      </div>
      <div className={styles.main}>
        <div className={styles.chart}>
          <div className={styles.mainChart}>
            <Progress
              percent={withOutViolationProc}
              type="dashboard"
              strokeColor={conicColors}
              format={() => (
                <div className={styles.chartCircle}>
                  <p className={cn(hasWithOutViolationProc ? styles.numberAll : styles.notAll)}>
                    {hasWithOutViolationProc ? `${withOutViolationProc}%` : 'Нет данных'}
                  </p>
                </div>
              )}
            />
          </div>
          <div className={styles.backgroundChart}>
            <Progress
              type="dashboard"
              percent={SLA_NORM}
              strokeColor="rgba(0, 0, 0, 0.1)"
              showInfo={false}
            />
          </div>
          <p>{`Норма ${SLA_NORM}%`}</p>
        </div>
        <div className={styles.detailedInformation}>
          <div className={styles.activeMarker}>
            <div>
              <p>Без нарушений</p>
            </div>
            <div>
              <p>{withOutViolation}</p>
            </div>
          </div>
          <div className={styles.workingMarker}>
            <div>
              <p>С нарушениями</p>
            </div>
            <div>
              <p>{withViolation}</p>
            </div>
          </div>
          <div className={styles.closeMarker}>
            <div>
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
