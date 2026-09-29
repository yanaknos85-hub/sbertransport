import React, { FC } from 'react';

interface LightningIconProps {
  color?: string;
}

const LightningIcon: FC<LightningIconProps> = ({ color }) => (
  <svg
    width="12"
    height="17"
    viewBox="0 0 12 17"
    fill={color && color}
    stroke={color && color}
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      fillRule="evenodd"
      clipRule="evenodd"
      d="M7.15375 6.14233L11 7.71935L4.0775 15.5973L4.84625 10.082L1 8.50644L7.9225 1.41553L7.15375 6.14233Z"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default LightningIcon;
