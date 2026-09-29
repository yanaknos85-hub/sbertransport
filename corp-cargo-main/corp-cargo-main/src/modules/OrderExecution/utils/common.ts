import classNames from 'classnames';
import styles from 'modules/Engineers/Engineer.module.scss';
import { DeadlineState } from 'modules/Engineers/constants/Engineers.constants';

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
