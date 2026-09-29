/* eslint-disable jsx-a11y/label-has-for */
import React, { FC, useState } from 'react';
import {
  Col, Form, InputNumber, Row, Tabs
} from 'antd';
import { preventDefault } from 'utils';
import { tabPaneStyles } from '../../styles/tariffTabStyles';
import { TariffStrings } from '../../constants/Tariffs.constants';
import { Line } from '../UtilComponents/Line';
import { PersonalTariffTab } from '../../types/types';
import { useCostRateFieldsRequired } from '../../utils/utils';

export const TaxiTariffTabPane: FC<PersonalTariffTab> = ({
  distanceIncludedInit,
  timeIncludedInit,
  minRideDistanceCostInit,
  minRideTimeCostInit,
}) => {
  const { TabPane } = Tabs;

  const initState = (value?: number) => (value === 0 ? 0 : value || '');
  // Условная валидация полей (Стоимость минимальной поездки за км, руб. и Стоимость минимальной поездки за минуты, руб.)
  const [distanceIncluded, setDistanceIncluded] = useState<number | string | undefined>(
    initState(distanceIncludedInit)
  );
  const [minRideDistanceCost, setMinRideDistanceCost] = useState<number | string | undefined>(
    initState(minRideDistanceCostInit)
  );
  const [timeIncluded, setTimeIncluded] = useState<number | string | undefined>(initState(timeIncludedInit));
  const [minRideTimeCost, setMinRideTimeCost] = useState<number | string | undefined>(initState(minRideTimeCostInit));

  const isCostRateFieldsRequired = useCostRateFieldsRequired(
    distanceIncluded,
    minRideDistanceCost,
    timeIncluded,
    minRideTimeCost
  );

  // todo убрать disabled, после реализации алгоритма совместных поездок
  return (
    <Tabs style={tabPaneStyles.formWrapper} defaultActiveKey="1">
      <TabPane tab={TariffStrings.parametersTab} key="1">
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.rideCostPerKm}
              name="rideCostPerKm"
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.rideCostPerMin}
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
                max={1000}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row style={tabPaneStyles.rowWrapper}>
          <Col style={tabPaneStyles.fieldMargin} span={6}>
            <label>{TariffStrings.minCostPerKmText}</label>
          </Col>
          <Col span={3}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label="км"
              name="distanceIncluded"
              rules={[
                {
                  required: isCostRateFieldsRequired.distanceIncludedRequired,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setDistanceIncluded(e === null ? '' : e)}
                min={0}
                step={0.01}
                max={100}
              />
            </Form.Item>
          </Col>
          <Col span={3}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label="руб."
              name="minRideDistanceCost"
              rules={[
                {
                  required: isCostRateFieldsRequired.minRideDistanceCostRequired,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setMinRideDistanceCost(e === null ? '' : e)}
                min={0}
                step={0.01}
                max={100000}
              />
            </Form.Item>
          </Col>
          <Col style={tabPaneStyles.fieldMargin} span={6}>
            <label>{TariffStrings.minCostPerMinText}</label>
          </Col>
          <Col span={3}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label="мин."
              name="timeIncluded"
              rules={[
                {
                  required: isCostRateFieldsRequired.timeIncludedRequired,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setTimeIncluded(e === null ? '' : e)}
                min={0}
                max={9999}
              />
            </Form.Item>
          </Col>
          <Col span={3}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label="руб."
              name="minRideTimeCost"
              rules={[
                {
                  required: isCostRateFieldsRequired.minRideTimeCostRequired,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setMinRideTimeCost(e === null ? '' : e)}
                min={0}
                step={0.01}
                max={9999}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.freeWaitingTime}
              name="freeWaitingTime"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                max={60}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.waitCostPerMin}
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
                max={1000}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.waitCostPerMinIntermediate}
              name="waitCostPerMinIntermediate"
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
                max={1000}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.suburbServiceCostPerKm}
              name="suburbServiceCostPerKm"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.suburbServiceCostPerMin}
              name="suburbServiceCostPerMin"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.costPerKmSuburb}
              name="costPerKmSuburb"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.costPerMinSuburb}
              name="costPerMinSuburb"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayMorning}
              name="coefWorkDayMorning"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayNoon}
              name="coefWorkDayNoon"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayEvening}
              name="coefWorkDayEvening"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayNight}
              name="coefWorkDayNight"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefDayOff}
              name="coefDayOff"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefTraffic}
              name="coefTraffic"
              rules={[{ required: false }]}
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefChildSeat}
              name="coefChildSeat"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefPetTransport}
              name="coefPetTransport"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefBicycle}
              name="coefBicycle"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefOrg}
              name="coefOrg"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={10}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.costPerKmInterRegion}
              name="costPerKmInterRegion"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.costPerMinInterRegion}
              name="costPerMinInterRegion"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                disabled
                min={0}
                step={0.01}
                max={1000}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.maxDiffComputedDistancePercent}
              name="maxDiffComputedDistancePercent"
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
                max={100}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.maxDiffFactDistancePercent}
              name="maxDiffFactDistancePercent"
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
                max={100}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.maxDiffComputedCostPercent}
              name="maxDiffComputedCostPercent"
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
                max={100}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.maxDiffContractorCostPercent}
              name="maxDiffContractorCostPercent"
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
                max={100}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={24}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.maxDiffComputedWaitingPercent}
              name="maxDiffComputedWaitingPercent"
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
              label={TariffStrings.minCancelTimeMin}
              name="minCancelTimeMin"
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
                max={60}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.triggerTime}
              name="triggerTime"
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
                max={250000}
              />
            </Form.Item>
          </Col>
        </Row>
      </TabPane>

      <TabPane tab={TariffStrings.coopParametersTab} key="2">
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.distanceDeviationKm}
              name="distanceDeviationKm"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={2000}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.timeDeviationMin}
              name="timeDeviationMin"
              rules={[{ required: false }]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                max={43200}
              />
            </Form.Item>
          </Col>
        </Row>
        <Line />
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.savingsDeviationPct}
              name="savingsDeviationPct"
              rules={[{ required: false }]}
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
      </TabPane>
    </Tabs>
  );
};
