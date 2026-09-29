import React, {
  useCallback, useEffect, useState, FC, ComponentProps, useRef
} from 'react';
import { Select, Spin } from 'antd';

import { useProfile } from 'api/profile/profile.api';
import { useSearchAllVehiclesMutation } from 'api/vehicles/vehicles.api';
import { Vehicle, VehiclesSearchResponse } from 'api/vehicles/vehicles.types';

import { TripTypes } from 'constants/app.constants';

import { useDebounce } from 'hooks/useDebounce';

import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

const OFFSET_FOR_FETCHING = 200;

type Props = ComponentProps<typeof Select> & {
  onChangeVal?: (val: string, speciality: TripTypes) => void;
  disableIds?: UUID[];
  searchValue?: string;
};

export const SelectVehicle: FC<Props> = ({
  onChangeVal, disableIds, searchValue, ...props
}) => {
  const { contractorId, autoparkId } = useProfile().data;

  const page = useRef(0);

  const [options, setOptions] = useState<Vehicle[]>([]);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [searchString, setSearchString] = useState(props.disabled && searchValue ? searchValue : '');

  const [fetchVehicles, { isLoading }] = useSearchAllVehiclesMutation(contractorId);

  const getNewData = useCallback(() => {
    const saveNewOptions = (response?: VehiclesSearchResponse) => {
      if (response) {
        const content = response?.content ?? [];
        setOptions(page.current === 0 ? content : prev => [...prev, ...content]);
        setTotalPages(response.totalPages);
      }

      return response;
    };

    fetchVehicles({
      page: page.current,
      size: 20,
      stateNumber: searchString || undefined,
      isActive: true,
      inExploitation: true,
      autopark: autoparkId,
    })
      .then(saveNewOptions)
      .catch(ignore);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchVehicles, searchString, contractorId, autoparkId]);

  useEffect(() => {
    page.current = 0;
    getNewData();
  }, [getNewData]);

  const handleScroll: React.UIEventHandler<HTMLDivElement> = useCallback(
    e => {
      const target = e.target as HTMLDivElement;

      if (isLoading || page.current + 1 >= totalPages) {
        return;
      }

      if (target.scrollTop + target.offsetHeight + OFFSET_FOR_FETCHING < target.scrollHeight) {
        return;
      }

      page.current++;
      getNewData();
    },
    [totalPages, isLoading, getNewData]
  );

  const onSearch = useDebounce(setSearchString, 300);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const onChange = (val: any, option: any) => {
    // какой-то сложный тип у option, нам вообще неважно, что там
    // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
    onChangeVal?.(val as string, options.find(x => x.id === val)?.vehicleType!);
    props.onChange?.(val, option);
  };

  return (
    <Select
      {...props}
      onPopupScroll={handleScroll}
      showSearch
      onSearch={onSearch}
      filterOption={false}
      placeholder="Начните вводить"
      onChange={onChange}
    >
      {options.map(({
        id, stateNumber, model,
      }) => (
        <Select.Option
          key={id}
          value={id}
          disabled={disableIds?.includes(id)}
        >
          {[stateNumber, '|', model.brand, model.name].filter(Boolean).join(' ')}
        </Select.Option>
      ))}
      {isLoading && (
        <Select.Option
          key="loading"
          value="loading"
          disabled
        >
          <Spin size="small" />
        </Select.Option>
      )}
    </Select>
  );
};
