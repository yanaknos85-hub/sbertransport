import classNames from 'classnames';
import styles from 'modules/Engineers/Engineer.module.scss';
import { DeadlineState } from 'modules/Engineers/constants/Engineers.constants';
import { GroupTransferChildSeatDetails } from 'stores/Registry/Registry.interface';
import { VALUE_NOT_FOUND } from '../constants/General';
import { groupTransferChildSeatsLabels } from '../constants/General';
import { Waypoint } from 'stores/Geo/Geo.interface';

export const getColorRow = (deadlineState: DeadlineState | null | undefined): string => {
  const className = {
    YELLOW: classNames(styles.tableRow, styles.rowDistance),
    RED: classNames(styles.tableRow, styles.rowDeadline),
    NONE: classNames(styles.tableRow),
  };

  return deadlineState ? className[deadlineState] : className.NONE;
};

export const tableScrollConfiguration = {
  scrollToFirstRowOnChange: false, y: 630, x: 20,
};

export const getGroupTransferChildSeatsString = (seats: GroupTransferChildSeatDetails | undefined): string => {
  if (!seats) return VALUE_NOT_FOUND;
  const seatsStrings = Object.keys(seats)
    .map(seatType => {
      const key = seatType as keyof GroupTransferChildSeatDetails;
      return seats[key] ? `${groupTransferChildSeatsLabels[key]} - ${seats[key]} шт.` : '';
    })
    .filter(Boolean);
  if (!seatsStrings.length) return VALUE_NOT_FOUND;
  return seatsStrings.join(', ');
};

export const getAddressString = (waypoint: Waypoint | undefined) => {
  if (!waypoint) return VALUE_NOT_FOUND;
  const {
    city, street, house,
  } = waypoint;
  const addressString = `${city ? `${city}, ` : ''}${street ? `${street}, ` : ''}${house || ''}`;
  return addressString || VALUE_NOT_FOUND;
};

export const getIntermediateAddressString = (waypoints: Waypoint[] | undefined) => {
  if (!waypoints || waypoints.length < 3) return VALUE_NOT_FOUND;
  const adressSttrings: string[] = [];
  for (let i = 1; i < waypoints.length - 1; i += 1) {
    adressSttrings.push(getAddressString(waypoints[i]));
  }
  return adressSttrings.length ? adressSttrings.join(',') : VALUE_NOT_FOUND;
};

export const formatNumber = (value: number | undefined) => {
  if (!value) return VALUE_NOT_FOUND;
  return new Intl.NumberFormat('ru-RU', { maximumFractionDigits: 2 }).format(value);
};
