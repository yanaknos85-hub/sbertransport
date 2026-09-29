/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable jsx-a11y/click-events-have-key-events */
import React from 'react';
import Timer from 'shared/components/Images/view/ui/Timer';

import { IAddressStore } from 'stores/Address/Address.interface';
import { AddressModel } from 'stores/Address/models/Address.model';
import { IGeoStore } from 'stores/Geo/Geo.interface';

import { AutocompleteText } from '../CreateTripRequest/styles/styled';

const useFavoriteAddressPicker = (address: IAddressStore, geo: IGeoStore, currentAddressInput: number, noHandle?: boolean): any[] => {
  const handleClick = (e: MouseEvent, item: AddressModel): void => {
    if (!noHandle && currentAddressInput !== undefined) {
      geo.setWaypointFromAddress(item, currentAddressInput);
    }
  };

  return [
    {
      label: 'Избранные',
      options: address.selfFavoriteList.map(item => ({
        value: item.addressString,
        key: `${item.id} favorite container`,
        label: (
          <AutocompleteText key={`${item.id} from favorite list`}>
            <div onClick={(e: any) => handleClick(e, item)}>{item.addressString}</div>
          </AutocompleteText>
        ),
      })),
    },
    {
      label: 'Недавние',
      options: address.selfFrequentList.slice(0, 3).map(item => ({
        value: item.addressString,
        key: `${item.id} frequent container`,
        label: (
          <>
            <Timer />
            <AutocompleteText key={`${item.id} from frequent list`}>
              <div onClick={(e: any) => handleClick(e, item)}>{item.addressString}</div>
            </AutocompleteText>
          </>
        ),
      })),
    },
  ];
};

export default useFavoriteAddressPicker;
