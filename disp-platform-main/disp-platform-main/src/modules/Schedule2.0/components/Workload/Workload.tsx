import React, { FC } from 'react';

import { Tooltip } from 'antd';

import { useTranslation } from 'i18n';

import { useWorkloadGeneral } from 'modules/Schedule2.0/context/workload.context';

import { RainBow } from '../RainBow/RainBow';

import { ReactComponent as Info } from 'assets/icons/info.svg';

import styles from './Workload.module.scss';

export const Workload: FC = () => {
  const { t } = useTranslation();
  const { workloadGeneral } = useWorkloadGeneral();

  return (
    <div className={styles.container}>
      <span className={styles.title}>
        {t.Shifts.workloadTitle}
        <Tooltip
          overlayClassName={styles.tooltip}
          title={() => (
            <p>
              <span>{t.Shifts.workloadTitle}</span>
              Показатель времени, в течение которого автомобили находятся на линии, выполняя поездки, относительно
              выбранного расчётного периода
              <span>Загруженность считается</span>
              По мере исполнения заявок, исходя из фактических данных по выполненным поездкам
            </p>
          )}
        >
          <Info />
        </Tooltip>
      </span>
      <RainBow value={workloadGeneral} size="middle" />
    </div>
  );
};
