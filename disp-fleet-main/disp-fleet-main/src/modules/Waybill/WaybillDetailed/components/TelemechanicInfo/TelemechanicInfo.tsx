import React from 'react';
import type { FC } from 'react';
import { Avatar } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';
import moment from 'moment';

import odometerIcon from 'assets/icons/odometer.svg';

import { DATE_FORMAT } from 'constants/app.constants';
import Panel from 'components/Panel/Panel';
import { TelemechType } from 'modules/Waybill/Waybill.constants';

import styles from './TelemechanicInfo.module.scss';

interface Props {
  type: TelemechType;
  firstName: string;
  lastName: string;
  patronymic: string;
  position: string;
  decisionTime: string;
  success: boolean;
  mileage: number;
}

const TelemechanicInfo: FC<Props> = ({
  type,
  firstName,
  lastName,
  patronymic,
  position,
  decisionTime,
  success,
  mileage,
}) => {
  const { inspections: i18 } = useTranslation().t.Waybill.detailed;

  const fullName = `${firstName} ${lastName} ${patronymic}`.trim();

  return (
    <Panel className={styles.panel}>
      <div className={styles.container}>
        <div className={styles.header}>
          <div className={styles.header__title}>
            {type === TelemechType.IN ? i18.telemechanic.inTitle : i18.telemechanic.outTitle}
          </div>

          {success && <div className={styles.header__status}>{i18.success.toUpperCase()}</div>}
        </div>

        <div className={styles.content}>
          <Avatar
            size="default"
            icon={<UserOutlined />}
            className={styles.content__avatar}
          />

          <div className={styles.description}>
            <div className={styles.personBlock}>
              <span>{fullName}</span>

              <span className={styles.personBlock__spec}>{`(${position})`}</span>
            </div>

            <span className={styles.date}>{moment(decisionTime).format(DATE_FORMAT.BASE_REVERTED_DOTS)}</span>

            <div className={styles.odometerBlock}>
              <img
                src={odometerIcon}
                className={styles.odometerBlock__icon}
                alt="odometer"
              />

              <span className={styles.odometerBlock__label}>{i18.telemechanic.odometer}</span>

              <span className={styles.odometerBlock__value}>{`${mileage} км`}</span>
            </div>
          </div>
        </div>
      </div>
    </Panel>
  );
};

export default TelemechanicInfo;
