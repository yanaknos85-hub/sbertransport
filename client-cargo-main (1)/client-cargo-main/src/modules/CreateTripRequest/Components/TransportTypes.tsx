import React, { FC } from 'react';
import { Form } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

import { useTripLimit } from '../../TaxiClasses/hooks';
import TaxiClasses from '../../TaxiClasses/TaxiClasses';
import { TransportTypesProps } from '../types/types';

export const TransportTypes: FC<TransportTypesProps> = observer(
  ({
    tariffsInfo, request, form, purpose, setCurrentTransportType, isExternal, externalPrices,
  }) => {
    const { [StoreNames.selfStore]: selfStore } = useAppStoreContext();
    const tripLimit = useTripLimit({ request });

    const isAuthor = selfStore.selfEmployee.id === request?.author.id;
    const requestExist = !!request;
    const isEditable = !requestExist || (requestExist && isAuthor);

    return (
      <Form.Item name="taxiClass">
        <TaxiClasses
          tariffsInfo={tariffsInfo}
          externalPrices={externalPrices}
          isEditable={isEditable}
          form={form}
          purpose={purpose}
          onChange={setCurrentTransportType}
          tripLimit={tripLimit}
          isExternal={isExternal}
        />
      </Form.Item>
    );
  }
);
