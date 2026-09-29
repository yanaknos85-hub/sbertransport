import React, { SVGProps } from 'react';

export const WarningIcon = (props: SVGProps<SVGSVGElement>) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    width={24}
    height={24}
    fill="none"
    {...props}
  >
    <defs>
      <clipPath id="c">
        <path
          fill="#fff"
          fillOpacity={0}
          d="M0 1h24v24H0z"
        />
      </clipPath>
      <clipPath id="b">
        <path
          fill="#fff"
          fillOpacity={0}
          d="M0 0h24v24H0z"
        />
      </clipPath>
      <clipPath id="a">
        <path
          fill="#fff"
          fillOpacity={0}
          d="M0 0h24v24H0z"
        />
      </clipPath>
    </defs>
    <g clipPath="url(#a)">
      <g clipPath="url(#b)">
        <path
          fill="#FF9A32"
          fillRule="evenodd"
          d="m14.6 3.5 8.1 14c.2.4.4.9.3 1.5 0 1.6-1.3 3-3 3H3.9c-.5 0-1-.1-1.5-.4-1.4-.8-1.9-2.7-1.1-4.1l8.1-14c.2-.4.6-.8 1.1-1.1 1.4-.8 3.3-.3 4.1 1.1Z"
          clipRule="evenodd"
        />
        <g clipPath="url(#c)">
          <path
            fill="#FFF"
            fillRule="evenodd"
            d="M12 14c.6 0 1-.4 1-1V9c0-.61-.4-1-1-1s-1 .39-1 1v4c0 .6.4 1 1 1Zm-1 3c0 .6.4 1 1 1s1-.4 1-1c0-.61-.4-1-1-1s-1 .39-1 1Z"
          />
        </g>
      </g>
    </g>
  </svg>
);
