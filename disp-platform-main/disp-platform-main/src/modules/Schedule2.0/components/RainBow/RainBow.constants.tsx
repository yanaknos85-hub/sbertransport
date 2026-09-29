import React from 'react';

import { ReactComponent as RainBowEmptySVG } from 'assets/icons/rainBowEmpty.svg';
import { ReactComponent as RainBowFullSVG } from 'assets/icons/rainBowFull.svg';
import { ReactComponent as RainBowLowSVG } from 'assets/icons/rainBowLow.svg';
import { ReactComponent as RainBowMediumSVG } from 'assets/icons/rainBowMedium.svg';
import { ReactComponent as RainBowMediumHighSVG } from 'assets/icons/rainBowMediumHigh.svg';
import { ReactComponent as RainBowMediumLowSVG } from 'assets/icons/rainBowMediumLow.svg';

export enum RainBowType {
  RAIN_BOW_EMPTY = 'RAIN_BOW_EMPTY',
  RAIN_BOW_LOW = 'RAIN_BOW_LOW',
  RAIN_BOW_MEDIUM_LOW = 'RAIN_BOW_MEDIUM_LOW',
  RAIN_BOW_MEDIUM = 'RAIN_BOW_MEDIUM',
  RAIN_BOW_MEDIUM_HIGH = 'RAIN_BOW_MEDIUM_HIGH',
  RAIN_BOW_FULL = 'RAIN_BOW_FULL',
}

export const rainBowIcons: Record<RainBowType, React.SVGProps<SVGSVGElement>> = {
  RAIN_BOW_EMPTY: <RainBowEmptySVG />,
  RAIN_BOW_LOW: <RainBowLowSVG />,
  RAIN_BOW_MEDIUM_LOW: <RainBowMediumLowSVG />,
  RAIN_BOW_MEDIUM: <RainBowMediumSVG />,
  RAIN_BOW_MEDIUM_HIGH: <RainBowMediumHighSVG />,
  RAIN_BOW_FULL: <RainBowFullSVG />,
};

export const rainRange: Record<RainBowType, number[]> = {
  RAIN_BOW_EMPTY: [0, 0],
  RAIN_BOW_LOW: [1, 15],
  RAIN_BOW_MEDIUM_LOW: [16, 35],
  RAIN_BOW_MEDIUM: [36, 71],
  RAIN_BOW_MEDIUM_HIGH: [72, 85],
  RAIN_BOW_FULL: [86, 100],
};
