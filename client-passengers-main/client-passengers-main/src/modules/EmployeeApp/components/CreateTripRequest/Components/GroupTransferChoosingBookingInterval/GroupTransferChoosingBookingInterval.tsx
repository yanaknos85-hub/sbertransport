import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';
import { FormInstance } from 'antd/es/form/Form';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { DatePicker, TimePicker } from 'antd';
import moment, { Moment } from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';

import { TimeSlot } from './components/timeSlot/TimeSlot';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { getTimeZone } from '../../utils/utils';
import { IContractorDispatcherTransport } from 'stores/Trip/Trip.interface';
import { formatRubles } from 'utils';
import TButton from 'shared/ui/Button/Button';

import './override.scss';
import styles from './groupTransferChoosingBookingInterval.module.scss';

interface GroupTransferChoosingBookingInterval {
  groupTransferForm: FormInstance;
  setVisibleGroupTransferChoosingBookingInterval: React.Dispatch<React.SetStateAction<boolean>>;
  setVisiblevisibleGroupTransferForm: React.Dispatch<React.SetStateAction<boolean>>;
}

export const GroupTransferChoosingBookingInterval: FC<GroupTransferChoosingBookingInterval> = observer(({
  groupTransferForm, setVisibleGroupTransferChoosingBookingInterval, setVisiblevisibleGroupTransferForm,
}) => {
  const {
    [StoreNames.tripStore]: tripStore,
    [StoreNames.geoStore]: geo,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const TIME_INTERVAL = 30;
  const date = groupTransferForm.getFieldValue('desiredDateGroupTransfer');
  const route = geo.calculatedRoute;
  const waitTime = geo.calculatedRoute?.waypoints.reduce((acc, value) => acc + value.waitTime, 0);
  const durationInMillis = waitTime !== undefined && route && (route.time + waitTime);
  const [transport, setTransport] = useState<IContractorDispatcherTransport>();

  const generateTimeSlots = () => {
    const slots: string[] = [];
    for (let h = 0; h < 24; h++) {
      for (let m = 0; m < 60; m += TIME_INTERVAL) {
        slots.push(`${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`);
      }
    }
    return slots;
  };

  const timeSlots = generateTimeSlots();
  const [selectedRange, setSelectedRange] = useState({ start: null, end: null });
  const [previewEnd, setPreviewEnd] = useState(null);
  const [validationError, setValidationError] = useState('');
  const [defaultRange, setDefaultRange] = useState<any>([]);
  const [rangePicker, setRangePicker] = useState<any>([]);
  const [dateBooking, setDateBooking] = useState<Moment>();

  const getContractorDispatcherTransport = () => {
    if (route) {
      const [startTime, endTime] = rangePicker;

      const difference = endTime && endTime.diff(startTime);

      const data = {
        distance: route.distance,
        time: difference ?? route.time,
        tripDate: (dateBooking ? dateBooking?.valueOf() : date.valueOf()),
        startPoint: { ...route.waypoints[0] },
        organizationId: selfStore.orgId,
        employeeId: selfStore.empId,
        waitingTime: route.waypoints[0].waitTime,
        timeZone: getTimeZone(),
        waypoints: route.waypoints,
      };

      const searchParams = {
        startDate: dateBooking ? dateBooking?.startOf('day').valueOf() : moment(date).startOf('day').valueOf(),
        endDate: dateBooking ? dateBooking?.endOf('day').valueOf() : moment(date).endOf('day').valueOf(),
      };
      const transportId = tripStore.availableDispatcherTransport?.id;

      if (transportId) {
        tripStore.getContractorDispatcherTransport(transportId, data, searchParams)
          .then(transport => {
            setTransport(transport);
            tripStore.setAvailableChoosingBookingTransport(transport);
          });
      }
    }
  };

  useEffect(() => {
    if ((!rangePicker.length && !validationError) || rangePicker.length > 1) {
      getContractorDispatcherTransport();
    }
  }, [rangePicker]);

  const roundToNearest30Minutes = (time, timeEnd = false, disabledTime = false) => {
    const minutes = time.minutes();

    if (minutes < 30) {
      if (timeEnd && minutes > 0) {
        if (disabledTime) {
          return time.minutes(30).add(30, 'minutes');
        }
        return time.minutes(30);
      }
      return time.minutes(0);
    } else if (minutes > 30) {
      if (timeEnd) {
        if (disabledTime) {
          return time.minutes(60).add(30, 'minutes');
        }
        return time.minutes(30).add(30, 'minutes');
      }

      return time.minutes(30);
    }

    return time;
  };

  // Проверка, забронирован ли слот с учетом перехода на другой день
  const isBooked = time => {
    const roundedTime = moment(time, 'HH:mm').set({
      year: dateBooking ? dateBooking.year() : date.year(),
      month: dateBooking ? dateBooking.month() : date.month(),
      date: dateBooking ? dateBooking.date() : date.date(),
    });

    const defaultTimeCutoff = roundToNearest30Minutes(moment(), true);

    if (roundedTime.isSame(moment(), 'day') && (roundedTime.isBefore(defaultTimeCutoff) || roundedTime.isSame(defaultTimeCutoff))) {
      return true;
    }

    return transport?.trips.some(({ start, end }) => {
      const startMoment = roundToNearest30Minutes(moment(start));
      const endMoment = roundToNearest30Minutes(moment(end), true);

      // Проверка на пересечение с забронированными слотами
      if (roundedTime.isBetween(startMoment, endMoment, null, '[]')) {
        return true;
      }

      // Проверка для случаев, когда слот идет через границу дня
      if (startMoment.isBefore(endMoment)) {
        return false;
      } else {
        return roundedTime.isSameOrAfter(startMoment) || roundedTime.isBefore(endMoment);
      }
    });
  };

  // Проверка на пересечение с забронированными слотами
  const rangeHasOverlap = (start, end) => {
    const selectedSlots = timeSlots.slice(
      timeSlots.indexOf(start),
      timeSlots.indexOf(end) + 1
    );

    return selectedSlots.some(slot => isBooked(slot));
  };

  // Начальный выбор слота
  const handleStartSelection = time => {
    if (isBooked(time)) return;

    const slotTime = moment(time, 'HH:mm').set({
      year: date.year(),
      month: date.month(),
      date: date.date(),
    });

    if (selectedRange.start === time) {
      setSelectedRange({ start: null, end: null });
      setPreviewEnd(null);
    } else {
      setSelectedRange({ start: time, end: null });
      setRangePicker([slotTime]);
      setPreviewEnd(null);
    }
  };

  // Округление миллисекунд до ближайшего 30-минутного интервала
  const roundMillisToNearest30Minutes = millis => {
    const minutes = Math.floor((millis / 1000 / 60) % 60);
    const roundedMinutes = minutes < 30 ? 0 : minutes > 30 ? 60 : 30;
    return Math.floor(millis / 1000 / 60 / 60) * 60 + roundedMinutes;
  };

  const requiredDurationMinutes = roundMillisToNearest30Minutes(durationInMillis) + 30;

  // Конечный выбор слота
  const handleEndSelection = time => {
    if (!selectedRange.start || time === selectedRange.start || isBooked(time)) return;
    const [start, end] = time > selectedRange.start ? [selectedRange.start, time] : [time, selectedRange.start];
    const startIdx = timeSlots.indexOf(start);
    const endIdx = timeSlots.indexOf(end);
    const durationMinutes = (endIdx - startIdx) * TIME_INTERVAL;

    const slotTimeStart = moment(start, 'HH:mm').set({
      year: date.year(),
      month: date.month(),
      date: date.date(),
    });

    const slotTimeEnd = moment(end, 'HH:mm').set({
      year: date.year(),
      month: date.month(),
      date: date.date(),
    });

    if (durationMinutes <= requiredDurationMinutes) {
      setSelectedRange({ start: null, end: null });
      setRangePicker([]);
      setPreviewEnd(null);
      setValidationError('Выбранный интервал не удовлетворяет условиям поездки');
      return;
    }

    if (rangeHasOverlap(start, end)) {
      setSelectedRange({ start: null, end: null });
      setRangePicker([]);
      setPreviewEnd(null);
      setValidationError('Выбранный интервал не должен пересекаться с занятыми слотами');
      return;
    }

    setSelectedRange({ start, end });
    setRangePicker([slotTimeStart, slotTimeEnd]);
    setPreviewEnd(null);
    setValidationError('');
  };

  // Обработчик наведения мыши для временного эффекта
  const handleMouseEnter = time => {
    if (!selectedRange.start || selectedRange.end || time === selectedRange.start || isBooked(time)) {
      setPreviewEnd(null);
      return;
    }
    setPreviewEnd(time);
  };

  // Проверка, входит ли слот в подтвержденный диапазон
  const isSelected = time => {
    if (!selectedRange.start || !selectedRange.end) return false;
    return (time > selectedRange.start && time < selectedRange.end) || (time < selectedRange.start && time > selectedRange.end);
  };

  // Проверка, входит ли слот в временный диапазон
  const isPreview = time => {
    if (!selectedRange.start || !previewEnd) return false;

    const [start, end] = previewEnd > selectedRange.start ? [selectedRange.start, previewEnd] : [previewEnd, selectedRange.start];
    return time > start && time < end;
  };

  useEffect(() => {
    if (transport) {
      const roundedStart = roundToNearest30Minutes(moment(rangePicker[0] ?? date)).format('HH:mm');
      const roundedEnd = durationInMillis && roundToNearest30Minutes(moment(rangePicker[1] ?? date, 'HH:mm').add(rangePicker[1] ? 0 : (durationInMillis + 1800000), 'milliseconds'), true).format('HH:mm');

      // Проверка, могут ли слоты быть выбраны по умолчанию
      if (!isBooked(roundedStart) && !isBooked(roundedEnd) && !rangeHasOverlap(roundedStart, roundedEnd)) {
        if (!rangePicker.length) {
          setSelectedRange({ start: roundedStart, end: roundedEnd });
          setDefaultRange(durationInMillis && [moment(dateBooking ?? date), moment(dateBooking ?? date, 'HH:mm').add(durationInMillis + 1800000, 'milliseconds')]);
        }
      } else {
        setRangePicker([]);
        setSelectedRange({ start: null, end: null });
        setValidationError('Выбранный интервал не должен пересекаться с занятыми слотами');
      }
    }
  }, [transport]);

  // Добавление RangePicker с дефолтным временем
  const handleRangePickerChange = range => {
    if (!range || range.length < 2) return;

    const [start, end] = range;

    if (start.isBefore(moment())) {
      setValidationError('Нельзя выбрать время в прошлом');
      setSelectedRange({ start: null, end: null });
      setRangePicker([]);
      return;
    }

    const startRounded = roundToNearest30Minutes(start);
    const endRounded = roundToNearest30Minutes(end, true);
    if (rangeHasOverlap(startRounded.format('HH:mm'), endRounded.format('HH:mm'))) {
      setValidationError('Выбранный интервал не должен пересекаться с занятыми слотами');
      setSelectedRange({ start: null, end: null });
      setRangePicker([]);
      return;
    }

    setValidationError('');
    setRangePicker(range);
    setSelectedRange({ start: startRounded.format('HH:mm'), end: endRounded.format('HH:mm') });
  };

  const disabledHours = () => {
    // Если выбранная дата — текущий день, возвращаем часы, которые уже прошли
    const time = moment(date);
    const momentTime = roundToNearest30Minutes(moment(), true, true);

    if (time.isSame(moment(momentTime), 'day')) {
      const currentHour = momentTime.hour();

      return Array.from({ length: 24 }, (_, i) => i).filter(hour => hour < currentHour);
    }

    return []; // Если это другой день, часы не блокируются
  };

  const disabledMinutes = selectedHour => {
    const time = moment(date);
    const momentTime = roundToNearest30Minutes(moment(), true, true);

    if (time.isSame(moment(momentTime), 'day')) {
      const currentHour = momentTime.hour();

      const currentMinute = momentTime.minute();

      return selectedHour === currentHour
        ? Array.from({ length: 60 }, (_, i) => i).filter(minute => minute < currentMinute)
        : [];
    }

    return [];
  };

  const getDisabledDate = (value: moment.Moment): boolean => {
    const now = moment().add(-1, 'days');
    return value < now || value > now.add(1, 'years');
  };

  const onDateChange = (dateChange: moment.Moment | null): void => {
    const date = dateChange;

    if (date) {
      if ((rangePicker || defaultRange) && (rangePicker[1] || defaultRange[1])) {
        const slotTimeStart = moment(rangePicker[0] ? rangePicker[0] : defaultRange[0], 'HH:mm').set({
          year: date.year(),
          month: date.month(),
          date: date.date(),
        });

        const slotTimeEnd = moment(rangePicker[1] ? rangePicker[1] : defaultRange[1], 'HH:mm').set({
          year: date.year(),
          month: date.month(),
          date: date.date(),
        });

        const startRange = rangePicker[0] ?? defaultRange[0];
        const updatedDate = date.clone()
          .hour(startRange.hour())
          .minute(startRange.minute());
        setRangePicker([slotTimeStart, slotTimeEnd]);
        setDateBooking(updatedDate);
      } else {
        setRangePicker([]);
        setSelectedRange({ start: null, end: null });
        setDateBooking(date);
      }
    }
  };

  const handleNextStep = () => {
    tripStore.setRangeDispatcherTransportDate(rangePicker.length ? rangePicker : defaultRange);
    setVisibleGroupTransferChoosingBookingInterval(false);
    setVisiblevisibleGroupTransferForm(true);
  };

  const disabledNext = rangePicker.length ? rangePicker.length < 2 : !defaultRange[1];

  return (
    <div className={styles.wrapper_bookingInterval}>
      <div className={styles.wrapper_bookingIntervalDate}>
        <div className={styles.title_bookingIntervalDate}>
          Выберите интервал бронирования
        </div>
        <DatePicker
          style={{ width: '100%' }}
          className={styles.bookingIntervalDate_picker}
          locale={locale}
          defaultValue={dateBooking ?? date}
          value={dateBooking}
          onChange={onDateChange}
          showTime={true}
          format={DATE_FORMAT.BASE_REVERTED_DOTS}
          placeholder="Выберите дату и время"
          dropdownClassName="no-now-btn"
          disabledDate={getDisabledDate}
          disabled={disabledNext}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      </div>
      <div className={styles.bookingInterval}>
        <>
          {transport && timeSlots.map(time => (
            <TimeSlot
              key={time}
              time={time}
              isBooked={isBooked(time)}
              isSelected={isSelected(time)}
              isPreview={isPreview(time)}
              isStart={time === selectedRange.start}
              isEnd={time === selectedRange.end || time === previewEnd}
              isReversEnd={previewEnd && selectedRange.start && previewEnd < selectedRange.start}
              onClick={() => selectedRange.start && !selectedRange.end ? handleEndSelection(time) : handleStartSelection(time)}
              onMouseEnter={() => handleMouseEnter(time)}
              previewEnd={previewEnd}
              selectedRange={selectedRange}
            />
          ))}
        </>
      </div>
      <div className={styles.wrapper_rangePicker}>
        <span className={styles.title_rangePicker}>
          Выбранный интервал
        </span>
        <TimePicker.RangePicker
          value={!rangePicker.length ? defaultRange : rangePicker}
          onChange={handleRangePickerChange}
          format="HH:mm"
          disabledTime={() => ({
            disabledHours: disabledHours,
            disabledMinutes: disabledMinutes,
          })}
          placeholder={['Начало', 'Окончание']}
          className={styles.bookingInterval_rangePicker}
        />
      </div>
      {validationError && (
        <div className={styles.title_validationError}>{validationError}</div>
      )}
      <div className={styles.wrapper_cost}>
        <span>
          Предварительная
          <br />
          стоимость
        </span>
        <span>{(rangePicker.length > 1 || defaultRange[1]) && transport && formatRubles(transport.calculated.cost / 100)}</span>
      </div>
      <div className={styles.wrapper_buttonNextStep}>
        <TButton
          type="primary"
          $size="middle"
          block={true}
          disabled={disabledNext}
          onClick={handleNextStep}
          className={styles.confirmTimeButton}
        >
          Далее
        </TButton>
      </div>
    </div>
  );
});
