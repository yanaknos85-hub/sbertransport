import React, { FC } from 'react';
import { InputNumber } from 'antd';
import OrderTitle from '../../constants';

import styles from  './styles.module.scss'

interface Props {
  value: number;
  onChange: (value) => void;
}

export const LoadersCount: FC<Props> = (props) => {

  const { onChange, value } = props;

  return (
    <div className={styles.wrapper}>
      <div className={styles.title}>
        {OrderTitle.loaders}
      </div>
      <InputNumber
        min={1}
        step={1}
        max={9}
        value={value}
        defaultValue={value}
        onChange={onChange}
      />
    </div>
  )
};
