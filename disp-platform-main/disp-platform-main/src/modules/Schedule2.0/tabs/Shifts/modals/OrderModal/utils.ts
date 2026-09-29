import moment, { Moment } from 'moment';

export const disableStartShiftOrderTime = (date: Moment | null, orderStartDate?: string) => {
  if (!date) return {};

  const disabledHours: number[] = [];
  const disabledMinutes: number[] = [];

  const startDate = moment(orderStartDate);

  const isStartDate = date.clone().format('YYYY-MM-DD') === startDate.format('YYYY-MM-DD');

  if (isStartDate) {
    for (let i = startDate.hours() + 1; i < 24; i++) {
      disabledHours.push(i);
    }

    if (date.hours() === startDate.hours()) {
      for (let i = startDate.minutes() + 1; i < 60; i++) {
        disabledMinutes.push(i);
      }
    }
  }

  return {
    disabledHours: () => disabledHours,
    disabledMinutes: () => disabledMinutes,
  };
};

export const disableEndShiftOrderTime = (date: Moment | null, orderEndDate?: string) => {
  if (!date) return {};

  const disabledHours: number[] = [];
  const disabledMinutes: number[] = [];

  const endDate = moment(orderEndDate);

  const isEndDate = date.clone().format('YYYY-MM-DD') === endDate.format('YYYY-MM-DD');

  if (isEndDate) {
    for (let i = 0; i < endDate.hours(); i++) {
      disabledHours.push(i);
    }

    if (date.hours() === endDate.hours()) {
      for (let i = 0; i < endDate.minutes(); i++) {
        disabledMinutes.push(i);
      }
    }
  }

  return {
    disabledHours: () => disabledHours,
    disabledMinutes: () => disabledMinutes,
  };
};
