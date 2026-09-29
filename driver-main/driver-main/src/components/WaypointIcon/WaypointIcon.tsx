import { FC, HTMLProps } from 'react';
import cn from 'classnames';
import { ALPHABET } from 'constants/app.constants';
import { CheckinType } from 'constants/trips.constants';
import styles from './WaypointIcon.module.scss';

interface WaypointIconProps extends HTMLProps<HTMLDivElement> {
  index?: number;
  checkinType?: CheckinType;
}

const WaypointIcon: FC<WaypointIconProps> = ({
  className,
  index,
  checkinType,
}) => (
  <div
    className={cn(styles.waypoint, className, styles[checkinType ?? 'NO_CHECKIN'])}
  >
    {index !== undefined && <div>{ALPHABET[index]}</div>}
  </div>
);

export default WaypointIcon;
