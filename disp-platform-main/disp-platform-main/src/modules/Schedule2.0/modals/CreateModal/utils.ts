import { FormInstance } from 'antd/lib/form';
import moment, { Moment } from 'moment';

import { ShiftsCreate, ShiftsCreateRequest } from 'api/schedule2.0/schedule.types';

import { UUID } from 'utils/io-ts';

import { PeriodicityTypes } from '../../constants/schedule.constants';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface FormValues extends Record<any, any> {
  duration: Moment;
  startDate: Moment;
  startTime: Moment;
  endDate?: Moment;
  shiftEndDate: Moment;
  vehicleId: UUID;
  driverId: UUID;
}

interface Props {
  formValues: FormValues;
  isRepeat: boolean;
  routeId?: string;
}

export const buildShifts = ({
  formValues,
  isRepeat,
  routeId,
}: Props) => {
  const shifts: ShiftsCreateRequest = [];

  const { driverId, vehicleId } = formValues;

  if (!isRepeat) {
    return [{
      driverId,
      vehicleId,
      index: 0,
      startDate: formValues.startDate.clone().utc().startOf('minute').format(),
      endDate: formValues.shiftEndDate.clone().utc().endOf('minute').format(),
      routeId,
    }];
  }

  const durationHours = formValues.duration.hours();
  const durationMin = formValues.duration.minutes();

  const startDate = moment(`${formValues.startDate.format('YYYY-MM-DD')}T${formValues.startTime.format('HH:mm')}`).utc();
  const endDate = formValues.endDate?.endOf('day').utc();

  let index = 0;

  do {
    if (formValues.periodicity === PeriodicityTypes.Weekly) {
      // График "Еженедельно"
      if (formValues.daysOfWeek?.[startDate.clone().local().weekday()] ?? true) {
        shifts.push({
          driverId,
          vehicleId,
          index: index++,
          startDate: startDate.format(),
          endDate: startDate.clone().add(durationHours, 'h').add(durationMin, 'm').format(),
        });
      }
    } else if (formValues.periodicity === PeriodicityTypes.Flexible) {
      // "Плавающий график"
      for (let i = 0; i < formValues.workDaysLength; i++) {
        if (startDate.isBefore(endDate)) {
          shifts.push({
            driverId,
            vehicleId,
            index: index++,
            startDate: startDate.format(),
            endDate: startDate.clone().add(durationHours, 'h').add(durationMin, 'm').format(),
          } as ShiftsCreate);
        }

        startDate.add(1, 'd');
      }
    }

    const addDays = formValues.periodicity === PeriodicityTypes.Flexible ? formValues.dayOffLength : 1;
    startDate.add(addDays, 'd');
  } while (startDate.isBefore(endDate));

  return shifts;
};

export const disableTime = (date: Moment | null, isRepeat: boolean, form: FormInstance) => {
  if (!date) return {};

  const disabledHours: number[] = [];
  const disabledMinutes: number[] = [];

  const isStartDate = date.clone().format('YYYY-MM-DD') === form.getFieldValue('startDate').format('YYYY-MM-DD');
  const startTime = isRepeat
    ? moment(`${form.getFieldValue('startDate').format('YYYY-MM-DD')}T${form.getFieldValue('startTime').format('HH:mm')}`)
    : form.getFieldValue('startDate');

  if (isStartDate) {
    for (let i = 0; i < startTime.hours(); i++) {
      disabledHours.push(i);
    }

    if (date.hours() === startTime.hours()) {
      for (let i = 0; i < startTime.minutes(); i++) {
        disabledMinutes.push(i);
      }
    }
  }

  const durationHours = form.getFieldValue('duration')?.hours();
  const durationMinutes = form.getFieldValue('duration')?.minutes();

  const endDate = isRepeat
    ? startTime.clone().add(durationHours, 'h').add(durationMinutes, 'm')
    : form.getFieldValue('shiftEndDate');
  const isEndDate = date.clone().format('YYYY-MM-DD') === endDate.format('YYYY-MM-DD');

  if (isEndDate) {
    for (let i = endDate.hours() + 1; i < 24; i++) {
      disabledHours.push(i);
    }

    if (date.hours() === endDate.hours()) {
      for (let i = endDate.minutes() + 1; i < 60; i++) {
        disabledMinutes.push(i);
      }
    }
  }

  return {
    disabledHours: () => disabledHours,
    disabledMinutes: () => disabledMinutes,
  };
};
