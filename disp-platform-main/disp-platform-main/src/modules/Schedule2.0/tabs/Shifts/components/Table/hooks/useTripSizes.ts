import { useCallback } from 'react';

import moment from 'moment';

import { CELL_PADDING, ONE_HOUR } from '../../../../../constants/schedule.constants';
import { useTableZoom } from '../../../context/tableZoom.context';

export const useEventSize = () => {
  const { cellWidth } = useTableZoom();

  const getEventSizes = useCallback((startDate: string, endDate: string, time: moment.Moment) => {
    const tripWidth = moment(endDate)
      .diff(moment(startDate)) / ONE_HOUR * (cellWidth + CELL_PADDING);
    const tripOffset = moment(startDate)
      .diff(time) / ONE_HOUR * (cellWidth + CELL_PADDING) - 1;

    return { tripWidth, tripOffset };
  }, [cellWidth]);

  return getEventSizes;
};
