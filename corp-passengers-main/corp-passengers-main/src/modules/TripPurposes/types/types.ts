import { Moment } from 'moment/moment';
import { Dispatch } from 'react';
import { ConditionType } from '../constants/TripPurposes.constants';

export interface SelectOption {
  value: string;
  label: string;
}

export interface TripPurposeAttributes {
  id: string;
}

export interface TripPurposeDates {
  startDate: Moment;
  endDate: Moment;
}

export interface TripPurposeDepartments {
  id: string;
}

export interface TripPurposeTimes {
  startTime: Moment;
  endTime: Moment;
}

export interface TripPurposeWeekdays {
  weekday: string;
}

export interface SubmitTripPurposeData {
  id?: string;
  label: string;
  purposeType: string;
  tripPurposeAttributes?: TripPurposeAttributes[];
  tripPurposeDates?: TripPurposeDates[];
  tripPurposeDepartments?: TripPurposeDepartments[];
  tripPurposeTimes?: TripPurposeTimes[];
  tripPurposeWeekdays?: TripPurposeWeekdays[];
}

export interface IterableInitialValues {
  conditionSelectOption?: string;
  attributeOption?: string;
  organizationOption?: string;
  departureDates?: Moment[];
  departureTimes?: Moment[];
  departureWeekdays?: string[];
}

export interface Condition {
  indexNo: number;
  conditionSelectOption?: ConditionType;
  attributeOption?: string;
  organizationOption?: string;
  organizationOptionName?: string;
  departureDates?: Moment[];
  departureTimes?: Moment[];
  departureWeekdays?: string[];
}

export interface InnerTripPurposeFormFieldsProps {
  onDelete?: Dispatch<unknown>;
  index: number;
  conditionSelectOptions: SelectOption[];
  weekdaysSelectOptions: SelectOption[];
  attributeOptions: SelectOption[];
  innerFormInitialValues: Condition;
}
