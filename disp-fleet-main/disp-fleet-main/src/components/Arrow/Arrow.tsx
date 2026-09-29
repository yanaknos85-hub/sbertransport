import React, { FC } from 'react';
import ArrowUp from 'assets/icons/ArrowUpDefault.svg';
import ArrowDown from 'assets/icons/ArrowDownDefault.svg';

interface Arrow {
  isRotate?: boolean;
  styles?: React.CSSProperties;
}

const Arrow: FC<Arrow> = ({ isRotate = false, styles }) => (
  <div
    style={{
      ...styles,
    }}
  >
    {isRotate
      ? <img src={ArrowUp} alt="ArrowUp" />
      : <img src={ArrowDown} alt="ArrowDown" />}
  </div>
);

export default Arrow;
