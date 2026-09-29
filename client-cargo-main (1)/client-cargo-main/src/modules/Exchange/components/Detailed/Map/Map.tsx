import React, { FC } from 'react';
import { Col, Row } from 'antd';
import { MapComponent } from 'shared/components/Map/MapComponent';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { WaypointExchangeModel } from 'stores/Exchange/models/WaypointExchangeModel.model';

import { DetailedViewExchange, Segment } from '../../../types';
import AddressBlockMultipleDetailed from '../../AddressBlockMulti/AddressBlockMultipleDetailed';

import styles from './Map.module.scss';

interface Props {
  request: DetailedViewExchange;
}

export const Map: FC<Props> = props => {
  // Из-за существенного изменения в модели waypoints и нехватки времени,
  // пришлось добавить утверждение as WaypointModel[]
  // Точки на карте отображаются корректно
  const markers = props.request?.waypoints
    ?.filter(wp => new WaypointExchangeModel(wp).isValid)
    .map(validWp => validWp.address.coordinates) as WaypointModel[];

  return (
    <div className={styles.cargoMainInnerContent}>
      <Row>
        <Col className={styles.addressCol}>
          <AddressBlockMultipleDetailed
            waypoints={props.request.waypoints}
          />
        </Col>
        <Col className={styles.addressCol}>
          <MapComponent
            markers={markers}
            polylines={props.request?.segments as Segment[]}
            className={styles.map}
            dragging={true}
            zoomControl={true}
          />
        </Col>
      </Row>
    </div>
  );
};
