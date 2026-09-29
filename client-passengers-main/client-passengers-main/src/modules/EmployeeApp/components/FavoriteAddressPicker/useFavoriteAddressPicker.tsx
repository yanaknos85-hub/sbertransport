/* eslint-disable @typescript-eslint/no-explicit-any */

/* eslint-disable jsx-a11y/no-static-element-interactions */
/* eslint-disable jsx-a11y/click-events-have-key-events */
import React from 'react';
import Timer from 'shared/components/Images/view/ui/Timer';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { AddressModel } from 'stores/Address/models/Address.model';
import { StoreNames } from 'stores/StoreNames.enum';
import { WaypointOption, filterAutocomplete } from '../CreateTripRequest/Components/Waypoints';
import { AutocompleteText } from '../CreateTripRequest/styles/styled';

const useFavoriteAddressPicker = (currentAddressInput: number): any[] => {
  const { [StoreNames.addressStore]: address, [StoreNames.geoStore]: geo } = useAppStoreContext();

  const handleClick = (e: MouseEvent, item: AddressModel): void => {
    if (currentAddressInput !== undefined) {
      geo.setWaypointFromAddress(item, currentAddressInput);
    }
  };

  const list = [] as any[];
  const selfFavoriteList = address.selfFavoriteList.filter(filterAutocomplete);
  const selfFrequentList = address.selfFrequentList.slice(0, 3).filter(filterAutocomplete);

  if (selfFavoriteList.length) {
    list.push({
      label: 'Избранные',
      options: selfFavoriteList.map(item => ({
        value: item.addressString,
        key: `${item.id} favorite container`,
        label: (
          <AutocompleteText key={`${item.id} from favorite list`}>
            <div onClick={(e: any) => handleClick(e, item)}>
              <WaypointOption item={item} />
            </div>
          </AutocompleteText>
        ),
      })),
    });
  }

  if (selfFrequentList.length) {
    list.push({
      label: 'Недавние',
      options: selfFrequentList.map(item => ({
        value: item.addressString,
        key: `${item.id} frequent container`,
        label: (
          <>
            <Timer />
            <AutocompleteText key={`${item.id} from frequent list`}>
              <div onClick={(e: any) => handleClick(e, item)}>
                <WaypointOption item={item} />
              </div>
            </AutocompleteText>
          </>
        ),
      })),
    });
  }

  return list;
};

export default useFavoriteAddressPicker;
