import React from 'react';

import Car from './car.png';

export const CarSvg = (): JSX.Element => (
  <svg width="88" height="88" viewBox="0 0 88 88" fill="none">
    <path
      fillRule="evenodd"
      clipRule="evenodd"
      d="M12.4481 27.3925C10.6522 29.1884 7.74047 29.1884 5.94458 27.3925C4.14868 25.5966 4.14868 22.6849 5.94458 20.889L16.9505 9.88305C17.8883 8.94525 19.1304 8.49716 20.3589 8.53879C21.5875 8.49717 22.8296 8.94526 23.7673 9.88305L34.7733 20.889C36.5692 22.6849 36.5692 25.5966 34.7733 27.3925C32.9774 29.1884 30.0657 29.1884 28.2698 27.3925L24.6251 23.7478V40.9423C24.6251 43.4821 22.5662 45.541 20.0264 45.541C17.4867 45.541 15.4278 43.4821 15.4278 40.9423V24.4128L12.4481 27.3925Z"
      fill="url(#paint0_linear_10086_503498)"
    />
    <rect x="11" y="22" width="66" height="44.3438" fill="url(#pattern0)" />
    <defs>
      <pattern id="pattern0" patternContentUnits="objectBoundingBox" width="1" height="1">
        <use href="#image0_10086_503498" transform="scale(0.00111607 0.00166113)" />
      </pattern>
      <linearGradient
        id="paint0_linear_10086_503498"
        x1="-11.1636"
        y1="27.0386"
        x2="25.3706"
        y2="58.1602"
        gradientUnits="userSpaceOnUse"
      >
        <stop stopColor="#1AE281" />
        <stop offset="1" stopColor="#10BF6A" />
      </linearGradient>
      <image id="image0_10086_503498" width="896" height="602" href={Car} />
    </defs>
  </svg>
);
