import * as React from 'react';
import { FC } from 'react';

import { declOfNum } from 'utils/declOfNum';

import { TWaypoint } from '../../../models/geo/types';
import {
  AddressStyled,
  BulletStyled,
  LabelStyled,
  ValueStyled,
  WaypointsStyled,
  WaypointStyled
} from './Waypoints.style';

export interface WaypointsProps {
  /** Масссив вейпойнтов */
  waypoints: TWaypoint[];
  /** Развернуть вейпойнты */
  open?: boolean;
  /** Скрывать ссылку Открыть на несолько адресов */
  hideLinkOpen?: boolean;
  /** Скрывать Откуда/Куда */
  hideLabels?: boolean;
  /** Массив JSX елеметов, если нужно добавить произвольный контент под каждый адрес */
  waypointsChilds?: JSX.Element[];
}

const Waypoints: React.FC<WaypointsProps> = (props: WaypointsProps) => {
  const {
    waypoints, open: isOpen, hideLabels, hideLinkOpen, waypointsChilds = [],
  } = props;
  const [open, setOppened] = React.useState(Boolean(isOpen));
  const count = waypoints.length;

  if (count < 2) {
    return null;
  }

  return (
    <WaypointsStyled open={open}>
      {waypoints.map((waypoint, i) => (
        <Waypoint
          // eslint-disable-next-line react/no-array-index-key
          key={i}
          index={i}
          waypoint={waypoint}
          hideLabels={Boolean(hideLabels)}
          hideLinkOpen={Boolean(hideLinkOpen)}
          hidden={!open && count > 2 && i > 0 && i < count - 1}
          open={open}
          first={i === 0}
          last={i === count - 1}
          lastButOne={count > 2 && i === count - 2}
          totalCount={count}
          onOpened={() => setOppened(!open)}
          child={waypointsChilds[i]}
        />
      ))}
    </WaypointsStyled>
  );
};

export interface WaypointProps {
  waypoint: TWaypoint;
  index: number;
  hideLabels: boolean;
  hideLinkOpen: boolean;
  hidden: boolean;
  first: boolean;
  last: boolean;
  lastButOne: boolean;
  totalCount: number;
  open: boolean;
  onOpened: () => void;
  child: JSX.Element;
}

const Waypoint: FC<WaypointProps> = props => {
  const {
    waypoint,
    index,
    hideLinkOpen,
    hideLabels,
    hidden,
    first,
    last,
    lastButOne,
    totalCount,
    open,
    onOpened,
    child,
  } = props;

  const addressLink = `${totalCount} ${declOfNum(totalCount, ['адрес', 'адреса', 'адресов'])}`;

  return (
    <WaypointStyled
      open={open}
      hidden={hidden}
      first={first}
      last={last}
      lastButOne={lastButOne}
    >
      <BulletStyled
        open={open}
        hideLabels={hideLabels}
        first={first}
        last={last}
      >
        {(open || first || totalCount === 2) && String.fromCharCode(97 + index).toUpperCase()}
      </BulletStyled>
      <AddressStyled>
        {!hideLabels && (
          <LabelStyled>
            {first && 'Откуда'}
            {!open && last && 'Куда'}
          </LabelStyled>
        )}
        {!open && last && totalCount > 2 ? (
          <ValueStyled link={!hideLinkOpen} onClick={() => !hideLinkOpen && onOpened()}>
            {addressLink}
          </ValueStyled>
        ) : (
          <>
            <ValueStyled>{waypoint.addressStringRepresentation}</ValueStyled>
            {child}
          </>
        )}
      </AddressStyled>
    </WaypointStyled>
  );
};

export default Waypoints;
