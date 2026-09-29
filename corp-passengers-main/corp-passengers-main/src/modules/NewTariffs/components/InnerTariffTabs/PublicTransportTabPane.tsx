import React, { FC } from 'react';
import {
  Col, Form, Row, Tabs, Typography
} from 'antd';
import { tabPaneStyles } from '../../styles/tariffTabStyles';
import { TariffStrings } from '../../constants/Tariffs.constants';
import { CostInput } from './CostInput';
import { MAX_TRAVEL_CARD_VALUE } from '../../utils/utils';
import { ValidationRules } from 'shared/fieldValidationRules';

export const PublicTransportTabPane: FC = (): JSX.Element => (
  <Tabs
    className="publicTabPaneFormWrapper"
    style={tabPaneStyles.publicTabFormWrapper}
    defaultActiveKey="1"
  >
    <Tabs.TabPane tab={TariffStrings.parametersTab} key="1">
      <Row>
        <Col offset={19}>
          <Typography>{TariffStrings.regionAvailability}</Typography>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.metroParam}</Typography>
        </Col>

        <Col span={16}>
          <Form.Item
            style={tabPaneStyles.tabItem}
            name="metroTicketCost"
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput />
          </Form.Item>
        </Col>
      </Row>
      <Row>
        <Col span={8}>
          <Typography>{TariffStrings.cardMetroParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            style={tabPaneStyles.tabItem}
            name="travelCardMetroCost"
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput maxValue={MAX_TRAVEL_CARD_VALUE} />
          </Form.Item>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.tramParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            name="tramTicketCost"
            style={tabPaneStyles.tabItem}
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput />
          </Form.Item>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.cardTramParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            style={tabPaneStyles.tabItem}
            name="travelCardTramCost"
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput maxValue={MAX_TRAVEL_CARD_VALUE} />
          </Form.Item>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.busParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            style={tabPaneStyles.tabItem}
            name="busTicketCost"
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput />
          </Form.Item>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.cardBusParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            name="travelCardBusCost"
            style={tabPaneStyles.tabItem}
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput maxValue={MAX_TRAVEL_CARD_VALUE} />
          </Form.Item>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.trolleyParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            name="trolleybusTicketCost"
            style={tabPaneStyles.tabItem}
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput />
          </Form.Item>
        </Col>
      </Row>
      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.cardTrolleybusParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            name="travelCardTrolleybusCost"
            style={tabPaneStyles.tabItem}
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput maxValue={MAX_TRAVEL_CARD_VALUE} />
          </Form.Item>
        </Col>
      </Row>

      <Row style={tabPaneStyles.tabBaselineRow}>
        <Col span={8}>
          <Typography>{TariffStrings.cardAllCityTransportParam}</Typography>
        </Col>
        <Col span={16}>
          <Form.Item
            style={tabPaneStyles.tabItem}
            name="travelCardAllCityTransportCost"
            rules={[
              ValidationRules.general.checkMinInt(),
            ]}
          >
            <CostInput maxValue={MAX_TRAVEL_CARD_VALUE} />
          </Form.Item>
        </Col>
      </Row>
    </Tabs.TabPane>
  </Tabs>
);
