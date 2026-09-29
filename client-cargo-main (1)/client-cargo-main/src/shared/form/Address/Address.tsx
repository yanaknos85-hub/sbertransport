import React, { FC } from 'react';
import {
  AutoComplete, AutoCompleteProps, Spin, Tooltip
} from 'antd';
import TextArea from 'antd/es/input/TextArea';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import useFavoriteAddressPicker from 'modules/FavoriteAddressPicker/useFavoriteAddressPicker';

import { useGeoPosition } from '../../hooks/usePosition';
import { StyledAddress } from './Address.style';

type Props = AutoCompleteProps & {
  index: number | undefined;
  placeholder?: string;
  isTooltip?: boolean;
};

const Address: FC<Props> = observer(({
  index = 0, placeholder, ...rest
}) => {
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.addressStore]: address,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const {
    addressAutocompleteList, editSingleAddress, onAddressSelect,
  } = geo;
  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;

  const specialAddresses = useFavoriteAddressPicker(address, geo, 0, true);

  const { position } = useGeoPosition();

  const [fetching, setFetching] = React.useState(false);

  const onSearch = (value: string) => {
    if (value.length > 3) {
      setFetching(true);
    }
    editSingleAddress(value, position);
    if (rest.onSearch) {
      rest.onSearch(value);
    }
  };
  return (
    <StyledAddress>
      <Tooltip
        color="red"
        // !! Нужно для корректной работы разовой заявки
        // Иначе появляется предупреждение
        open={!!rest.isTooltip}
        placement="bottomRight"
        title="Вы не задали точку маршрута"
      >
        <AutoComplete
          {...rest}
          className="ant-select-customize-input"
          placeholder={placeholder}
          notFoundContent={fetching ? <Spin size="small" /> : <>Адрес не найден</>}
          allowClear
          onSearch={onSearch}
          onSelect={(value, option) => {
            onAddressSelect(value, '', index);
            setFetching(false);
            if (rest.onSelect) {
              rest.onSelect(value, option);
            }
          }}
          options={[
            {
              label: 'Результаты поиска',
              options: (IS_PERSONAL_DEVICE ? addressAutocompleteList.slice(0, 5) : addressAutocompleteList).map(({
                latitude, longitude, addressString,
              }) => ({
                value: addressString,
                key: [latitude, longitude, addressString].join(),
                label: addressString,
              })),
            },
            ...specialAddresses,
          ]}
        >
          <TextArea
            status={rest.isTooltip ? 'error' : ''}
            style={{
              minHeight: '48px', padding: '13xp 16px', resize: 'none',
            }}
            autoSize
          />
        </AutoComplete>
      </Tooltip>
    </StyledAddress>
  );
});

export default Address;
