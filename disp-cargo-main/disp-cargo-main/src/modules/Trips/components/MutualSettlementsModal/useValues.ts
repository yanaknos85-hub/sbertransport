import { useState } from 'react';
import { Moment } from 'moment';

export interface DownloadFilters {
  startTime?: [Moment, Moment] | undefined;
}

export const useValues = () => {
  const [formValues, setFormValues] = useState<DownloadFilters>({});

  const setValues = (values: DownloadFilters) => setFormValues({ ...formValues, ...values });

  return { formValues, setValues };
};
