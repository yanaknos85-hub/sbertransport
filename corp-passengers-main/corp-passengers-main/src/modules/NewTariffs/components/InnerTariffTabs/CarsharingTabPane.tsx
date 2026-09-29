import React, { FC } from 'react';
import {
  Col, Form, InputNumber, Row, Tabs
} from 'antd';
import { preventDefault } from 'utils';
import { tabPaneStyles } from '../../styles/tariffTabStyles';
import { TariffStrings } from '../../constants/Tariffs.constants';
import { Line } from '../UtilComponents/Line';
import { TaxiTariffTab } from '../../types/types';

export const CarsharingTabPane: FC<TaxiTariffTab> = () => {
  const { TabPane } = Tabs;

  return (
    <Tabs style={tabPaneStyles.formWrapper} defaultActiveKey="1">
      <TabPane tab={TariffStrings.parametersTab} key="1">
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.rideCostPerKmCS}
              name="rideCostPerKm"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0.01}
                step={0.01}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.rideCostPerMinCS}
              name="rideCostPerMin"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={100}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.waitCostPerMinCS}
              name="waitCostPerMin"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={100}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefWorkDayMorning}
              name="coefWorkDayMorning"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefWorkDayNoon}
              name="coefWorkDayNoon"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefWorkDayEvening}
              name="coefWorkDayEvening"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefWorkDayNight}
              name="coefWorkDayNight"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefDayOff}
              name="coefDayOff"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefTraffic}
              name="coefTraffic"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefChildSeat}
              name="coefChildSeat"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={1}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefPetTransport}
              name="coefPetTransport"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={1}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.coefCasko}
              name="coefCasko"
              rules={[
                {
                  required: false,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={1}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
        </Row>
      </TabPane>
    </Tabs>
  );
};
