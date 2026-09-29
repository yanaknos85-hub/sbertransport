import React, { FC } from 'react';
import { ReactComponent as RedCircleIcon } from 'shared/assets/svg/redCircle.svg';
import { ReactComponent as YellowCircleIcon } from 'shared/assets/svg/yellowCircle.svg';
import { ReactComponent as GreenCircleIcon } from 'shared/assets/svg/greenCircle.svg';

import { DeadlineType } from './deadline.constants';
import type { DeadlineValue } from './deadline.constants';

const CIRCLE_ICON_MAP: Record<DeadlineValue, React.FC<React.SVGProps<SVGSVGElement>>> = {
  [DeadlineType.LESS_THAN_20]: RedCircleIcon,
  [DeadlineType.BETWEEN_20_AND_50]: YellowCircleIcon,
  [DeadlineType.MORE_THAN_50]: GreenCircleIcon,
};

interface Props {
  type: DeadlineValue;
}

const DeadlineCircleIcon: FC<Props> = ({ type }) => {
  const Icon = CIRCLE_ICON_MAP[type];
  return <Icon />;
};

export default DeadlineCircleIcon;