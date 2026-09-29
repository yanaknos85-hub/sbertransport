import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { CargoListItem } from 'types/Cargo';

import CargoItem from './CargoItem';
import { List } from './CargoList.style';

interface Props {
  list: CargoListItem[] | undefined;
}

const CargoList: FC<Props> = observer(({ list }) => {
  if (!list?.length) {
    return null;
  }

  return (
    <List>
      {list?.map(data => (
        <CargoItem key={data.position} data={data} />
      ))}
    </List>
  );
});

export default CargoList;
