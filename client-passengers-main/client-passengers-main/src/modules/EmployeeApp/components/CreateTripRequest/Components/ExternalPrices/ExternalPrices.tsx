import { Form, Tabs } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import TaxiClasses from 'modules/EmployeeApp/components/TaxiClasses/TaxiClasses';

import { TaxiClassEnum, TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';

import { TransportTypesProps } from '../../types/types';

const { TabPane } = Tabs;

const availableTaxiClasses = [
  TaxiClassEnum.ECONOMY,
  TaxiClassEnum.COMFORT,
  TaxiClassEnum.COMFORT_PLUS,
  TaxiClassEnum.BUSINESS,
];

const ExternalPrices: FC<TransportTypesProps> = observer(
  ({
    tariffsInfo, form, purpose, setCurrentTransportType, externalPrices,
  }) => (
    <Tabs defaultActiveKey="1">
      {[
        { type: TaxiClassEnum.ECONOMY, key: '1' },
        { type: TaxiClassEnum.COMFORT, key: '2' },
        { type: TaxiClassEnum.COMFORT_PLUS, key: '3' },
        { type: TaxiClassEnum.BUSINESS, key: '4' },
      ].map(i => (
        <TabPane tab={TaxiClassTitlesEnum[i.type]} key={i.key}>
          <Form.Item name={`externalPrices_${i.type}`}>
            <TaxiClasses
              tariffsInfo={tariffsInfo}
              externalPrices={externalPrices}
              isEditable={true}
              form={form}
              purpose={purpose}
              onChange={setCurrentTransportType}
              isExternal={true}
              externalTaxiClass={i.type}
              availableTaxiClasses={availableTaxiClasses}
            />
          </Form.Item>
        </TabPane>
      ))}
    </Tabs>
  )
);

export default ExternalPrices;
