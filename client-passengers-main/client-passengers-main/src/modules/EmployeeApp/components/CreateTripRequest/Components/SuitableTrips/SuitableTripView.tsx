
// @ts-nocheck
import { FormInstance } from 'antd/es/form/Form';
import { observer } from 'mobx-react';
import React, { useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';

import { TaxiClassEnum, TaxiClassTitlesEnum, TransportTypeTitlesEnum } from 'stores/Trip/Trip.interface';
import { FormValues } from '../../types/types';
import ModalInformationTrip from '../ModalInformationTrip/modalInformationTrip';
import { chooseEconomyColor } from './economyUtils';
import styles from './styles.module.scss';
import { TripInfoRowsLayout } from './TripInfoRowsLayout';

export const SuitableTripView = observer(
  ({
    item,
    form,
    onCommonFinish,
  }: {
    item: TripSuitableModel;
    form: FormInstance;
    onCommonFinish: (
      data: FormValues,
      isTripSearching?: boolean,
      isSuitabelTrip?: boolean,
      currentJoiningTrip?: TripSuitableModel
    ) => void;
  }): JSX.Element => {
    const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
    const [visible, setVisible] = useState(false);
    const landing = item.readableTimeStops[0]; // first element contains valid data
    const saving = item.kpi.ordersKpi.find(orderKpi => orderKpi.candidate)?.savingsPct;
    const colorSetting = chooseEconomyColor({
      economyPercent: Math.round(saving || 0), tripStore, form,
    });
    const onVisibleModal = (): void => {
      if (!visible) {
        setVisible(true);
      }
    };
    const { taxiClass } = form.getFieldsValue();
    const onJoinCoopTrip = (currentJoiningTrip?: TripSuitableModel): void => {
      form.setFieldsValue({ coopTripId: currentJoiningTrip.id });
      if (taxiClass && taxiClass !== TaxiClassEnum.ECONOMY && taxiClass !== TaxiClassEnum.PERSONAL) {
        // eslint-disable-next-line no-param-reassign
        currentJoiningTrip.taxiClass = form.getFieldsValue().taxiClass; // временное решение, пока в ручке suitable не приходит taxiClass, так как по дефолту во всех сп по такси taxiClass ставится econom
      }
      onCommonFinish(form.getFieldsValue(), false, currentJoiningTrip);
      setVisible(false);
    };

    const isPersonal = item.transportType === 'PERSONAL';

    const onCancel = () => {
      setVisible(false);
    };

    return (
      <div
        key={item.id}
        className={styles.containerSuitableTrips}
        onClick={() => onVisibleModal()}
      >
        <div className={styles.suitableTrips}>
          <div className={styles.tripInfoMain}>
            <div className={styles.tripInfo}>
              <div className={styles.tripInfoLogo}>
                {isPersonal ? (
                  <span className={styles.transportLogoPersonal} />
                ) : (
                  <span className={styles.transportLogo} />
                )}
              </div>
              <div className={styles.tripInfoTransportType}>
                {isPersonal ? (
                  <span>{TransportTypeTitlesEnum[item.transportType]}</span>
                ) : (
                  <span>
                    {TransportTypeTitlesEnum[item.transportType]}
                    {' '}
                    {TaxiClassTitlesEnum[item.taxiClass]}
                  </span>
                )}
              </div>
            </div>
            <div className={styles.tripInfoDate}>
              <span className={styles.tripInfoTime}>{landing?.startTime}</span>
              <div className={styles.tripInfoAdress}>
                {/* возможно нужно будет доделать функционал совпадения адресса и времени в совмсетсных поездках */}
                {/* <span>Адрес и время совпадают</span> */}
                {/* {uniqueVals.map( el => {
                  if(el.address === dataRequest.waypoints[0].waypoint && el.eventType === 'Посадка') {
                    return <div>Совпадает только адресс</div>
                  }
                })
                } */}
              </div>
            </div>
          </div>
          <div className={styles.tripInfoCost}>
            <TripInfoRowsLayout
              item={item}
              form={form}
              colorSetting={colorSetting}
            />
          </div>
        </div>
        <ModalInformationTrip
          onJoinCoopTrip={onJoinCoopTrip}
          visible={visible}
          onCancel={() => onCancel()}
          colorSetting={colorSetting}
          item={item}
          form={form}
        />
      </div>
    );
  }
);
