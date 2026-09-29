import React, { SVGProps } from 'react';

export const AlertInfoIcon = (props: SVGProps<SVGSVGElement>) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    {...props}
  >
    <path
      fill="#0091FF"
      fillRule="evenodd"
      d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2Z"
      clipRule="evenodd"
    />
    <path
      fill="#FFF"
      fillRule="evenodd"
      d="M11 8c0 .6.4 1 1 1s1-.4 1-1-.4-1-1-1-1 .4-1 1Zm1 9c.6 0 1-.4 1-1v-4c0-.6-.4-1-1-1s-1 .4-1 1v4c0 .6.4 1 1 1Z"
      clipRule="evenodd"
    />
  </svg>
);
