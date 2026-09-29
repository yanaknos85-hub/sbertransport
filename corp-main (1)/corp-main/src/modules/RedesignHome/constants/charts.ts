import type { ProgressProps } from 'antd';

export const SLA_NORM = 96;
export const CSI_NORM = 95;

export const conicColorsPositive: ProgressProps['strokeColor'] = {
  '0%': 'rgb(103, 210, 52)',
  '25%': 'rgb(50, 191, 84)',
  '50%': 'rgb(58, 205, 149)',
  '75%': 'rgb(11, 147, 198)',
  '100%': 'rgb(203, 234, 16)',
};

export const conicColorsNegative: ProgressProps['strokeColor'] = {
  '0%': 'rgb(210, 106, 52)',
  '33%': 'rgb(3, 3, 3)',
  '66%': 'rgb(255, 211, 0)',
  '100%': 'rgb(234, 16, 16)',
};
