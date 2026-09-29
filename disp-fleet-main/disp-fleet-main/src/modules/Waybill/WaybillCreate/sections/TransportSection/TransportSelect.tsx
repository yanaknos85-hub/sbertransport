import { Select, SelectPropsOriginal } from '@sber-sbertransport/ui-kit/src';
import { useGetSelfOrganizationDispatcher } from 'api/telemechanicDispatchers/telemechanicDispatchers.api';
import { useTelemechanicTransportMutation } from 'api/telemechanicTransport/telemechanicTransport.api';
import { Roles } from 'constants/app.constants';
import { useDebounce } from 'hooks/useDebounce';
import { useRole } from 'hooks/useRole';
import React, { ComponentProps, FC, useState } from 'react';
import { ignore } from 'utils/utils';

const TransportSelect: FC<Omit<SelectPropsOriginal, 'options' | 'onSearch'>> = props => {
  const [transport, setTransport] = useState<ComponentProps<typeof Select>['options']>();

  const [getTransport, { isLoading }] = useTelemechanicTransportMutation();
  const self = useGetSelfOrganizationDispatcher().data;

  const roles = useRole();

  const search = (stateNumber: string) => {
    if (!stateNumber) {
      setTransport([]);
    } else {
      getTransport({
        stateNumber,
        organizationId: roles.includes(Roles.ROLE_MAIN_DISPATCHER_CONTRACTOR) ? self.organization.id : undefined,
        // departmentId: roles.includes(Roles.ROLE_MAIN_DISPATCHER_CONTRACTOR) ? undefined : self.department.id,
      })
        .then(response => {
          setTransport(response?.content.map(vehicle => ({
            label: vehicle.stateNumber,
            value: vehicle.id,
            ...vehicle,
          })) ?? []);
        })
        .catch(ignore);
    }
  };

  const onSearch = useDebounce(search, 300);

  return (
    <Select
      loading={isLoading}
      {...props}
      options={transport}
      onSearch={onSearch}
      showSearch
    />
  );
};

export default TransportSelect;
