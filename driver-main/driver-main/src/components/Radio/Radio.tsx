import React from 'react';
import AntdRadio, { RadioProps } from 'antd-mobile/es/components/radio';
import cn from 'classnames';

import styles from './Radio.module.scss';

const Radio: React.FC<RadioProps> = ({ className, ...props }) => (
  <AntdRadio {...props} className={cn(styles.radio, [className])} />
);

export const RadioGroup = AntdRadio.Group;

export default Radio;
