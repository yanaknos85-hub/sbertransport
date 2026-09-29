import { Form, Tabs } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';

import { TaxiClassEnum, TaxiClassTitlesEnum, TaxiEnum } from 'stores/Trip/Trip.interface';

import { TransportTypesProps } from '../../types/types';
import { TransportTypes } from '../TransportTypes';

const { TabPane } = Tabs;

const availableTaxiClasses = [
  TaxiClassEnum.ECONOMY,
  TaxiClassEnum.COMFORT,
  TaxiClassEnum.COMFORT_PLUS,
  TaxiClassEnum.BUSINESS,
];

const taxiClasses = [
  { type: TaxiClassEnum.ECONOMY, key: '1' },
  { type: TaxiClassEnum.COMFORT, key: '2' },
  { type: TaxiClassEnum.COMFORT_PLUS, key: '3' },
  { type: TaxiClassEnum.BUSINESS, key: '4' },
];

const ExternalPrices: FC<TransportTypesProps> = observer(
  ({
    tariffsInfo,
    form,
    purpose,
    externalPrices,
    product,
    setProduct,
    setStep,
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    setSubClass = () => {},
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    setExternalProvider = () => {},
  }) => {
    const [forcedUpdateForm, setForcedUpdateForm] = useState(false);
    const handleClassChange = (value: string) => {
      const typeForValue = taxiClasses.filter(el => el.key === value)[0].type;
      form?.setFieldsValue({
        externalPrices: typeForValue,
      });
      setForcedUpdateForm(!forcedUpdateForm); // этот стейт нужен для принудительного перерендера, так как форма из antd как то мемонизируется и перерендер перестает происходить
    };

    useEffect(() => {
      form?.setFieldsValue({
        externalPrices: TaxiClassEnum.ECONOMY,
      });
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
      <Form.Item name="externalPrices">
        <Tabs defaultActiveKey="1" onChange={handleClassChange}>
          {taxiClasses.map(i => (
            <TabPane tab={TaxiClassTitlesEnum[i.type]} key={i.key}>
              <Form.Item>
                <TransportTypes
                  tariffsInfo={tariffsInfo}
                  tariffExternal={i}
                  externalPrices={externalPrices}
                  form={form}
                  isExternal={true}
                  purpose={purpose}
                  availableTaxiClasses={availableTaxiClasses}
                  product={product}
                  setProduct={setProduct}
                  setStep={setStep}
                  setSubClass={() => setSubClass(i.type as unknown as TaxiEnum)}
                  setExternalProvider={setExternalProvider}
                />
              </Form.Item>
            </TabPane>
          ))}
        </Tabs>
      </Form.Item>
    );
  }
);

export default ExternalPrices;
