import React, { FC, HTMLProps } from 'react';

import { ALPHABET } from 'constants/app.constants';
import { WaypointType } from 'types/waypoint';
import { Waypoint } from './WaypointIcon.styled';

interface WaypointIconProps extends HTMLProps<HTMLDivElement> {
  index: number;
  type: WaypointType;
}

const WaypointIcon: FC<WaypointIconProps> = ({ index, type }) => (
  <Waypoint type={type}>
    <div>{ALPHABET[index]}</div>
  </Waypoint>
);

export default WaypointIcon;
