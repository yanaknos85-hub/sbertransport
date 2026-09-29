import React from 'react';

export type SliderRangeValue = [number, number];

export interface EconomyIndicatorProps {
  title: string;
  value: SliderRangeValue;
  initialRangeValue: SliderRangeValue;
  setRangeValue: React.Dispatch<React.SetStateAction<SliderRangeValue>>;
  busy: boolean;
}
