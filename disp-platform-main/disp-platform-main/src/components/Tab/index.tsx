import React, { memo, FC } from 'react';
import cn from 'classnames';
import { Tabs } from 'antd';
import { TabsProps } from 'antd/lib/tabs';

import './styles.scss';
import styles from './styles.module.scss';

type Props = TabsProps & { themeSize?: 'xl' | 'l'; card?: boolean };

export const Tab: FC<Props> = memo(({
  children, themeSize, className, card, ...other
}) => (
  <Tabs className={cn(styles.tabs, className, { [`tab_size-${themeSize}`]: themeSize, [styles.card]: card })} {...other}>
    {children}
  </Tabs>
));
