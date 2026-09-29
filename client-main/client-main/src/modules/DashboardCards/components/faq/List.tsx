import React from 'react';
import type { FC } from 'react';
import { Collapse } from 'antd';

import uuid from 'utils/uuid';
import type { ICollapseItem } from 'modules/DashboardCards/types/faq.types';
import { WrapperList } from './styled';
import { ReactComponent as ArrowUp } from 'shared/components/Images/ArrowUpDefault.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/ArrowDownDefault.svg';

interface Props {
  items: ICollapseItem[];
}

const List: FC<Props> = ({ items }) => (
  <WrapperList>
    <Collapse expandIcon={({ isActive }) => isActive ? <ArrowUp /> : <ArrowDown />}>
      {items.map(({ label, children }) => (
        <Collapse.Panel key={uuid()} header={label}>{children}</Collapse.Panel>
      ))}
    </Collapse>
  </WrapperList>
);

export default List;
