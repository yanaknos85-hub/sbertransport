import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';

import styles from './styles.module.scss';

interface Props {
  type: 'init' | 'signing';
}

const Loading: FC<Props> = ({ type }) => {
  const { signModal: i18 } = useTranslation().t.Etrn;

  return (
    <div className={styles.container}>
      <span className={styles.container__title}>{i18.loading[type]}</span>
      <SpinWrapped />
      <span>{i18.loading.warning}</span>
    </div>
  );
};

export default Loading;
