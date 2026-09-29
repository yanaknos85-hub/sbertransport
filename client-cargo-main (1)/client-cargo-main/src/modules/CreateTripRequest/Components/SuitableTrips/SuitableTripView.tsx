import React from 'react';
import { TeamOutlined } from '@ant-design/icons';
import { Button, Radio } from 'antd';
import { FormInstance } from 'antd/es/form/Form';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { MESSAGES } from 'constants/constants.app';

import { CreateRequestLinks, CreateRequestLinksTitles } from '../../constants/CreateRequest.constants';
import { FormValues } from '../../types/types';
import { chooseEconomyColor } from './economyUtils';
import { TripInfoRowsLayout } from './TripInfoRowsLayout';

import styles from './styles.module.scss';

export const SuitableTripView = observer(
  ({
    item,
    form,
    onCommonFinish,
  }: {
    item: TripSuitableModel;
    form: FormInstance;
    onCommonFinish: (data: FormValues, isTripSearching?: boolean, currentJoiningTrip?: TripSuitableModel) => void;
  }): JSX.Element => {
    const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();
    const emp = item.employeePassengers[0];
    const landing = item.readableTimeStops.find(
      stop => stop.orderId !== -1 && stop.eventType === CreateRequestLinksTitles[CreateRequestLinks.landing]
    );
    // TODO: Разобраться почему где-то id пользователя помеченного -1 - это строка, в другом - число
    const saving = item.kpi.ordersKpi.find(orderKpi => orderKpi.orderId === '-1')?.savingsPct;
    const colorSetting = chooseEconomyColor({
      economyPercent: Math.round(saving || 0), tripStore, form,
    });

    const onJoinCoopTrip = (
      e: React.MouseEvent<HTMLElement, MouseEvent>,
      currentJoiningTrip?: TripSuitableModel
    ): void => {
      form.setFieldsValue({ coopTripId: e.currentTarget.getAttribute('itemid') as string });
      onCommonFinish(form.getFieldsValue(), false, currentJoiningTrip);
    };

    return (
      <div key={item.id} style={{ border: 'none' }}>
        <Radio
          key={item.id}
          className="no-input-radio"
          value={item.id}
        >
          <div className={styles.suitableTrips}>
            <div className={styles.tripType}>{item.isPersonal ? MESSAGES.personalTrip : MESSAGES.taxiTrip}</div>
            <div
              className={styles.economics}
              style={{ backgroundColor: colorSetting.backgroundColor, color: colorSetting.color }}
            >
              {`${CreateRequestLinksTitles[CreateRequestLinks.economy]} ${(saving && Math.round(saving)) ?? 0}%`}
            </div>
            <div className={styles.tripInfo}>
              {item.isPersonal ? (
                <span className={styles.transportLogoPersonal} />
              ) : (
                <span className={styles.transportLogo} />
              )}
              <TripInfoRowsLayout
                item={item}
                form={form}
                colorSetting={colorSetting}
              />
            </div>
            <div>
              <span className={styles.header}>{`${CreateRequestLinksTitles[CreateRequestLinks.dateAndTime]}`}</span>
              <span>{landing?.startTime}</span>
            </div>
            <div>
              <span className={styles.header}>{`${CreateRequestLinksTitles[CreateRequestLinks.tripCreator]}`}</span>
              <span>{emp.shortName}</span>
            </div>
            <div>
              <span className={styles.header}>{`${CreateRequestLinksTitles[CreateRequestLinks.mobilePhone]}`}</span>
              <span>{emp.mobilePhone || MESSAGES.notSpecified}</span>
            </div>
            <div>
              <span className={styles.header}>{`${CreateRequestLinksTitles[CreateRequestLinks.tripStartsFrom]}`}</span>
              <span>{landing?.address}</span>
            </div>
            <Button
              onClick={event => {
                onJoinCoopTrip(event, item);
              }}
              type="primary"
              key={item.id}
              icon={<TeamOutlined />}
              className={styles.button}
              itemID={item.id}
            >
              {CreateRequestLinksTitles[CreateRequestLinks.join]}
            </Button>
          </div>
        </Radio>
      </div>
    );
  }
);
