import React, { memo, FC } from 'react';
import cn from 'classnames';
import { Tabs } from 'antd';
import { TabsProps } from 'antd/lib/tabs';

import './styles.scss';

type Props = TabsProps & { themeSize?: 'xl' | 'l' };

export const Tab: FC<Props> = memo(({
  children, themeSize, className, ...other
}) => (
  <Tabs className={cn(className, { [`tab_size-${themeSize}`]: themeSize })} {...other}>
    {children}
  </Tabs>
));
