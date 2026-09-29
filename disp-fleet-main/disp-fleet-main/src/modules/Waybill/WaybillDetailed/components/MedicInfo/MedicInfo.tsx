import React from 'react';
import type { FC } from 'react';
import { Avatar } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import moment from 'moment';

import { useTranslation } from 'i18n';
import { DATE_FORMAT } from 'constants/app.constants';
import Panel from 'components/Panel/Panel';

import styles from './MedicInfo.module.scss';

interface Props {
  firstName: string;
  lastName: string;
  patronymic: string;
  position: string;
  decisionTime: string;
  success: boolean;
}

const MedicInfo: FC<Props> = ({
  firstName,
  lastName,
  patronymic,
  position,
  decisionTime,
  success,
}) => {
  const { inspections: i18 } = useTranslation().t.Waybill.detailed;

  const fullName = `${firstName} ${lastName} ${patronymic}`.trim();

  return (
    <Panel className={styles.panel}>
      <div className={styles.container}>
        <div className={styles.header}>
          <div className={styles.header__title}>{i18.medic.title}</div>

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
          </div>
        </div>
      </div>
    </Panel>
  );
};

export default MedicInfo;
