import React, { ComponentProps, FC, useState } from 'react';
import { Select, SelectPropsOriginal } from '@sber-sbertransport/ui-kit/src';
import { useTelemechanicDriversMutation } from 'api/telemechanicDriver/telemechanicDriver.api';
import { useDebounce } from 'hooks/useDebounce';
import { UUID } from 'utils/io-ts';
import { ignore } from 'utils/utils';

interface DriverSelectProps extends Omit<SelectPropsOriginal, 'options' | 'onSearch'> {
  transportId?: UUID;
}

const DriversSelect: FC<DriverSelectProps> = ({ transportId, ...props }) => {
  const [drivers, setDrivers] = useState<ComponentProps<typeof Select>['options']>();

  const [getDrivers, { isLoading }] = useTelemechanicDriversMutation();

  const onSearch = useDebounce(
    (searchText: string) => {
      if (!searchText) {
        setDrivers([]);
        return;
      }

      getDrivers({
        searchText,
        transportId,
      })
        .then(response => {
          setDrivers(response?.content.map(driver => ({
            label: driver.driver.fullName,
            value: driver.driver.id,
            ...driver,
          })) ?? []);
        })
        .catch(ignore);
    },
    300
  );

  return (
    <Select
      loading={isLoading}
      {...props}
      options={drivers}
      onSearch={onSearch}
      showSearch
    />
  );
};

export default DriversSelect;
