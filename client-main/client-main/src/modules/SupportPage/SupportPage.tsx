import React, { FC } from 'react';
import { MailOutlined, PhoneOutlined, SendOutlined } from '@ant-design/icons';
import cn from 'classnames';

import { Contacts, SDOContacts, EmployeeAppLinksTitles } from 'constants/constants.app';
import { StoreNames, useAppStore } from 'stores';
import PageLayout from 'shared/components/PageLayout/PageLayout';

import styles from './styles.module.scss';

const SupportPage: FC = () => {
  const { [StoreNames.configStore]: configStore } = useAppStore();
  const IS_SDO = configStore.env.IS_SDO;

  return (
    <PageLayout title={EmployeeAppLinksTitles.support}>
      <div className={styles.container}>
        <p>
          <PhoneOutlined className={styles.icon} />
          <a className={styles.link} href={IS_SDO ? SDOContacts.linkTel : Contacts.linkOutTel}>
            {IS_SDO ? SDOContacts.tel : Contacts.outTel}
          </a>
          {' '}
          {IS_SDO ? '(контактный номер)' : '(внешний)'}
        </p>
        {!IS_SDO && (
          <>
            <p>
              <PhoneOutlined className={styles.icon} />
              <a className={styles.link} href={Contacts.linkInsideTel}>
                {Contacts.insideTel}
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
                href={Contacts.linkChatSbertransport}
                rel="noopener noreferrer"
              >
                СберЧате
              </a>
            </p>
          </>
        )}
        <p>
          <MailOutlined className={styles.icon} />
          Для жалоб и предложений напишите нам
          {' '}
          <a
            className={cn(styles.link, styles.email)}
            href={IS_SDO ? SDOContacts.linkMailSbertransport : Contacts.linkMailSbertransport}
          >
            {IS_SDO ? SDOContacts.mailSbertransport : Contacts.mailSbertransport}
          </a>
        </p>
      </div>
    </PageLayout>
  );
};

export default SupportPage;
