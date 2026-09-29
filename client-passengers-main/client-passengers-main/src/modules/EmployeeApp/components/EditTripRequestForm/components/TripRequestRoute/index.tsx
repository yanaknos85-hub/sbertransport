
import { Button, Form, Input } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import classNames from 'classnames';
import React, { useEffect, useState } from 'react';

import { SpinWrapped } from 'shared/components';
import { ReactComponent as PlusIcon } from 'shared/components/Images/view/plus.svg';
import { CurrentCoordinates } from 'shared/hooks/geo/useCurrentCoordinates';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { AddressModel } from 'stores/Address/models/Address.model';
import { getDistanceString, getTimeString } from 'utils';

import AddressPicker from '../AddressPicker';
import { Waypoints } from '../Waypoints';

import styles from './styles.module.scss';

export default function Component({
  tripRequestRoute,
  currentCoordsState,
  form,
}: {
  tripRequestRoute: TripRequestRoute;
  currentCoordsState: CurrentCoordinates;
  form: FormInstance;
}): JSX.Element {
  const {
    geoWaypoints, actualRoute, inProgress,
  } = tripRequestRoute;

  const [activeWaypointIndex, setActiveWaypointIndex] = useState<number>(0);

  const onFavoriteAddressSelect = (item: AddressModel): void => {
    geoWaypoints.editWaypoint(activeWaypointIndex, item);
  };

  useEffect(() => {
    form.setFieldsValue({ expected: actualRoute });
  }, [form, actualRoute]);

  return (
    <div className={styles.routeContainer}>
      <div className={styles.routeContent}>
        <Form.Item
          noStyle={true}
          name="expected"
          hidden={true}
        >
          <Input />
        </Form.Item>
        <AddressPicker onSelect={onFavoriteAddressSelect} className={classNames('with-hor-scroll-bar')} />
        <Waypoints
          setActiveWaypointIndex={setActiveWaypointIndex}
          geoWaypoints={geoWaypoints}
          currentCoordsState={currentCoordsState}
        />
        <div className={styles.routeInfo}>
          <span>{`${getTimeString(actualRoute.time) ?? ''}`}</span>
          <span>{`${getDistanceString(actualRoute.distance) ?? ''}`}</span>
        </div>
        <Button
          type="link"
          icon={<PlusIcon />}
          size="middle"
          className={styles.buttonAddWaypoint}
          block={true}
          onClick={geoWaypoints.addWaypoint}
        >
          Добавить точку
        </Button>
      </div>
      {inProgress && (
        <div className={styles.routeContainerLoader}>
          <SpinWrapped text="Обновление данных" />
        </div>
      )}
    </div>
  );
}
