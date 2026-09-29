import React from 'react';
import type { FC } from 'react';
import { CloseCircleOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';

import styles from './Error.module.scss';
import { Messages } from 'constants/cryptoPro';

interface Props {
  messages: Messages[];
}

const Error: FC<Props> = ({ messages }) => {
  const { sign: i18 } = useTranslation().t.Waybill.modal;
  const isWorkplaceFailed = messages
    .some(message => ([Messages.NoCspProvider, Messages.NoCadesPlugin] as Messages[]).includes(message));

  return (
    <div className={styles.container}>
      <div className={styles.title}>
        <CloseCircleOutlined />

        <span>
          {isWorkplaceFailed ? i18.error.invalidWorkplace : i18.error[messages[0]]}
        </span>
      </div>

      {isWorkplaceFailed && (
        <ul>
          {messages.map(message => <li>{i18.error[message]}</li>)}
        </ul>
      )}
    </div>
  );
};

export default Error;
