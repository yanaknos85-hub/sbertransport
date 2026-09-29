import React, { ComponentProps, FC, useMemo } from 'react';

import { Select } from '../Select';
import { TripTypes } from 'constants/app.constants';
import { ALLOWED_NEXT_TRIP_STATUSES, TRIP_STATUSES, TripStatuses } from 'constants/trips.constants';

const options = Object.keys(TRIP_STATUSES).map(status => ({
  label: TripStatuses[status as TRIP_STATUSES].title,
  cargoLabel: TripStatuses[status as TRIP_STATUSES].cargoTitle,
  value: status,
  isEditable: TripStatuses[status as TRIP_STATUSES].isEditable,
}));

interface StatusSelectProps extends ComponentProps<typeof Select> {
  isEdit?: boolean;
  tripMode: TripTypes;
  restrict?: string[];
}

export const StatusSelect: FC<StatusSelectProps> = ({
  isEdit, tripMode, restrict, ...props
}) => {
  const statusOptions = useMemo(() => {
    let _options = options;

    if (isEdit) {
      const activeStatusIdx = Object.keys(TRIP_STATUSES).findIndex(x => x === props.value);

      const isAvailableStatus = (value: TRIP_STATUSES, index: number) => {
        if (ALLOWED_NEXT_TRIP_STATUSES[props.value as TRIP_STATUSES]) {
          return activeStatusIdx === index || ALLOWED_NEXT_TRIP_STATUSES[props.value as TRIP_STATUSES].includes(value);
        }

        return activeStatusIdx === index;
      };

      _options = options.map((option, index) => ({
        ...option,
        isEditable: option.isEditable && isAvailableStatus(option.value as TRIP_STATUSES, index),
      }));
    }

    return restrict ? _options.filter(({ value }) => restrict.includes(value)) : _options;
  }, [restrict, isEdit, props.value]);

  return (
    <Select
      optionFilterProp="label"
      {...props}
      options={statusOptions.map(({ value, ...x }) => ({
        value,
        label: tripMode === TripTypes.Passenger ? x.label : x.cargoLabel,
        disabled: isEdit && !x.isEditable,
      }))}
    />
  );
};
