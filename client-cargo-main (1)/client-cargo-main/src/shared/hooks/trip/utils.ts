import { RouteModel } from 'shared/models/geo/Route.model';

import { TripRequestModel } from 'stores/Trip/models';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';
import { taxiClassIsClassic } from 'utils/trips';

export const prepareRequestTripData = ({
  request,
  cost,
  expected,
}: {
  request: TripRequestModel;
  cost: number | undefined;
  expected: RouteModel | undefined;
}): Partial<TripRequestModel> => {
  const { taxiClass } = request;
  // Данные, которые нужно отправить на бэк,
  // для обновления заявки
  const targetKeys = [
    'humanReadableId',
    'id',
    'author',
    'desiredDate',
    'passenger',
    'taxiClass',
    'passengerCount',
    'tariffId',
    'expected',
    'purpose',
    'coopTrip',
    'commentForDriver',
    'approvedBy',
    'transportType',
  ];

  // За основу взят useCreateTripRequest
  const requestObject: Record<string, any> = targetKeys.reduce(
    (acc, key) => ({
      ...acc,
      [key]: request[key as keyof TripRequestModel],
    }),
    {}
  );

  if (!taxiClassIsClassic(taxiClass as TaxiClassEnum)) {
    delete requestObject.taxiClass;
  }

  return { ...requestObject, expected: { ...expected, cost } as RouteModel };
};
