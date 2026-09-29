import { useState } from 'react';
import { Moment } from 'moment';
import { TRIP_STATUSES, finalTripStatuses } from 'constants/trips.constants';

export interface DownloadFilters {
  statuses?: TRIP_STATUSES[];
  startTime?: [Moment, Moment] | undefined;
}

export const useValues = () => {
  const [formValues, setFormValues] = useState<DownloadFilters>({
    statuses: finalTripStatuses,
  });

  const setValues = (values: DownloadFilters) => setFormValues({ ...formValues, ...values });

  return { formValues, setValues };
};
