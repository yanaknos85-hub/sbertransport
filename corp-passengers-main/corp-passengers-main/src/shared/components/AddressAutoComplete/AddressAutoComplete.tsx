/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { AutoComplete, Input } from 'antd';
import { useCoordinatesByAddress } from 'api/geo';
import React, { FC, useEffect, useState } from 'react';
import { addressString } from 'stores/Geo/Geo.interface';
import { WaypointModel } from 'stores/Geo/models/Waypoint.model';
import { useDebounce } from 'use-debounce';
import { preventDefault } from 'utils';

interface AddressAutoCompleteProps {
  onSelect?(val?: WaypointModel): void;
  onSearch?(val: string): void;
  value?: string;
  placeholder?: string;
  onFocus?(event: React.FocusEvent<HTMLElement>): void;
}

const AddressAutoComplete: FC<AddressAutoCompleteProps> = ({
  onSelect = () => {},
  onSearch = () => {},
  value = '',
  placeholder = 'Введите адрес',
  onFocus,
}) => {
  const [localValue, setLocalValue] = useState(value);
  const [debouncedValue] = useDebounce(localValue, 500);

  const { data: suggestions, isLoading } = useCoordinatesByAddress(debouncedValue || '', {
    enabled: localValue?.length >= 3,
  });

  const [options, setOptions] = React.useState<any>([]);

  React.useEffect(() => {
    if (isLoading) {
      setOptions([]);
    } else {
      setOptions(suggestions?.map(x => ({ value: addressString(x) })) ?? []);
    }
  }, [suggestions, isLoading]);

  const handleChange = (s: string) => {
    setLocalValue(s);
  };

  useEffect(() => {
    setLocalValue(value);
  }, [value]);

  const selectHandler = (val: string): void => {
    const item = suggestions.find(x => addressString(x) === val);
    setLocalValue(val);
    onSelect(new WaypointModel(item));
  };

  const searchHandler = (val: string): void => {
    setLocalValue(val);
    onSearch(val);
  };

  const handleFocus = (event: React.FocusEvent<HTMLInputElement>) => {
    if (onFocus) {
      onFocus(event);
    }
    event.target.select();
  };

  return (
    <AutoComplete
      style={{ width: '100%' }}
      value={localValue}
      options={options}
      onSelect={selectHandler}
      onSearch={searchHandler}
      onChange={handleChange}
      getPopupContainer={trigger => trigger.parentNode}
      placeholder={placeholder}
      onFocus={handleFocus}
      backfill
      aria-autocomplete="none"
      children={<Input onPressEnter={preventDefault} />}
    />
  );
};

export default AddressAutoComplete;
