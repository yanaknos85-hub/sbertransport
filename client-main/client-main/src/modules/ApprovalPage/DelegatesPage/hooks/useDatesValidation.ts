import { useState } from 'react';
import moment from 'moment/moment';

import { Delegates, DelegatesCyrillic } from '../Delegates.constants';

export const useDatesValidation = (): {
  datesCorrectionObject: {
    start: boolean;
    end: boolean;
  };
  checkStartDateCorrection: (date: moment.Moment, endDate: moment.Moment) => void;
  checkEndDateCorrection: (date: moment.Moment, startDate: moment.Moment) => void;
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

  const checkEndDateCorrection = (date: moment.Moment, startDate?: moment.Moment): void => {
    if (startDate) {
      setDatesCorrectionObject({
        start: !initialDatesCorrection.start ? startDate.isBefore(moment(date)) : initialDatesCorrection.start,
        end: date.isAfter(moment(startDate)),
      });
    }
  };

  const checkStartDateCorrection = (date: moment.Moment, endDate?: moment.Moment): void => {
    if (endDate) {
      setDatesCorrectionObject({
        start: date.isBefore(moment(endDate)),
        end: !initialDatesCorrection.end ? endDate.isAfter(moment(date)) : initialDatesCorrection.end,
      });
    }
  };

  const checkDelegatesEndDate = (rule: boolean, value: moment.Moment): Promise<void> => (
    !value || rule ? Promise.resolve() : Promise.reject(new Error(DelegatesCyrillic[Delegates.endDateIncorrect]))
  );

  const checkDelegatesStartDate = (rule: boolean, value: moment.Moment): Promise<void> => (
    !value || rule ? Promise.resolve() : Promise.reject(new Error(DelegatesCyrillic[Delegates.startDateIncorrect]))
  );

  return {
    datesCorrectionObject,
    checkStartDateCorrection,
    checkEndDateCorrection,
    checkDelegatesEndDate,
    checkDelegatesStartDate,
  };
};
