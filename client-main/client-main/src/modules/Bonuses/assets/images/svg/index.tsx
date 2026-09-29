import React from 'react';

import { Bonus } from './Bonus';
import { CarSvg } from './carSvg';
import { ReactComponent as FirstGrid } from './firstGrid.svg';
import { ReactComponent as HeaderBonus } from './headerBonus.svg';

type BonusesSvgType<T> = {
  FirstGrid: T;
  CarSvg: T;
  HeaderBonus: T;
  Bonus: T;
};

export const BonusesSvg = (style?: React.SVGAttributes<SVGElement>): BonusesSvgType<() => JSX.Element> =>
  new Proxy(
    {
      FirstGrid: <FirstGrid />,
      CarSvg: <CarSvg />,
      HeaderBonus: <HeaderBonus />,
      Bonus: <Bonus style={style} />,
    } as unknown as BonusesSvgType<() => JSX.Element>,
    {
      get(obj: any, name: PropertyKey) {
        return (): JSX.Element => obj[name];
      },
    } as any
  );
