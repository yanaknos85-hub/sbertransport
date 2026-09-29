import React from 'react';
import { PassTrip } from 'api/trips/trips.types';
import { DATE_FORMAT, EMPTY_CELL_CONTENT } from 'constants/app.constants';
import moment from 'moment';

export const useSubtitle = (trip: PassTrip) => {
  const mainRequest = trip.requests.find(x => x.sharedRideOwner) ?? trip.requests[0];

  const isSameTimeZone = moment.parseZone(trip.expectedStartTime).utcOffset() === moment().utcOffset();

  return isSameTimeZone || !mainRequest?.creationTime
    ? `Дата создания заявки инициатором: ${mainRequest?.creationTime
      ? moment(mainRequest.creationTime).format(DATE_FORMAT.DATE_WITH_TIME)
      : EMPTY_CELL_CONTENT}`
    : (
      <>
        <div>
          {`Дата создания заявки инициатором (клиент): ${moment.parseZone(mainRequest.creationTime)
            .format(DATE_FORMAT.DATE_WITH_TIME)}`}
        </div>
        <div>
          {`Дата создания заявки инициатором (диспетчер): ${moment(mainRequest.creationTime)
            .format(DATE_FORMAT.DATE_WITH_TIME)}`}
        </div>
      </>
    );
};
