import React from 'react';
import type { FC } from 'react';
import { useTranslation } from 'i18n';

import SpinWrapped from 'components/SpinWrapped/SpinWrapped';

import styles from './Loading.module.scss';

interface Props {
  type: 'init' | 'signing';
}

const Loading: FC<Props> = ({ type }) => {
  const { sign: i18 } = useTranslation().t.Waybill.modal;

  return (
    <div className={styles.container}>
      <span className={styles.container__title}>{i18.loading[type]}</span>
      <SpinWrapped />
      <span>{i18.loading.warning}</span>
    </div>
  );
};

export default Loading;
