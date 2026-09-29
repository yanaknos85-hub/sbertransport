import React, { FC } from 'react';
import cn from 'classnames';

import Flex from 'components/Flex/Flex';

import { ReactComponent as Car } from 'assets/icons/map-car.svg';
import { ReactComponent as CarGreen } from 'assets/icons/map-car-green.svg';

import styles from './index.module.scss';
import { BusynessFilters } from './Map.constants';

const filters = [
  {
    id: BusynessFilters.Free, title: 'Свободен', icon: CarGreen,
  },
  {
    id: BusynessFilters.Busy, title: 'Занят', icon: Car,
  },
];

interface BusynessFilterProps {
  value: BusynessFilters | undefined;
  onChange: React.Dispatch<React.SetStateAction<BusynessFilters | undefined>>;
}

export const BusynessFilter: FC<BusynessFilterProps> = ({
  value,
  onChange,
}) => {
  const toggle = (val: BusynessFilters) => onChange(prev => !!prev && prev === val ? undefined : val);

  return (
    <Flex gap={5}>
      {filters.map(({
        id, title, icon: Icon,
      }) => (
        <div
          key={id}
          className={cn(styles.busynessFilter, { [styles.active]: id === value })}
          onClick={() => toggle(id)}
        >
          <Icon />
          {title}
        </div>
      ))}
    </Flex>
  );
};
