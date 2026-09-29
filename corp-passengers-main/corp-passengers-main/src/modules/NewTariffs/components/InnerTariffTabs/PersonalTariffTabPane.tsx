/* eslint-disable jsx-a11y/label-has-for */
import React, { FC, useState } from 'react';
import {
  Col, DatePicker, Form, InputNumber, Row, Tabs
} from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { preventDefault } from 'utils';
import { tabPaneStyles } from '../../styles/tariffTabStyles';
import { TariffStrings } from '../../constants/Tariffs.constants';
import { Line } from '../UtilComponents/Line';
import { PersonalTariffTab } from '../../types/types';
import { useCostRateFieldsRequired } from '../../utils/utils';

export const PersonalTariffTabPane: FC<PersonalTariffTab> = ({
  rideCostPerKmInit,
  rideCostPerMinInit,
  distanceIncludedInit,
  timeIncludedInit,
  minRideDistanceCostInit,
  minRideTimeCostInit,
}) => {
  const { TabPane } = Tabs;
  const { RangePicker: DateRange } = DatePicker;
  const defaultCoef = 1.0;

  const initState = (value?: number) => (value === 0 ? 0 : value || '');
  // Условная валидация полей rideCostPerKm, rideCostPerMin
  const [rideCostPerKm, setRideCostPerKm] = useState<number | string | undefined>(initState(rideCostPerKmInit));
  const [rideCostPerMin, setRideCostPerMin] = useState<number | string | undefined>(initState(rideCostPerMinInit));
  // Условная валидация полей (Стоимость минимальной поездки за км, руб. и Стоимость минимальной поездки за минуты, руб.)
  const [distanceIncluded, setDistanceIncluded] = useState<number | string | undefined>(
    initState(distanceIncludedInit)
  );
  const [minRideDistanceCost, setMinRideDistanceCost] = useState<number | string | undefined>(
    initState(minRideDistanceCostInit)
  );
  const [timeIncluded, setTimeIncluded] = useState<number | string | undefined>(initState(timeIncludedInit));
  const [minRideTimeCost, setMinRideTimeCost] = useState<number | string | undefined>(initState(minRideTimeCostInit));

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const isCostRateFieldsRequired = useCostRateFieldsRequired(
    distanceIncluded,
    minRideDistanceCost,
    timeIncluded,
    minRideTimeCost
  );

  return (
    <Tabs style={tabPaneStyles.formWrapper} defaultActiveKey="1">
      <TabPane tab={TariffStrings.parametersTab} key="1">
        <Row>
          <Col span={12}>
            <Form.Item
              style={tabPaneStyles.tabItem}
              label={TariffStrings.rideCostPerKm}
              name="rideCostPerKm"
              initialValue={rideCostPerKm}
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setRideCostPerKm(e === null ? '' : e)}
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
              initialValue={rideCostPerMin}
              rules={[
                {
                  required: true,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setRideCostPerMin(e === null ? '' : e)}
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
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setDistanceIncluded(e === null ? '' : e)}
                min={0}
                step={0.01}
                max={100}
                disabled
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
                  required: minRideTimeCost === '',
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setMinRideDistanceCost(e === null ? '' : e)}
                min={0}
                step={0.01}
                max={1000}
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
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setTimeIncluded(e === null ? '' : e)}
                min={0}
                max={60}
                disabled
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
                  required: minRideDistanceCost === '',
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                onChange={e => setMinRideTimeCost(e === null ? '' : e)}
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
                  required: false,
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
              rules={[
                {
                  required: false,
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
              label={TariffStrings.suburbServiceCostPerMinPers}
              name="suburbServiceCostPerMin"
              rules={[
                {
                  required: false,
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
              label={TariffStrings.costPerKmSuburbPers}
              name="costPerKmSuburb"
              rules={[
                {
                  required: false,
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
              label={TariffStrings.costPerMinSuburbPers}
              name="costPerMinSuburb"
              rules={[
                {
                  required: false,
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
              label={TariffStrings.coefEngine1_6}
              name="coefEngine1_6"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefEngine1_6_to_2_0}
              name="coefEngine1_6_to_2_0"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefEngine2_0_to_2_5}
              name="coefEngine2_0_to_2_5"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
        <Row style={tabPaneStyles.rowWrapper}>
          <Col style={{ marginTop: '5px' }} span={6}>
            <label>{TariffStrings.seasonalCoefficientLabel}</label>
          </Col>
          <Col span={6}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.seasonStartEnd}
              name="seasonStartEnd"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <DateRange locale={locale} format="DD.MM.YYYY" />
            </Form.Item>
          </Col>
          <Col span={6}>
            <Form.Item
              style={tabPaneStyles.formItem}
              label={TariffStrings.seasonalCoefficient}
              name="seasonalCoefficient"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefDayOff}
              name="coefDayOff"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefTraffic}
              name="coefTraffic" // todo
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayMorning}
              name="coefWorkDayMorning"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayNoon}
              name="coefWorkDayNoon"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefPassenger}
              initialValue={defaultCoef}
              name="coefPassenger"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayEvening}
              name="coefWorkDayEvening"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefWorkDayNight}
              name="coefWorkDayNight"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.coefMaterialAssets}
              name="coefMaterialAssets"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
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
              style={tabPaneStyles.formItem}
              label={TariffStrings.trustIdx}
              name="trustIdx"
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
            >
              <InputNumber
                onPressEnter={preventDefault}
                min={0}
                step={0.01}
                max={100000}
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
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
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
              rules={[
                {
                  required: false,
                  message: TariffStrings.anyParameterRequired,
                },
              ]}
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
              rules={[
                {
                  required: false,
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
              label={TariffStrings.minCancelTimeMin}
              name="minCancelTimeMin"
              rules={[
                {
                  required: false,
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
        </Row>
      </TabPane>
    </Tabs>
  );
};
