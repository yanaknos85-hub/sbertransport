import { useState } from 'react';
import { Moment } from 'moment';
import { AVAILABLE_CARGO_STATUSES } from '../../constants';
import { TRIP_STATUSES } from 'constants/trips.constants';

export interface DownloadFilters {
  statuses?: TRIP_STATUSES[];
  startTime?: [Moment, Moment] | undefined;
}

export const useValues = () => {
  const [formValues, setFormValues] = useState<DownloadFilters>({
    statuses: AVAILABLE_CARGO_STATUSES,
  });

  const setValues = (values: DownloadFilters) => setFormValues({ ...formValues, ...values });

  return { formValues, setValues };
};
