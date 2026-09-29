import React, { ComponentProps, FC } from 'react';
import { DatePicker as DatePickerAntd } from 'antd';
import cn from 'classnames';

import styles from './DatePicker.module.scss';

const DatePicker: FC<ComponentProps<typeof DatePickerAntd>> = ({ className, ...props }) => (
  <DatePickerAntd className={cn(styles.datePicker, className)} {...props} />
);

export default DatePicker;
