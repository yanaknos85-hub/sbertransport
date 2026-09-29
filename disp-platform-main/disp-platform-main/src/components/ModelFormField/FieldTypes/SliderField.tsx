import React from 'react';
import { Form } from 'antd';
import Slider from 'antd/lib/slider';
import styles from './SliderFields.module.scss';

import { SliderFieldProps } from '../ModelFormField';

export default (props: SliderFieldProps): JSX.Element => {
  const {
    editable, name, label, min, max, ...other
  } = props;

  return (
    <Form.Item
      name={name}
      label={label}
      shouldUpdate
    >
      <div className={styles.container}>
        <div className={styles.item}>{min}</div>
        <div className={styles.wrapper}>
          <Slider {...other} disabled={!editable} />
        </div>
        <div className={styles.item}>{max}</div>
      </div>
    </Form.Item>
  );
};
