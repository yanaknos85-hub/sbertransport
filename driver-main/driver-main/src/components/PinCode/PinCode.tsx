import React from 'react';
import PasscodeInput, { PasscodeInputProps } from 'antd-mobile/es/components/passcode-input';
import NumberKeyboard from 'antd-mobile/es/components/number-keyboard';
import cn from 'classnames';

import styles from './PinCode.module.scss';

const PinCode: React.FC<PasscodeInputProps> = ({
  className, plain = true, seperated = true, ...props
}) => (
  <PasscodeInput
    {...props}
    keyboard={<NumberKeyboard />}
    className={cn(styles.passcode, [className])}
    plain={plain}
    seperated={seperated}
  />
);

export default PinCode;
