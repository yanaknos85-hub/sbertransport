import { FC } from 'react';
import { InputProps } from 'antd-mobile/es/components/input';
import Input from 'components/Input/input';

const DriverLicenseMask: FC<InputProps> = ({
  value, onChange, ...props
}) => {
  const formatLicense = (input: string): string => {
    const digits = input.replace(/\D/g, '').slice(0, 10);

    if (!digits.length) return '';

    const formatted = [
      digits.slice(0, 2),
      ...(digits.length > 2 ? [' ' + digits.slice(2, 4)] : []),
      ...(digits.length > 4 ? [' ' + digits.slice(4, 10)] : []),
    ].join('');

    return formatted;
  };

  const handleChange = value => {
    const formatted = formatLicense(value);
    onChange?.(formatted);
  };

  return (
    <Input
      value={value}
      onChange={handleChange}
      placeholder="12 34 567890"
      maxLength={12}
      {...props}
    />
  );
};

export default DriverLicenseMask;
