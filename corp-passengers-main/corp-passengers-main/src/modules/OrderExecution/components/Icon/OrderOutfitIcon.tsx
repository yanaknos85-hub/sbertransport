import React, { FC } from 'react';

interface OrderOutfitIconProps {
  color?: string;
}

const OrderOutfitIcon: FC<OrderOutfitIconProps> = ({ color }) => (
  <svg
    width="40"
    height="40"
    viewBox="0 0 40 40"
    fill="none"
    stroke={color && color}
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M15 25.0022H19.0017"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <path
      d="M25.5052 23.5625L23.6288 25.4389L22.5039 24.3131"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <rect
      x="10.9961"
      y="10.9961"
      width="18.0075"
      height="18.0075"
      rx="3"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <path
      d="M23.0033 21.5002H15"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <rect
      x="15"
      y="14.9961"
      width="10.0042"
      height="3.00125"
      rx="0.5"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
);

export default OrderOutfitIcon;
