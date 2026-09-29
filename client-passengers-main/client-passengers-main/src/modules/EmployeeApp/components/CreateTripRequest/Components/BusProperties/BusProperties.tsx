/* eslint-disable @typescript-eslint/no-non-null-assertion */
import {
  Select as AntdSelect, Col, Form, FormInstance, InputNumber, Row
} from 'antd';
import React, {
  FC, useCallback, useEffect, useMemo, useState
} from 'react';
import styled from 'styled-components';
import { useAppStore } from 'ioc/ioc.context';
import { StoreNames } from 'ioc/ioc.storeNames';
import {
  BusEnum, GroupTransferClassesEnum, SubClass, TaxiEnum
} from 'stores/Trip/Trip.interface';
import { TransportTypesConfig } from '../../../TaxiClasses/TaxiClassesConfig';
import { busIcons } from './images';

const Wrapper = styled.div`
  .ant-input-number-handler-wrap {
    opacity: 1;
  }
`;

const Select = styled(AntdSelect)`
  height: 35px;
  .ant-select-selection-item {
    height: auto !important;
    line-height: 30px !important;
    margin: 0 !important;
    padding: 0 !important;
    font-size: 14px !important;
  }
  .ant-select-item-option-content {
    display: block !important;
  }
`;

const Total = styled.div`
  font-weight: bold;
  font-size: 1.25em;
  text-align: right;
`;

const Error = styled.div`
  color: red;
  text-align: center;
  line-height: 1;
  margin: 1em;
`;

interface BusPropertiesProps {
  form: FormInstance;
  onValidate?: (isValid: boolean) => void;
  setSubClass?: (value?: TaxiEnum | BusEnum | GroupTransferClassesEnum) => void;
}

export const BusProperties: FC<BusPropertiesProps> = ({
  form, onValidate, setSubClass,
}) => {
  const [total, setTotal] = useState<number>();
  const [error, setError] = useState('');

  const { [StoreNames.tripStore]: tripStore } = useAppStore();
  const busClassOptions = useMemo(() => tripStore.busCosts, [tripStore.busCosts]);

  const handleBusChange = useCallback(() => {
    const passengerCount = form.getFieldValue('passengerCount') || 0;
    const busCount = 1;
    const busClass = form.getFieldValue('busClass') || '';
    const selectedCost = busClassOptions.find(item => item.id === busClass);
    if (setSubClass) {
      setSubClass(selectedCost?.taxiClass ? (selectedCost?.taxiClass as SubClass) : undefined);
    }
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    const maxPassengers = selectedCost && TransportTypesConfig[selectedCost.taxiClass!].maxPassengers;
    const isPassengerCountValid = maxPassengers === undefined || passengerCount <= maxPassengers * busCount;
    const isValid = Boolean(busCount && busClass && isPassengerCountValid);
    // eslint-disable-next-line @typescript-eslint/no-shadow
    const total = busCount && busClass && selectedCost ? Math.round((busCount * selectedCost.cost) / 100) : undefined;
    setError(isValid || !busClass ? '' : 'Количество пассажиров не соответствует классу и количеству автобусов');
    setTotal(total);
    if (onValidate) {
      onValidate(isValid);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [form, busClassOptions, onValidate, setError, setTotal]);

  useEffect(() => {
    handleBusChange();
  }, [handleBusChange, onValidate]);

  return (
    <Wrapper>
      <Row gutter={16} style={{ marginBottom: '1em' }}>
        <Col span={10}>
          <Form.Item name="busRentDuration" label="Часы аренды">
            <InputNumber min={0} />
          </Form.Item>
        </Col>
        <Col span={14}>
          <Form.Item name="passengerCount" label="Количество пассажиров">
            <InputNumber min={1} onChange={handleBusChange} />
          </Form.Item>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: '1em' }}>
        <Col span={24}>
          <Form.Item name="busClass" label="Класс автобуса">
            <Select onChange={handleBusChange} getPopupContainer={trigger => trigger.parentNode}>
              {busClassOptions.map(item => {
                const Icon = busIcons[item.taxiClass!];
                return (
                  <Select.Option
                    key={item.id}
                    value={item.id}
                    label={TransportTypesConfig[item.taxiClass!].name}
                  >
                    <Row gutter={8}>
                      <Col span={16}>{TransportTypesConfig[item.taxiClass!].name}</Col>
                      <Col span={8}>
                        <span style={{ position: 'relative', top: '3px' }}>{Icon && <Icon />}</span>
                      </Col>
                    </Row>
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
        </Col>
      </Row>

      {error && (
        <Row gutter={16}>
          <Col span={24}>
            <Error>{error}</Error>
          </Col>
        </Row>
      )}

      {total && (
        <Row gutter={16}>
          <Col span={24}>
            <Total>
              Итого:
              {' '}
              <span>
                {total}
                {' '}
                ₽
              </span>
            </Total>
          </Col>
        </Row>
      )}
    </Wrapper>
  );
};
