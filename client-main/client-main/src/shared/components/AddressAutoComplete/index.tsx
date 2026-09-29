import { AutoComplete } from 'antd';
import React, { FC, useEffect, useState } from 'react';

import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { noop } from 'utils';

interface AddressAutoCompleteProps {
  list: WaypointModel[];
  onSelect?(val?: WaypointModel): void;
  onSearch?(val: string): void;
  value?: string;
  placeholder?: string;
  onFocus?(event: React.FocusEvent<HTMLElement>): void;
  defaultValue?: string;
}

const AddressAutoComplete: FC<AddressAutoCompleteProps> = ({
  list,
  onSelect = noop,
  onSearch = noop,
  value,
  placeholder = 'Введите адрес',
  onFocus,
  defaultValue,
}) => {
  const options = list.map(x => ({
    key: `${x.addressString}--${x.latitude}-${x.longitude}`,
    value: x.addressString,
  }));
  const initOption = options.find(option => option.value === value);
  const initValue = (initOption && initOption.value) || '';
  const [localValue, setLocalValue] = useState(initValue);
  useEffect(() => {
    setLocalValue(initValue);
  }, [initValue]);

  const selectHandler = (val: string): void => {
    const item = list.find(x => x.addressString === val);
    setLocalValue(val);
    onSelect(item);
  };

  const searchHandler = (val: string): void => {
    setLocalValue(val);
    onSearch(val);
  };

  const handleFocus = (event: React.FocusEvent<HTMLInputElement>): void => {
    if (onFocus) {
      onFocus(event);
    }
    event.target.select();
  };

  return (
    <AutoComplete
      style={{ width: '100%' }}
      // FIXME Обнаружил странный баг с обновлением состояния поля.
      // В некоторых кейсах (зависимость пока не понял)
      // localValue и options имеют новое состояние, а в компоненте
      // отображается старое, при фокусе на поле ввода, значение
      // сбрасывается на актуальное из localValue
      value={defaultValue ?? localValue}
      options={options}
      onSelect={selectHandler}
      onSearch={searchHandler}
      placeholder={placeholder}
      onFocus={handleFocus}
      backfill={true}
      aria-autocomplete="none"
    />
  );
};

export default AddressAutoComplete;
