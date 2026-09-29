import React from 'react';
import MaskedInput from 'antd-mask-input';
import { preventDefault } from 'utils/utils';

const stateNumberMask = ({
  onClearButtonActivator,
  className,
  name,
}: {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void;
  className?: string;
  name?: string;
} = {}): JSX.Element => (
  <MaskedInput
    name={name}
    mask="A111AA 111 RUS"
    placeholder="А123АА 123 RUS"
    allowClear
    onPressEnter={preventDefault}
    onChange={onClearButtonActivator}
    className={className}
  />
);

const StateNumber = stateNumberMask();

const vinMask = (className?: string, name?: string): JSX.Element => (
  <MaskedInput
    name={name}
    mask="###-######-#-#######"
    placeholder="AAA-AAAAAA-A-AAAAAAA"
    allowClear
    onPressEnter={preventDefault}
    className={className}
  />
);

const Vin = vinMask();

const insuranceNumberMask = (className?: string, name?: string): JSX.Element => (
  <MaskedInput
    name={name}
    className={className}
    mask="AAA 1111111111"
    placeholder="AAA 1234567890"
    allowClear
    onPressEnter={preventDefault}
  />
);

const InsuranceNumber = insuranceNumberMask();

const vehiclePassportMask = (className?: string, name?: string, key?: string): JSX.Element => (
  <MaskedInput
    className={className}
    key={key}
    name={name}
    mask="11 AA 111111"
    placeholder="12 AA 123456"
    allowClear
    onPressEnter={preventDefault}
  />
);

const VehiclePassport = vehiclePassportMask();

const phoneMask = ({
  className,
  onClearButtonActivator,
  name,
}: {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void;
  className?: string;
  name?: string;
} = {}): JSX.Element => (
  <MaskedInput
    className={className}
    name={name}
    mask="+7 (111) 111-11-11"
    placeholder="+7 (123) 456-78-90"
    allowClear
    onPressEnter={preventDefault}
    onChange={onClearButtonActivator}
  />
);

const Phone = phoneMask();

const serviceLicenseMask = (isLicenseAvailable?: boolean): JSX.Element => (
  <MaskedInput
    mask="###-11-111111"
    placeholder="AAA-12-123456"
    allowClear
    onPressEnter={preventDefault}
    disabled={!isLicenseAvailable}
  />
);

const ServiceLicense = serviceLicenseMask();

const driverPassportMask = (className?: string): JSX.Element => (
  <MaskedInput
    className={className}
    mask="1111 111111"
    placeholder="1234 123456"
    allowClear
    onPressEnter={preventDefault}
  />
);

const DriverPassport = driverPassportMask();

const driverLicenseMask = (className?: string): JSX.Element => (
  <MaskedInput
    className={className}
    mask="11 11 111111"
    placeholder="12 34 567890"
    allowClear
    onPressEnter={preventDefault}
  />
);

const DriverLicense = driverLicenseMask();

export {
  StateNumber,
  stateNumberMask,
  Vin,
  InsuranceNumber,
  VehiclePassport,
  Phone,
  phoneMask,
  serviceLicenseMask,
  ServiceLicense,
  DriverPassport,
  DriverLicense,
  vinMask,
  insuranceNumberMask,
  vehiclePassportMask,
  driverLicenseMask,
  driverPassportMask
};
