import * as React from 'react';
import { declOfNum } from 'utils';
import * as t from 'io-ts';

import {
  AddressStyled,
  BulletStyled,
  LabelStyled,
  ValueStyled,
  WaypointStyled,
  WaypointsStyled
} from './Waypoints.style';

export const IOWaypoint = t.intersection([
  t.type({}),
  t.partial({
    latitude: t.number,
    longitude: t.number,
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    waitTime: t.number,
    checkinAutomatic: t.boolean,
    checkinManual: t.boolean,
    absenceReason: t.string,
    icon: t.string,
    district: t.string,
  }),
]);

export type TWaypoint = t.TypeOf<typeof IOWaypoint>;

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
          data={waypoint}
          hideLabels={Boolean(hideLabels)}
          hideLinkOpen={Boolean(hideLinkOpen)}
          hidden={!open && count > 2 && i > 0 && i < count - 1}
          open={open}
          first={i === 0}
          last={i === count - 1}
          lastButOne={count > 2 && i === count - 2}
          totalCount={count}
          onOppened={() => setOppened(!open)}
          child={waypointsChilds[i]}
        />
      ))}
    </WaypointsStyled>
  );
};

export interface WaypointProps {
  data: TWaypoint;
  index: number;
  hideLabels: boolean;
  hideLinkOpen: boolean;
  hidden: boolean;
  first: boolean;
  last: boolean;
  lastButOne: boolean;
  totalCount: number;
  open: boolean;
  onOppened: () => void;
  child: JSX.Element;
}

const Waypoint: React.FC<WaypointProps> = (props: WaypointProps) => {
  const {
    data, index, hideLinkOpen, hideLabels, hidden, first, last, lastButOne, totalCount, open, onOppened, child,
  }
    = props;
  const {
    street, house, city,
  } = data;

  const address = [street, house, city].filter(Boolean).join(', ');
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
          <ValueStyled link={!hideLinkOpen} onClick={() => !hideLinkOpen && onOppened()}>
            {addressLink}
          </ValueStyled>
        ) : (
          <>
            <ValueStyled>{address}</ValueStyled>
            {child}
          </>
        )}
      </AddressStyled>
    </WaypointStyled>
  );
};

export default Waypoints;
