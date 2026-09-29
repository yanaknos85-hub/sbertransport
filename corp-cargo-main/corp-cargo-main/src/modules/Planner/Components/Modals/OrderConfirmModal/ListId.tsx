import React, { FC } from 'react';
import { OrderListMinimalType } from '../../../types';
import { ListItem } from './ListItem';

interface Props {
  list: OrderListMinimalType[];
}

export const ListId: FC<Props> = props => {
  const { list } = props;

  return (
    <>
      {list.map((item, idx) => (
        <ListItem
          key={item.humanReadableId}
          position={idx}
          value={item.humanReadableId}
        />
      ))}
    </>
  );
};
