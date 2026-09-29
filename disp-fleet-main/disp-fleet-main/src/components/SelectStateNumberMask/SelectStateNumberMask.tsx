import React, {
  KeyboardEvent, useState, useEffect, useRef
} from 'react';
import { Dropdown, Menu } from 'antd';

import { StateNumberMask } from 'components/StateNumberMask/StateNumberMask';
import { ignore } from 'utils/utils';
import { firstTwoCharsPattern, VEHICLE_TYPES, VehiclesTypes } from './SelectStateNumberMask.constants';

import styles from './styles.module.scss';

interface OwnProps {
  placeholder?: string;
  disabled?: boolean;
  value?: string;
  onChange?: (value: string) => void;
  allowClear?: boolean;
  onPressEnter?: (e: KeyboardEvent<HTMLInputElement>) => void;
  className?: string;
}

export const SelectStateNumberMask = ({
  value, placeholder, onChange = ignore, ...props
}: OwnProps): JSX.Element => {
  const [vehiclesType, setVehiclesType] = useState<VehiclesTypes>(VehiclesTypes.Passenger);

  const firstRender = useRef(true);

  useEffect(() => {
    if (firstRender.current && value) {
      firstRender.current = false;

      if (firstTwoCharsPattern.test(value)) {
        const digitStr = value.replace(/\D/g, '');

        if (digitStr.length >= 5) {
          setVehiclesType(VehiclesTypes.Bus);
        }
      }
    }
  }, [value]);

  const onVehiclesType = (type: VehiclesTypes) => () => {
    setVehiclesType(type);
    value && onChange('');
  };

  return (
    <div className={styles.wrapper}>
      <StateNumberMask
        value={value}
        mask={VEHICLE_TYPES[vehiclesType].mask}
        placeholder={placeholder ?? VEHICLE_TYPES[vehiclesType].example}
        onChange={onChange}
        {...props}
      />
      <Dropdown
        trigger={['click']}
        overlayClassName={styles.dropdownMenu}
        overlay={(
          <Menu>
            {Object.values(VehiclesTypes).map(type => (
              <Menu.Item key={type} onClick={onVehiclesType(type)}>
                {VEHICLE_TYPES[type].title}
              </Menu.Item>
            ))}
          </Menu>
        )}
      >
        <span className={styles.vehicle}>{VEHICLE_TYPES[vehiclesType].title}</span>
      </Dropdown>
    </div>
  );
};
