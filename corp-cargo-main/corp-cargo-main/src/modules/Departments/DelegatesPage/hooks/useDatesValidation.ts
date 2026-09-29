import { useState } from 'react';

import moment from 'moment/moment';
import { FormInstance } from 'antd/es/form';

import { Delegates, DelegatesCyrillic } from '../constants/Delegates.constants';

export const useDatesValidation = (
  form: FormInstance
): {
    datesCorrectionObject: {
      start: boolean;
      end: boolean;
    };
    checkStartDateCorrection: (date: moment.Moment) => void;
    checkEndDateCorrection: (date: moment.Moment) => void;
    checkDelegatesEndDate: (rule: boolean, value: moment.Moment) => Promise<void>;
    checkDelegatesStartDate: (rule: boolean, value: moment.Moment) => Promise<void>;
  } => {
  const initialDatesCorrection = {
    start: true,
    end: true,
  };

  const [datesCorrectionObject, setDatesCorrectionObject] = useState<{
    start: boolean;
    end: boolean;
  }>(initialDatesCorrection);

  const startDateFieldValue = form.getFieldValue(Delegates.startDate);
  const endDateFieldValue = form.getFieldValue(Delegates.endDate);

  const checkEndDateCorrection = (date: moment.Moment): void => {
    if (startDateFieldValue) {
      setDatesCorrectionObject({
        start: datesCorrectionObject.start,
        end: date.isAfter(moment(form.getFieldValue(Delegates.startDate))),
      });
    }
  };

  const checkStartDateCorrection = (date: moment.Moment): void => {
    if (endDateFieldValue) {
      setDatesCorrectionObject({
        start: date.isBefore(moment(form.getFieldValue(Delegates.endDate))),
        end: datesCorrectionObject.end,
      });
    }
  };

  // eslint-disable-next-line @stylistic/max-len
  const checkDelegatesEndDate = (rule: boolean, value: moment.Moment): Promise<void> => !value || rule ? Promise.resolve() : Promise.reject(new Error(DelegatesCyrillic[Delegates.endDateIncorrect]));

  // eslint-disable-next-line @stylistic/max-len
  const checkDelegatesStartDate = (rule: boolean, value: moment.Moment): Promise<void> => !value || rule ? Promise.resolve() : Promise.reject(new Error(DelegatesCyrillic[Delegates.startDateIncorrect]));

  return {
    datesCorrectionObject,
    checkStartDateCorrection,
    checkEndDateCorrection,
    checkDelegatesEndDate,
    checkDelegatesStartDate,
  };
};
