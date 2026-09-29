import React, { FC } from 'react';
import { colors } from 'shared/styles/styles';

interface ListItemProps {
  position: number;
  value: string;
}

export const ListItem: FC<ListItemProps> = ({ position, value }) => (
  <li>
    <span style={{ color: colors.gray6 }}>{position + 1}</span>
    .
    {' '}
    <a href="#">
      <span style={{ color: colors.solidBodyNormal }}>{value}</span>
    </a>
  </li>
);
