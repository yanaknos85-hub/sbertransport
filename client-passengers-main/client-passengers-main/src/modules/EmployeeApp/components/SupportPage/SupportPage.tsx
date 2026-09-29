import { MailOutlined, PhoneOutlined, SendOutlined } from '@ant-design/icons';
import React, { FC } from 'react';

import { EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import PageLayout from 'shared/components/PageLayout/PageLayout';

import styles from './styles.module.scss';

const SupportPage: FC = () => (
  <PageLayout title={EmployeeAppLinksTitles.support}>
    <div className={styles.container}>
      <p>
        <PhoneOutlined className={styles.icon} />
        <a className={styles.link} href="tel:88007074882">
          8-800-707-48-82
        </a>
        {' '}
        (внешний)
      </p>
      <p>
        <PhoneOutlined className={styles.icon} />
        <a className={styles.link} href="tel:855990047">
          8-559-90-047
        </a>
        {' '}
        (внутренний)
      </p>
      <p>
        <SendOutlined className={styles.icon} />
        Служба поддержки в
        {' '}
        <a
          className={styles.link}
          target="_blank"
          href="https://sberchat.sberbank.ru/@SBERTRANSPORT"
          rel="noopener noreferrer"
        >
          СберЧате
        </a>
      </p>
      <p>
        <MailOutlined className={styles.icon} />
        Для жалоб и предложений напишите нам
        {' '}
        <a className={styles.link} href="mailto:catransport@sberbank.ru">
          catransport@sberbank.ru
        </a>
      </p>
    </div>
  </PageLayout>
);

export default SupportPage;
