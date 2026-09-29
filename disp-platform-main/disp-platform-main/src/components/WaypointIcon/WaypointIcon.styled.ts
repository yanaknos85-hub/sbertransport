import styled from 'styled-components';

import { colorMap } from 'constants/waypoint.constants';
import { WaypointType } from 'types/waypoint';

export const Waypoint = styled.div<{ type: WaypointType }>`
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 18px;
  height: 18px;
  font-size: 10px;
  color: white;
  background: ${({ type }) => colorMap[type]};
`;
