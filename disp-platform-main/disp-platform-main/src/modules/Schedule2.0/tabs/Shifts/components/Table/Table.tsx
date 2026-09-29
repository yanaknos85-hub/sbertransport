import React, { FC, useMemo, useRef } from 'react';

import { Pagination } from '@sber-sbertransport/ui-kit/src';

import cn from 'classnames';
import { Moment } from 'moment';

import { useProfile } from 'api/profile/profile.api';
import { useDriverStatuses } from 'api/schedule2.0/schedule.api';
import { ScheduleVehicle } from 'api/schedule2.0/schedule.types';

import { useTranslation } from 'i18n';

import { SHIFT_DATE_FORMAT } from 'modules/Schedule2.0/constants/schedule.constants';
import { useModals } from 'modules/Schedule2.0/context/modal.context';
import { useSelectedShift } from 'modules/Schedule2.0/context/selectedShift.context';
import { ShiftWithCar } from 'modules/Schedule2.0/Schedule.types';
import { useWorkload } from 'modules/Schedule2.0/tabs/Shifts/context/analyticsWorkload.context';
import { useEstimatedHours } from 'modules/Schedule2.0/tabs/Shifts/context/estimatedHours.context';
import { useShiftsQuery } from 'modules/Schedule2.0/tabs/Shifts/context/shiftsQuery.context';
import { useTableZoom } from 'modules/Schedule2.0/tabs/Shifts/context/tableZoom.context';

import { RainBow } from '../../../../components/RainBow/RainBow';
import useTimes from '../../components/Table/hooks/useTimes';
import { CurrentTime } from './components/CurrentTime/CurrentTime';
import { OnlineSwitcher } from './components/OnlineSwitcher';
import { Shift } from './components/Shift';
import { Th } from './components/Th';
import { Trip } from './components/Trip';
import { Vehicle } from './components/Vehicle';
import { useBusynessData } from './hooks/useBusynessData';
import { useScheduleData } from './hooks/useScheduleData';
import { FIXED_ALL_COLUMNS_WIDTH, FIXED_BASE_COLUMNS_WIDTH } from './Table.constants';

import styles from './Table.module.scss';

export const Table: FC = () => {
  const { t } = useTranslation();
  const { contractorId } = useProfile().data;

  const { query, setPagination } = useShiftsQuery();
  const times = useTimes();

  const { schedule, vehicleIds } = useScheduleData(times);
  const { content: calendarVehicles, totalElements } = schedule;

  const { passBusyness, cargoBusyness } = useBusynessData(vehicleIds, times);

  const statuses = useDriverStatuses({
    contractorId,
    vehicleIds,
    startDate: query.startDate,
    endDate: query.endDate,
  }, {
    suspense: false,
  }).data;

  const { workload, visibleVehicleWorkload } = useWorkload();

  const { cellWidth } = useTableZoom();

  const { handleOpenCreate } = useModals();

  const { setSelectedShift } = useSelectedShift();

  const { estimatedHours } = useEstimatedHours();

  const openCreateSelectedTime = ({ vehicle, time }: { vehicle: ScheduleVehicle; time: Moment }) => () => {
    handleOpenCreate();
    setSelectedShift({
      vehicle,
      startDate: time.toISOString(),
      endDate: time.clone().add(estimatedHours, 'h').toISOString(),
    } as ShiftWithCar);
  };

  const table = useRef<HTMLDivElement>(null);

  const fixedColumnsWidth = useMemo(
    () => (visibleVehicleWorkload ? FIXED_ALL_COLUMNS_WIDTH : FIXED_BASE_COLUMNS_WIDTH),
    [visibleVehicleWorkload]
  );

  return (
    <div className={styles.wrapper}>
      <div className={styles.container} ref={table}>
        <div className={styles.header}>
          <div className={cn(styles.aboutColumns, styles.headerColumns)}>
            {visibleVehicleWorkload && (
              <div className={cn(styles.cell, styles.workload)}>{t.Shifts.workload}</div>
            )}
            <div className={cn(styles.cell, styles.driverInfo)}>{t.Shifts.driverInfo}</div>
            <div className={cn(styles.cell, styles.online)}>{t.Shifts.onLine}</div>
          </div>

          <div className={cn(styles.cells, styles.headerColumns)}>
            {times.map(time => <Th key={time.toISOString()} time={time} />)}
          </div>
        </div>

        {calendarVehicles.map(({ vehicle, shifts }) => {
          const vehicleWorkload = workload?.vehicles.find(({ id }) => id === vehicle.id)?.workload;
          const isVehicleBooked = !!passBusyness
            ?.find(busyness => busyness.vehicleId === vehicle.id)?.trips
            .some(trip => trip.ordered);

          return (
            <div className={cn(styles.cells, styles.dataRow)} key={vehicle.id}>
              <div className={styles.aboutColumns}>
                {visibleVehicleWorkload && (
                  <div className={cn(styles.cell, styles.workload)}>
                    <RainBow value={vehicleWorkload} />
                  </div>
                )}
                <div className={cn(styles.cell, styles.driverInfo, { [styles.booked]: isVehicleBooked })}>
                  <Vehicle vehicle={vehicle} isVehicleBooked={isVehicleBooked} />
                </div>
                <div className={cn(styles.cell, styles.online, { [styles.booked]: isVehicleBooked })}>
                  <OnlineSwitcher
                    data={statuses?.find(status => status.vehicleId === vehicle.id)}
                    vehicleType={vehicle.vehicleType}
                  />
                </div>
              </div>

              {times.map(time => (
                <div
                  key={time.toISOString()}
                  className={cn(styles.cell, styles.clickable, { [styles.booked]: isVehicleBooked })}
                  role="button"
                  onClick={openCreateSelectedTime({ vehicle, time })}
                  tabIndex={-1}
                  style={{ width: cellWidth }}
                >
                  {shifts
                    .filter(shift => shift.calendarStartHour === time.format(SHIFT_DATE_FORMAT))
                    .map(shift => (
                      <Shift
                        key={shift.id}
                        shift={shift}
                        vehicle={vehicle}
                        time={time}
                        isVehicleBooked={false}
                        table={table}
                        fixedColumnsWidth={fixedColumnsWidth}
                      />
                    ))}

                  {passBusyness
                    ?.find(busyness => busyness.vehicleId === vehicle.id)?.trips
                    ?.filter(trip => trip.calendarStartHour === time.format(SHIFT_DATE_FORMAT))
                    .map(trip => (
                      <Trip
                        key={trip.id}
                        trip={trip}
                        vehicle={vehicle}
                        time={time}
                      />
                    ))}

                  {cargoBusyness
                    ?.find(busyness => busyness.vehicleId === vehicle.id)?.trips
                    ?.filter(trip => trip.calendarStartHour === time.format(SHIFT_DATE_FORMAT))
                    .map(trip => (
                      <Trip
                        key={trip.id}
                        trip={trip}
                        vehicle={vehicle}
                        time={time}
                      />
                    ))}
                </div>
              ))}
            </div>
          );
        })}

        <CurrentTime
          times={times}
          vehicleCount={calendarVehicles.length}
          fixedColumnsWidth={fixedColumnsWidth}
        />
      </div>

      <Pagination
        pagination={query}
        setPagination={setPagination}
        total={totalElements}
      />
    </div>
  );
};
