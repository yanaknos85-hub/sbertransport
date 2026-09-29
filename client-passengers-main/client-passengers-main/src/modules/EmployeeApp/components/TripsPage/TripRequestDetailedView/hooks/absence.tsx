import { AbsenceRequestData, useAbsenceReason } from 'api/check-in';

import { TripRequestModel } from 'stores/Trip/models';

import { WaypointField } from 'shared/models/geo/types';

export const useFormAbsenceReason = ({
  request,
  waypointFields,
}: {
  request: TripRequestModel | undefined;
  waypointFields: WaypointField[];
}): {
    sendAbsenceReasons: (values: Record<string, string>) => Promise<unknown[]>;
  } => {
  const [absenceReason] = useAbsenceReason();

  /**
   * Подготавливает значения формы для отправки
   * на сервер причин отсутствия на точке (absenceReason)
   */
  const prepareRequestData = (values: Record<string, string>): AbsenceRequestData[] => Object.keys(values).map(fieldName => {
    const waypointField = waypointFields.find(field => field.fieldName === fieldName);

    return {
      requestId: request?.id || '',
      latitude: waypointField?.latitude,
      longitude: waypointField?.longitude,
      absenceReason: values[fieldName],
      orderingIndex: 1,
    };
  });

  const sendAbsenceReasons = (values: Record<string, string>): Promise<unknown[]> => {
    const data = prepareRequestData(values);
    return Promise.all(data.map(requestData => absenceReason(requestData)));
  };

  return { sendAbsenceReasons };
};
