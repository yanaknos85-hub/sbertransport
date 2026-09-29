/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-unused-vars */

import { CheckCircleFilled, ClockCircleFilled, ExclamationCircleFilled } from '@ant-design/icons';
import { Col, Row } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import React from 'react';
import { MESSAGES, RUBLE_SIGN } from 'constants/constants.app';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import styles from './styles.module.scss';

const FullMatchItem = (): JSX.Element => (
  <Row
    className={styles.infoDetailed}
    justify="start"
    align="middle"
  >
    <Col span={4} className={styles.halfPart}>
      <CheckCircleFilled />
      {MESSAGES.allPoints}
    </Col>
    <Col span={12} className={classNames(styles.halfPart, styles.fullContent)}>
      <ClockCircleFilled />
      {MESSAGES.allTime}
    </Col>
  </Row>
);

const DeviationsByRouteItem = (): JSX.Element => (
  <Row
    className={styles.infoDetailedWarning}
    justify="start"
    align="middle"
  >
    <Col span={4} className={styles.halfPart}>
      <ExclamationCircleFilled />
    </Col>
    <Col span={12} className={classNames(styles.halfPart, styles.fullContent)}>
      {MESSAGES.deviationsByRoute}
    </Col>
  </Row>
);

const DeviationsByTimeItem = (): JSX.Element => (
  <Row
    className={styles.infoDetailedWarning}
    justify="start"
    align="middle"
  >
    <Col span={4} className={styles.halfPart}>
      <ClockCircleFilled />
    </Col>
    <Col span={12} className={classNames(styles.halfPart, styles.fullContent)}>
      {MESSAGES.deviationsByTime}
    </Col>
  </Row>
);

const DeviationsByBothItem = (): JSX.Element => (
  <Row
    className={styles.infoDetailedWarning}
    justify="start"
    align="middle"
  >
    <Col span={4} className={styles.halfPart}>
      <ExclamationCircleFilled />
    </Col>
    <Col span={12} className={classNames(styles.halfPart, styles.fullContent)}>
      {MESSAGES.deviationsByBoth}
    </Col>
  </Row>
);

export const TripInfoRowsLayout = observer(
  ({
    item, form, colorSetting,
  }: { item: TripSuitableModel; form: FormInstance; colorSetting: any }): JSX.Element => {
    const candidate = item.kpi.ordersKpi.find(orderKpi => orderKpi.candidate);
    const cost = item.kpi.totalCost / 100;
    const economyCost = (candidate?.orderPriceKop || 0) / 100;

    return (
      <div className={styles.infoCols}>
        <Row
          justify="start"
          align="middle"
          className={styles.costContainer}
        >
          <div className={styles.oldCost}>{`${cost} ${RUBLE_SIGN}`}</div>
          <div className={styles.economyCost}>{`${economyCost} ${RUBLE_SIGN}`}</div>
        </Row>
      </div>
    );
  }
);
