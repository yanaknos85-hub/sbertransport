/* eslint-disable no-nested-ternary */
/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { useEffect, useState } from 'react';
import { Form, Checkbox } from 'antd';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { WaypointField } from 'shared/models/geo/types';
import { TripRequestRoute } from 'shared/hooks/trip/useTripRequestRoute';
import { plainToNew } from 'utils';
import WayPointFormField from 'modules/EmployeeApp/components/TripsPage/TripRequestDetailedView/WayPoints/components/WayPointFormField';
import WaypointAddressAutoComplete from 'modules/EmployeeApp/components/TripsPage/TripRequestDetailedView/WayPoints/components/WaypointAddressAutoComplete';
import ModalCheckIn from '../ModalCheckIn/modalCheckIn';
import './item.scss';

interface Props {
  addresses: WaypointField[];
  tripRequestRoute: TripRequestRoute;
  isCheckInState: boolean;
}

const RouteMapCheckIn = ({
  addresses, tripRequestRoute, isCheckInState,
}: Props): JSX.Element => {
  const {
    actualRoute, geoWaypoints, request,
  } = tripRequestRoute;
  const { waypoints } = actualRoute;
  const { editWaypoint } = geoWaypoints;
  const [onModalCheckIn, setOnModalCheckIn] = useState(false);
  const [numberAddress, setNumberAddress] = useState(0);

  const renderAbsenceReason = (waypointField: WaypointField, waypointIndex: number): JSX.Element => {
    const needDisplayAbsenceReason = !waypointField.checkinAutomatic && waypointField.absenceReason;
    const onEditAbsenceReason = (event: React.ChangeEvent<HTMLInputElement>): void => {
      const { value } = event.target;
      editWaypoint(waypointIndex, plainToNew(WaypointModel, { ...waypointField, absenceReason: value }));
    };

    return (
      <>
        {needDisplayAbsenceReason && isCheckInState && (
          <span className="reasonAbsence">
            Причина отсутствия:
            {waypointField.absenceReason}
          </span>
        )}
      </>
    );
  };

  const renderWaypointAddressAutoComplete = (
    waypointField: WaypointField,
    waypointIndex: number
  ): JSX.Element | null => {
    const onEdit = (value: WaypointModel): WaypointModel[] => editWaypoint(waypointIndex, value);

    return !waypointField.isValid && isCheckInState ? (
      <Form.Item
        name={`address-${waypointField.fieldName}`}
        rules={[{ required: true, message: 'Пожалуйста, выберите адрес' }]}
      >
        <WaypointAddressAutoComplete editWaypoint={onEdit} />
      </Form.Item>
    ) : null;
  };

  return (
    <div className="wrapperMain">
      {addresses.map((address, index) => (
        <div key={index}>
          <div className="wrapperAddresses">
            <div className="wrapperAddressesMain">
              <div className="marker">
                <Checkbox
                  className={address.checkinManual && address.active ? 'activeCheck' : 'unActiveCheck'}
                  onClick={() => {
                    setOnModalCheckIn(true);
                    setNumberAddress(index);
                  }}
                  disabled={address.checkinAutomatic || address.checkinManual || !address.active}
                  checked={address.checkinAutomatic || address.checkinManual}
                />
              </div>
              <div>
                {index === 0 ? (
                  <div>
                    <div className="route">
                      <span>Начало поездки</span>
                      <Form.Item
                        name={address.fieldName}
                        rules={[
                          {
                            required: !address.checkinAutomatic && !address.checkinManual && address.active,
                            message: '',
                          },
                        ]}
                        shouldUpdate
                        initialValue={address.absenceReason}
                      >
                        <span className={!address.active ? 'notActiveRoute' : 'activeRoute'}>
                          {address?.addressString}
                        </span>
                      </Form.Item>
                      {!address.checkinAutomatic && !address.checkinManual && address.active && (
                        <span className="installationNotIndexed">Остановка не зафиксирована</span>
                      )}
                      {address.checkinManual && address.active && (
                        <span className="manualMarking">Ручная отметка на точке</span>
                      )}
                      {address.checkinAutomatic && address.active && (
                        <span className="automaticMarking">Автоматическая отметка на точке</span>
                      )}
                      {!address.active && <span className="removedRoute">Остановка удалена из маршрута</span>}
                      {renderWaypointAddressAutoComplete(address, index)}
                    </div>
                  </div>
                ) : index === addresses.length - 1 ? (
                  <Form.Item
                    name={address.fieldName}
                    rules={[{ required: !address.checkinAutomatic && !address.checkinManual && address.active }]}
                    shouldUpdate
                    initialValue={address.absenceReason}
                  >
                    <div className="route">
                      <span>Конец поездки</span>
                      <Form.Item
                        name={address.fieldName}
                        rules={[
                          {
                            required: !address.checkinAutomatic && !address.checkinManual && address.active,
                            message: '',
                          },
                        ]}
                        shouldUpdate
                        initialValue={address.absenceReason}
                      >
                        <span className={!address.active ? 'notActiveRoute' : 'activeRoute'}>
                          {address?.addressString}
                        </span>
                      </Form.Item>
                      {!address.checkinAutomatic && !address.checkinManual && address.active && (
                        <span className="installationNotIndexed">Остановка не зафиксирована</span>
                      )}
                      {address.checkinManual && address.active && (
                        <span className="manualMarking">Ручная отметка на точке</span>
                      )}
                      {address.checkinAutomatic && address.active && (
                        <span className="automaticMarking">Автоматическая отметка на точке</span>
                      )}
                      {!address.active && <span className="removedRoute">Остановка удалена из маршрута</span>}
                      {renderWaypointAddressAutoComplete(address, index)}
                    </div>
                  </Form.Item>
                ) : (
                  <Form.Item
                    name={address.fieldName}
                    rules={[{ required: !address.checkinAutomatic && !address.checkinManual && address.active }]}
                    shouldUpdate
                    initialValue={address.absenceReason}
                  >
                    <div className="route">
                      <span>
                        Остановка №
                        {index}
                      </span>
                      <Form.Item
                        name={address.fieldName}
                        rules={[
                          {
                            required: !address.checkinAutomatic && !address.checkinManual && address.active,
                            message: '',
                          },
                        ]}
                        shouldUpdate
                        initialValue={address.absenceReason}
                      >
                        <span className={!address.active ? 'notActiveRoute' : 'activeRoute'}>
                          {address?.addressString}
                        </span>
                      </Form.Item>
                      {!address.checkinAutomatic && !address.checkinManual && address.active && (
                        <span className="installationNotIndexed">Остановка не зафиксирована</span>
                      )}
                      {address.checkinManual && address.active && (
                        <span className="manualMarking">Ручная отметка на точке</span>
                      )}
                      {address.checkinAutomatic && address.active && (
                        <span className="automaticMarking">Автоматическая отметка на точке</span>
                      )}
                      {!address.active && <span className="removedRoute">Остановка удалена из маршрута</span>}
                      {renderWaypointAddressAutoComplete(address, index)}
                    </div>
                  </Form.Item>
                )}
              </div>
            </div>
            {renderAbsenceReason(address, index)}
          </div>
        </div>
      ))}
      {onModalCheckIn && (
        <ModalCheckIn
          requestId={request?.id}
          numberAddress={numberAddress}
          addresses={addresses}
          visible={onModalCheckIn}
          onCancel={() => setOnModalCheckIn(false)}
        />
      )}
    </div>
  );
};

export default RouteMapCheckIn;
