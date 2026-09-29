import { FC } from 'react';
import { InputProps } from 'antd-mobile/es/components/input';
import Input from 'components/Input/input';

const PhoneMask: FC<InputProps> = ({
  value, onChange, ...props
}) => {
  const formatPhone = (input: string): string => {
    const digits = input.replace(/\D/g, '').slice(0, 11);

    if (!digits.length) return '';

    const formatted = [
      '+7',
      digits[0] === '7' ? '' : digits[0],
      ...(digits.length > 1 ? [' (' + digits.slice(1, 4)] : []),
      ...(digits.length > 4 ? [') ' + digits.slice(4, 7)] : []),
      ...(digits.length > 7 ? [' ' + digits.slice(7, 9)] : []),
      ...(digits.length > 9 ? [' ' + digits.slice(9, 11)] : []),
    ].join('');

    return formatted;
  };

  const handleChange = value => {
    const formatted = formatPhone(value);
    onChange?.(formatted);
  };

  return (
    <Input
      value={value}
      onChange={handleChange}
      placeholder="+7 (123) 456 78 90"
      maxLength={18}
      {...props}
    />
  );
};

export default PhoneMask;
