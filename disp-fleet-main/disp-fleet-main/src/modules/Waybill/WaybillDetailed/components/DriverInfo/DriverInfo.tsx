import React from 'react';
import type { FC } from 'react';
import { Avatar } from 'antd';
import { PhoneOutlined, UserOutlined } from '@ant-design/icons';
import moment from 'moment';

import { useTranslation } from 'i18n';
import { DATE_FORMAT } from 'constants/app.constants';
import Card from '../../components/Card';
import { getFullName } from 'utils/getFullName';
import { formatPhoneNumber } from 'utils/formatPhoneNumber';

import styles from './DriverInfo.module.scss';

interface Props {
  firstName: string;
  lastName: string;
  patronymic?: string;
  personnelNumber: string;
  mobilePhone: string;
  series: string;
  number: number;
  issueDate: string;
}

const DriverInfo: FC<Props> = ({
  firstName,
  lastName,
  patronymic,
  personnelNumber,
  mobilePhone,
  series,
  number,
  issueDate,
}) => {
  const { driver: i18 } = useTranslation().t.Waybill.detailed.common;

  const fullName = getFullName({
    firstName,
    lastName,
    patronymic,
  });

  return (
    <Card
      title={(
        <>
          <div className={styles.header__title}>{i18.title}</div>

          <PhoneOutlined rotate={90} className={styles.header__icon} />
        </>
      )}
    >
      <Avatar
        size="default"
        icon={<UserOutlined />}
        className={styles.content__avatar}
      />

      <div className={styles.description}>
        <span className={styles.description__header}>{fullName}</span>

        <div className={styles.description__section}>
          <div className={styles.personBlock}>
            {formatPhoneNumber(mobilePhone)}
          </div>

          <div className={styles.divider} />

          <div className={styles.personBlock}>
            <span className={styles.personBlock__title}>{i18.personnelNumber}</span>

            <span>{personnelNumber}</span>
          </div>
        </div>

        <div className={styles.licenseSection}>
          <div className={styles.licenseBlock}>
            <span>{i18.license}</span>

            <span className={styles.licenseBlock__number}>
              {`${series} ${number}`}
            </span>
          </div>

          <div className={styles.licenseBlock}>
            <span>{i18.dateBy}</span>

            <span>
              {moment(issueDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
            </span>
          </div>
        </div>
      </div>
    </Card>
  );
};

export default DriverInfo;
