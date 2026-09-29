import { Divider as DividerAntd } from 'antd';
import { DividerProps } from 'antd/lib/divider';
import React, { FC } from 'react';
import cn from 'classnames';
import styles from './divider.module.scss';

const Divider: FC<DividerProps> = ({ className, ...props }) => (
  <DividerAntd className={cn(styles.divider, className)} {...props} />
);

export default Divider;
