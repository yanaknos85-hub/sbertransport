import React from 'react';
import { CheckCircleFilled, ClockCircleFilled, ExclamationCircleFilled } from '@ant-design/icons';
import { Col, Row } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import classNames from 'classnames';
import { observer } from 'mobx-react';

import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { MESSAGES, RUBLE_SIGN } from 'constants/constants.app';

import { calculateEconomyCost } from '../../utils/utils';
import { calculateCoincidenceStatus, CoincidenceStatuses } from './utils';

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
    item, form,
  }: { item: TripSuitableModel; form: FormInstance; colorSetting: any }): JSX.Element => {
    // TODO: Разобраться почему где-то id пользователя помеченного -1 - это строка, в другом - число
    const saving = item.kpi.ordersKpi.find(orderKpi => orderKpi.orderId === '-1')?.savingsPct;
    // в данном случае formValues.taxiClass содержит и типы транспорта,
    // а не только классы такси, поэтому передаем вторым аргументом
    // напилот внесли изменения захоркодили передачу вида транспорта
    // и класса для определения типа совместной поездки и просчета тарифа
    const cost = item.kpi.totalCost;
    const economyCost = Number(calculateEconomyCost(cost, saving || 0).toFixed(1));
    const coincidenceStatus = calculateCoincidenceStatus(item);

    const { passengerCount } = form.getFieldsValue();

    const coincidenceItemRender = (coincidence: string): JSX.Element | null => (coincidence === CoincidenceStatuses.fullMatch && <FullMatchItem />)
      || (coincidence === CoincidenceStatuses.deviationsByRoute && <DeviationsByRouteItem />)
      || (coincidence === CoincidenceStatuses.deviationsByTime && <DeviationsByTimeItem />)
      || (coincidence === CoincidenceStatuses.deviationsByBoth && <DeviationsByBothItem />)
      || null;

    return (
      <div className={styles.infoCols}>
        <Row
          justify="start"
          align="middle"
          className={styles.costContainer}
        >
          <div className={styles.economyCost}>{`${economyCost / passengerCount} ${RUBLE_SIGN}`}</div>
          <div className={styles.oldCost}>{`${cost} ${RUBLE_SIGN}`}</div>
        </Row>
        {coincidenceItemRender(coincidenceStatus)}
      </div>
    );
  }
);
