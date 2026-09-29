import React, { FC } from 'react';
import { Col, Form, Row, Tabs, Switch, Select } from 'antd';
import { useAutoGuide } from "api/tariffs-cargo";
import { tabPaneStyles } from '../../styles/tariffTabStyles';
import { TariffStrings } from '../../constants/Tariffs.constants';
import CargoPackTable from "./CargoPackTable/CargoPackTable";
import CustomInputNumber from 'modules/Customers/Tariffs/components/InputNumber/CustomInputNumber';

import styles from './styles.module.scss';

type Props = {
  /** 
   * Автопланирование 
   * @default false
   */
  autoPlanning?: boolean;
  
  /** 
   * Водитель-грузчик 
   * @remarks Если true, водитель может выполнять погрузку
   */
  driverLoader?: boolean;
  
  /** ID договора */
  contractId?: string;
  
  /** ID контрагента */
  contractorId?: string;
  
  /** 
   * Блокировка поля 
   * @example ['autoPlanning']
   */
  disabledFields?: string[];
  
  /** 
   * Расчет 
   * @example '1 Версия || 2 Версия'
   */
  calculationType?: string;

  /** 
   * Вид транспорта (обязательный)
   */
  transportType: string;
};

export const CargoDedicatedTabPane: FC<Props> = props => {
  const { contractId, contractorId, transportType, disabledFields = [] } = props;

  const { TabPane } = Tabs;

  const { data } = useAutoGuide(transportType);
  const autoGuideOptions = data.map(auto => ({
    label: auto.name,
    value: auto.id,
  }));
  return (
    <Tabs style={tabPaneStyles.formWrapper} defaultActiveKey="1">
      <TabPane tab={TariffStrings.parametersTab} key="1" className={styles.parametersTab}>
        {/* РАСЧЕТ */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              name="calculationType"
              label={TariffStrings.calculation}
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <Select allowClear className={styles.customSelect}>
                <Select.Option value="V1">1 Версия</Select.Option>
                <Select.Option value="V2">2 Версия</Select.Option>
              </Select>
            </Form.Item>
          </Col>
        </Row>

        {/* Тариф за поездку 1 км, руб */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.rideCostPerKmCS}
              name="tariffKm"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <CustomInputNumber
                min={0.01}
                max={10000}
                step={0.01}
                precision={2}
              />
            </Form.Item>
          </Col>
        </Row>

        {/* Стоимость грузчика за 1 час, руб*/}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.costLoader}
              name="costLoader"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <CustomInputNumber min={0} max={10000} step={0.01} precision={2} />
            </Form.Item>
          </Col>
        </Row>

        {/* Минимальное время работы грузчика, час */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.minTimeLoader}
              name="minTimeLoader"
            >
              <CustomInputNumber min={0} max={10} step={1} precision={0} />
            </Form.Item>
          </Col>
        </Row>

        {/* Стоимость минимального времени работы грузчика, руб */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.minCostTimeLoader}
              name="minCostTimeLoader"
            >
              <CustomInputNumber min={0} max={10000} step={0.01} precision={2} />
            </Form.Item>
          </Col>
        </Row>

        {/* Водитель грузчик */}
        <Row>
          <Col span={24}>
            <Form.Item
              name="driverLoader"
              label={TariffStrings.driverLoader}
              valuePropName="checked"
            >
              <Switch
                checkedChildren="Вкл"
                unCheckedChildren="Выкл"
                className={styles.switchRightAligned}
              />
            </Form.Item>
          </Col>
        </Row>

        {/* Бесплатное ожидание */}
        <Row>
          <Col span={24}>
            <Form.Item name="freeWaitingAmount" label={TariffStrings.freeWaitingAmount}>
              <CustomInputNumber min={0} max={500} step={1} />
            </Form.Item>
          </Col>
        </Row>

        {/* Стоимость 1 минуты ожидания */}
        <Row>
          <Col span={24}>
            <Form.Item name="waitingCostMinute" label={TariffStrings.waitingCostMinute} initialValue={0.01}>
              <CustomInputNumber
                min={0.01}
                max={1000}
                step={0.01}
                precision={2}
              />
            </Form.Item>
          </Col>
        </Row>

        {/* Доплата за Экспресс */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.express}
              name="express"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <CustomInputNumber min={0} max={10000} step={0.01} disabled />
            </Form.Item>
          </Col>
        </Row>

        {/* Автопланирование */}
        <Row>
          <Col span={24}>
            <Form.Item name="autoPlanning" label="Автопланирование" valuePropName="checked">
              <Switch
                checkedChildren="Вкл"
                unCheckedChildren="Выкл"
                className={styles.switchRightAligned}
                disabled={disabledFields.includes('autoPlanning')}
              />
            </Form.Item>
          </Col>
        </Row>

        {/* Минимальная поездка, км */}
        <Row>
          <Col span={24}>
            <Form.Item style={tabPaneStyles.tabItem} label={TariffStrings.distanceIncluded} name="distanceIncluded">
              <CustomInputNumber min={0} max={100000} step={0.1} precision={1} />
            </Form.Item>
          </Col>
        </Row>

        {/* Стоимость минимальной поездки, руб */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.minRideDistanceCost}
              name="minRideDistanceCost"
            >
              <CustomInputNumber min={0} max={999999} step={0.1} precision={1} />
            </Form.Item>
          </Col>
        </Row>

        {/* Вид транспорта */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.transportMode}
              name="autoId"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <Select allowClear className={styles.customSelect} options={autoGuideOptions} />
            </Form.Item>
          </Col>
        </Row>

        {/* Максимальная протяженность маршрута, км */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.maxRouteLength}
              name="maxRouteLength"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <CustomInputNumber min={0} max={50000} step={1} />
            </Form.Item>
          </Col>
        </Row>

        {/* Максимальное количество точек в маршруте */}
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.maxWaypointCount}
              name="maxWaypointCount"
              rules={[
                {
                  required: true,
                  message: TariffStrings.integerRequired,
                  pattern: new RegExp('^\\d+$'),
                },
              ]}
            >
              <CustomInputNumber min={0} max={100} step={1} />
            </Form.Item>
          </Col>
        </Row>
      </TabPane>
      <TabPane tab={TariffStrings.packTab} key="2">
        <CargoPackTable contractId={contractId} contractorId={contractorId} />
      </TabPane>
    </Tabs>
  );
};
