import { AutoComplete, AutoCompleteProps, Spin } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { IGeoStore } from 'stores/Geo/Geo.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { StyledAddress } from './Address.style';

type Props = AutoCompleteProps & { index: number | undefined; placeholder?: string };

const Address: FC<Props> = observer(({
  index = 0, placeholder, ...rest
}) => {
  const { [StoreNames.geoStore]: geo }: { geoStore: IGeoStore } = useAppStoreContext();
  const {
    addressAutocompleteList, editSingleAddress, onAddressSelect,
  } = geo;

  const [fetching, setFetching] = React.useState(false);

  return (
    <StyledAddress>
      <AutoComplete
        {...rest}
        className="ant-select-customize-input"
        placeholder={placeholder}
        notFoundContent={fetching ? <Spin size="small" /> : <>Адрес не найден</>}
        onSearch={value => {
          if (value.length > 3) {
            setFetching(true);
          }
          editSingleAddress(value);
          if (rest.onSearch) {
            rest.onSearch(value);
          }
        }}
        onSelect={(value, option) => {
          onAddressSelect(value, '', index);
          setFetching(false);
          if (rest.onSelect) {
            rest.onSelect(value, option);
          }
        }}
      >
        {addressAutocompleteList.map(({
          latitude, longitude, addressString,
        }) => (
          <AutoComplete.Option
            value={addressString}
            key={[latitude, longitude, addressString].join()}
            title={addressString}
          >
            {addressString}
          </AutoComplete.Option>
        ))}
      </AutoComplete>
    </StyledAddress>
  );
});

export default Address;
