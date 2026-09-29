import React, {
  useCallback, useEffect, useState, FC, ComponentProps, useRef,
  useMemo
} from 'react';
import { Spin } from 'antd';

import { useSearchDriversMutation } from 'api/drivers/drivers.api';
import { Driver, SearchDriversResponse } from 'api/drivers/drivers.types';
import { useProfile } from 'api/profile/profile.api';

import { DriverSpecialityTypes } from 'constants/driver.constants';
import { TripTypes } from 'constants/app.constants';
import { Option, Select } from 'components/Select';
import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import { getFullName } from 'utils/getFullName';
import { useDebounce } from 'hooks/useDebounce';

import styles from './SelectDrivers.module.scss';

const OFFSET_FOR_FETCHING = 200;

type Props = ComponentProps<typeof Select> & {
  type?: TripTypes;
  disableIds?: UUID[];
  fetchOnOpen?: boolean;
  allowAutoparkRestrict?: boolean;
};

export const SelectDrivers: FC<Props> = ({
  type,
  disableIds,
  placeholder = 'Начните вводить',
  fetchOnOpen,
  allowAutoparkRestrict = false,
  ...props
}) => {
  const { contractorId, autoparkId } = useProfile().data;

  const page = useRef(0);

  const [options, setOptions] = useState<Driver[]>([]);
  const [totalPages, setTotalPages] = useState(Infinity);
  const [searchString, setSearchString] = useState('');
  const [isOpen, setOpen] = useState(false);

  const [fetchDrivers, { isLoading }] = useSearchDriversMutation(contractorId);

  const getNewData = useCallback(() => {
    const saveNewOptions = (response?: SearchDriversResponse) => {
      if (response) {
        const content = response?.content ?? [];
        setOptions(page.current === 0 ? content : prev => [...prev, ...content]);
        setTotalPages(response.totalPages);
      }

      return response;
    };

    if (!searchString || searchString.length >= 3) {
      fetchDrivers({
        page: page.current,
        size: 20,
        driverFullName: searchString || undefined,
      })
        .then(saveNewOptions)
        .catch(ignore);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchDrivers, searchString, contractorId]);

  useEffect(() => {
    if (isOpen || !fetchOnOpen) {
      page.current = 0;
      getNewData();
    }
  }, [getNewData, isOpen, fetchOnOpen]);

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

  const filteredOptions = useMemo(
    () => options
      .filter(({ driverSpeciality }) => {
        // Если тип универсальный - показать всех водителей
        if (type === TripTypes.Universal) return true;
        return (type as string) === driverSpeciality || driverSpeciality === DriverSpecialityTypes.Both;
      })
      .filter(driver => allowAutoparkRestrict && autoparkId
        ? !driver.autoparkId || (driver.autoparkId && driver.autoparkId === autoparkId)
        : true
      ),
    [allowAutoparkRestrict, autoparkId, options, type]
  );

  return (
    <Select
      {...props}
      onPopupScroll={handleScroll}
      showSearch
      onSearch={onSearch}
      filterOption={false}
      placeholder={placeholder}
      className={styles.select}
      onFocus={() => setOpen(true)}
      onBlur={() => setOpen(false)}
    >
      {filteredOptions.map(({ id, ...option }) => (
        <Option
          key={id}
          value={id}
          disabled={disableIds?.includes(id)}
        >
          <span>{getFullName(option)}</span>
        </Option>
      ))}
      {isLoading && (
        <Option
          key="loading"
          value="loading"
          disabled
        >
          <Spin size="small" />
        </Option>
      )}
    </Select>
  );
};

