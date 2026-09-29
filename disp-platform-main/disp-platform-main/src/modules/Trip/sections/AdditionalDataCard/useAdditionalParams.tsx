import React, { useMemo } from 'react';
import { PassTrip } from 'api/trips/trips.types';
import { EMPTY_CELL_CONTENT } from 'constants/app.constants';
import { ChildSeatTitles, ChildSeatTypes } from 'constants/trips.constants';

export const useAdditionalParams = (trip: PassTrip) => {
  const data = useMemo(() => {
    const childSeatDetails = trip.requests[0]?.information?.childSeatDetails
      ?? trip.information?.childSeatDetails ?? {};

    return [
      {
        title: 'Детское кресло',
        desc: (trip.requests[0]?.information?.childSeat || trip.information?.childSeat)
          ? Object
            .entries(childSeatDetails)
            .filter(([_, quantity]) => quantity)
            .map(([seatType, quantity]) => (
              <div key={seatType}>
                {ChildSeatTitles[seatType as ChildSeatTypes] ?? seatType}
                <span> - </span>
                {quantity}
                {' '}
                шт.
              </div>
            ))
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Негабаритный багаж',
        desc: trip.requests[0]?.information?.bugsOversized || trip.information?.bugsOversized
          ? (trip.requests[0]?.information?.bugsOversizedComment
          ?? trip.information?.bugsOversizedComment)
          : EMPTY_CELL_CONTENT,
      },
      {
        title: 'Желаемый тип ТС',
        desc: trip.requests[0]?.information?.typeVehicle
        ?? trip.information?.typeVehicle ?? EMPTY_CELL_CONTENT,
      },
      {
        title: 'Перевозка животного',
        desc: (trip.requests[0]?.information?.animal || trip.information?.animal)
          ? (trip.requests[0]?.information?.animalComment
          ?? trip.information?.animalComment)
          : EMPTY_CELL_CONTENT,
      },
    ];
  }, [trip]);

  return { data };
};
