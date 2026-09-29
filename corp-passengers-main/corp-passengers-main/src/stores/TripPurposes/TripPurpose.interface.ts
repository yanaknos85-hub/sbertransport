import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const tripPurposeAttribute = t.type({
  attribute: t.type({
    id: t.string, name: t.string, status: tt.optional(t.string),
  }),
});

export const tripPurposeTime = t.type({
  startTime: t.string,
  endTime: t.string,
});

export const tripPurposeWeekday = t.type({
  weekday: t.string,
});

export const tripPurposeDate = t.type({
  startDate: t.string,
  endDate: t.string,
});

export const tripPurposeDepartment = t.type({
  department: t.type({
    id: t.string, departmentName: t.string, status: tt.optional(t.string),
  }),
});

export const TripPurpose = t.type({
  id: tt.uuid,
  label: t.string,
  purposeType: t.string,
  tripPurposeAttributes: t.array(tripPurposeAttribute),
  tripPurposeDates: t.array(tripPurposeDate),
  tripPurposeDepartments: t.array(tripPurposeDepartment),
  tripPurposeTimes: t.array(tripPurposeTime),
  tripPurposeWeekdays: t.array(tripPurposeWeekday),
});

export const TripPurposeJournal = t.type({
  id: t.string,
  label: t.string,
  condition: t.string,
});

export type TripPurposeJournal = t.TypeOf<typeof TripPurposeJournal>;
export type TripPurpose = t.TypeOf<typeof TripPurpose>;
export type TripPurposeAttributesArray = t.TypeOf<typeof tripPurposeAttribute>;
