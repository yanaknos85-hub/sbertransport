import React from 'react';

import { ReactComponent as OneRoomIcon } from '../icons/one-room.svg';
import { ReactComponent as StudioIcon } from '../icons/studio.svg';
import { ReactComponent as ThreeRoomIcon } from '../icons/three-room.svg';
import { ReactComponent as TwoRoomIcon } from '../icons/two-room.svg';

export const flatsDescription = [
  {
    title: 'Студия',
    text: '~9 коробок или сумок, 4 предмета крупной мебели',
    icon: <StudioIcon />,
    type: 'studio',
  },
  {
    title: '1-комн. квартира',
    text: '~11 коробок или сумок, 5 предметов крупной мебели',
    icon: <OneRoomIcon />,
    type: 'one_room',
  },
  {
    title: '2-комн. квартира',
    text: '~14 коробок или сумок, 8 предметов крупной мебели',
    icon: <TwoRoomIcon />,
    type: 'two_room',
  },
  {
    title: '3-комн. квартира',
    text: '~19 коробок или сумок, 11 предметов крупной мебели',
    icon: <ThreeRoomIcon />,
    type: 'three_room',
  },
];
