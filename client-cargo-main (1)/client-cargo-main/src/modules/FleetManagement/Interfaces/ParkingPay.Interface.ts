import { Moment } from 'moment-timezone';

import { STEP } from '../Constants';

export type Steps = STEP.ready | STEP.inProgress | STEP.noLimit;

export interface SelectProps {
  type: string;
  title: string;
  subtitle?: string;
}
export interface TimeRangeProps {
  defaultRange: number;
}

export interface TimerProps {
  startCount: number;
  endParkingTime: Moment;
  endTimerHandler: () => void;
}

export interface TimeSteps {
  [key: number]: string;
  0: string;
  25: string;
  50: string;
  75: string;
  100: string;
}

export interface LoadingTrackProps {
  max: number;
  remainder: number;
}

export interface Remainder {
  hours: number;
  minutes: number;
  seconds: number;
}

export interface DoneModalProps {
  visible: boolean;
  parkingNumber: string;
}

export interface Store {
  step: string;
  regNumber: string;
  parkingNumber: string;
  maxLimit: number;
  remainderLimit: number;
}
