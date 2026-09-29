import React, { FC } from 'react';
import { CloseCircleOutlined } from '@ant-design/icons';
import { useTranslation } from 'i18n';
import { Messages } from 'modules/Planner/Components/EtrnSignature/constants/CryptoPro';

import styles from './styles.module.scss';

interface Props {
  messages: Messages[];
}

const Error: FC<Props> = ({ messages }) => {
  const { signModal: i18 } = useTranslation().t.Etrn;
  const isWorkplaceFailed = messages.some(message =>
    ([Messages.NoCspProvider, Messages.NoCadesPlugin] as Messages[]).includes(message));

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
          {messages.map(message => <li key={message}>{i18.error[message]}</li>)}
        </ul>
      )}
    </div>
  );
};

export default Error;
