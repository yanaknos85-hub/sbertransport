import React, { FC } from 'react';
import { Form, Tabs } from 'antd';
import { observer } from 'mobx-react';

import { TaxiClassEnum, TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';
import TaxiClasses from 'modules/TaxiClasses/TaxiClasses';

import { TransportTypesProps } from '../../types/types';

const { TabPane } = Tabs;

const ExternalPrices: FC<TransportTypesProps> = observer(
  ({
    tariffsInfo, form, purpose, setCurrentTransportType, externalPrices,
  }) => (
    <Tabs defaultActiveKey="1">
      {[
        { type: TaxiClassEnum.ECONOMY, key: '1' },
        { type: TaxiClassEnum.COMFORT, key: '2' },
        { type: TaxiClassEnum.BUSINESS, key: '3' },
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
            />
          </Form.Item>
        </TabPane>
      ))}
    </Tabs>
  )
);

export default ExternalPrices;
