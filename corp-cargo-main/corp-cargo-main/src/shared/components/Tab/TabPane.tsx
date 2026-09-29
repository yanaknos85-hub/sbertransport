import React, { memo, FC } from 'react';
import cn from 'classnames';
import { Tabs } from 'antd';
import { TabPaneProps } from 'antd/lib/tabs';

import './styles.scss';

type Props = TabPaneProps & { themeSize?: 'xl' | 'l'; theme?: 'card' };

export const TabPane: FC<Props> = memo(({
  children, themeSize, theme, className, ...other
}) => (
  <Tabs.TabPane
    className={cn(className, { [`tab-pane_size-${themeSize}`]: themeSize, [`tab-pane_${theme}`]: theme })}
    {...other}
  >
    {children}
  </Tabs.TabPane>
));
