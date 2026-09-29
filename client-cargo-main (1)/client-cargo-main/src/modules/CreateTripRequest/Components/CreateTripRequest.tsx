import '../styles/override.scss';

import React, { useState } from 'react';
import { Redirect } from 'react-router-dom';
import { PersonalCar } from '@sber-sbertransport/mf-core';
import { Form, RadioChangeEvent, Switch } from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import Arrow from 'shared/components/Images/direction/Arrow';
import { ReactComponent as PlusIcon } from 'shared/components/Images/view/plus.svg';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';

import { StoreNames } from 'stores/StoreNames.enum';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';

import { TripRequestDate, TripRequestPurpose } from '../../EditTripRequestForm/components';
import { useTripRequestDisable, useTripRequestPurpose } from '../../EditTripRequestForm/hooks';
import { useTripPurposeList } from '../../EditTripRequestForm/hooks/useTripPurposeList';
import { useCooperativeTrip } from '../hooks/useCooperativeTrip';
import { useCreateTripRequest } from '../hooks/useCreateTripRequest';
import { useDeepLinks } from '../hooks/useDeepLinks';
import { useTransport } from '../hooks/useTransport';
import { Back, ButtonWrapper, Item } from '../styles/styled';
import { FormValues } from '../types/types';
import { updateFormValuesBySelectedTransport } from '../utils/utils';
import { Additional } from './Additional/Additional';
import ExternalPrices from './ExternalPrices/ExternalPrices';
import { MapRenderer } from './MapRenderer';
import { PersonalCars } from './PersonalCars/PersonalCars';
import { PhoneModal } from './PhoneModal/PhoneModal';
import { TransportTypes } from './TransportTypes';
import { Waypoints } from './Waypoints';

import styles from '../styles/create.module.scss';

const CreateTripRequest: React.FC<any> = observer(() => {
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.employeeStore]: empStore,
  } = useAppStoreContext();

  const [isFirstStep, setFirstStep] = useState(true);
  const [isExternal, setExternal] = useState(false);
  const [personalCar, setPersonalCar] = useState<PersonalCar>();
  const [currentAddressInput, setCurrentAddressInput] = useState(0);

  const { externalPrices } = tripStore;

  const tripPurposeList = useTripPurposeList(tripStore.currentTripRequest);

  const { purposes } = tripPurposeList;

  const {
    initialValues, form, tariffsInfo, saved, getGeolocation, onValuesChange, onFinish, requestExist,
  }
    = useCreateTripRequest({ tripPurposeList });

  const { transport, setTransport } = useTransport(initialValues.taxiClass);

  const tripPurpose = useTripRequestPurpose(initialValues.purpose);

  const coopTripData = useCooperativeTrip({ tripPurposeList });

  const expected = geo.calculatedRoute;
  const from = expected?.segments[0].coordinates[0];
  const to
    = expected?.segments[expected?.segments.length - 2].coordinates[
      expected?.segments[expected?.segments.length - 2].coordinates.length - 1
    ];

  const { disabled, isNoPersonalCars } = useTripRequestDisable({
    transport,
    tripPurpose,
    tariffsCosts: tripStore?.classCosts,
    request: tripStore.currentTripRequest,
    isExternal,
    from,
    to,
  });

  const {
    deeplinkCitymobil, deeplinkUber, deeplinkYandex,
  } = useDeepLinks(form, from, to);

  const { applyCoopTrip } = coopTripData;

  const loadExternalPrices = () => {
    setFirstStep(false);

    if (isExternal) {
      const updatedData = updateFormValuesBySelectedTransport(form.getFieldsValue(), tariffsInfo);

      onFinish(updatedData, true);
    }
  };

  const onCommonFinish = (
    data: FormValues,
    isTripSearching?: boolean,
    currentJoiningTrip?: TripSuitableModel
  ): void => {
    const { coopTrip, coopTripId } = data;
    const updatedData = updateFormValuesBySelectedTransport(data, tariffsInfo);

    if (isExternal) {
      // Создание поездки через контрагента
      window.location.href = deeplinkCitymobil || deeplinkUber || deeplinkYandex || window.location.href;
      return;
    }

    if (!coopTrip) {
      // Создание индивидуальной поездки
      onFinish(updatedData);
      return;
    }

    if (isTripSearching && !coopTripId) {
      // Поиск совместной поезки
      applyCoopTrip(updatedData, isTripSearching, coopTripId, tariffsInfo);
    } else if (!isTripSearching && coopTripId) {
      // Присоединиться к поездке
      applyCoopTrip(updatedData, false, coopTripId, tariffsInfo, currentJoiningTrip);
    } else {
      // Создание совместной поездки
      onFinish(updatedData);
    }
  };

  const onChangePersonalCar = (event: RadioChangeEvent) => setPersonalCar(event.target.value);

  return saved ? (
    <Redirect to="/client/success" />
  ) : (
    <div className={classNames(styles.formWrapper, 'create-trip-request')}>
      <Form
        layout="vertical"
        form={form}
        name="createRequest"
        size="middle"
        onFinish={onCommonFinish}
        initialValues={initialValues}
        onValuesChange={onValuesChange}
        style={{
          height: 'inherit', position: 'relative', display: 'flex',
        }}
      >
        <div className={classNames(styles.mapWrapper)}>
          <MapRenderer geo={geo} />
        </div>

        <div className={classNames(styles.menu)}>
          {isFirstStep ? (
            <>
              <Item $padding="0">
                <Waypoints
                  currentAddressInput={currentAddressInput}
                  setCurrentAddressInput={setCurrentAddressInput}
                  geo={geo}
                  getGeolocation={getGeolocation}
                />

                {(geo.calculatedRoute?.getTimeString || geo.calculatedRoute?.getDistanceString) && (
                  <div className={styles.routeInfo}>
                    <span>{`${geo.calculatedRoute?.getTimeString() ?? ''}`}</span>
                    <span>{`${geo.calculatedRoute?.getDistanceString() ?? ''}`}</span>
                  </div>
                )}

                {!isExternal && (
                  <TButton
                    $size="small"
                    type="link"
                    icon={<PlusIcon />}
                    className={styles.custom_button}
                    block={true}
                    onClick={geo.addWaypoint}
                    $makeLikeLink={true}
                  >
                    Добавить точку
                  </TButton>
                )}
              </Item>

              <Item $padding="16px">
                {!isExternal && (
                  <TripRequestDate
                    purposes={purposes}
                    form={form}
                    disabled={false}
                    tripPurpose={tripPurpose}
                    style={{ margin: '12px 0' }}
                  />
                )}

                <Switch
                  defaultChecked={isExternal}
                  checked={isExternal}
                  onChange={setExternal}
                  checkedChildren="В личных целях"
                  unCheckedChildren="За счет компании"
                />
              </Item>

              {!isExternal && (
                <Item $padding="0">
                  <TripRequestPurpose
                    tripPurposeList={tripPurposeList}
                    tripPurpose={tripPurpose}
                    form={form}
                  />
                </Item>
              )}

              <Item>
                <TransportTypes
                  tariffsInfo={tariffsInfo}
                  externalPrices={externalPrices}
                  request={tripStore.currentTripRequest}
                  form={form}
                  purpose={form.getFieldValue('purpose')}
                  setCurrentTransportType={setTransport}
                />
                <PersonalCars
                  onChangePersonalCar={onChangePersonalCar}
                  specificEmployee={tripStore.currentTripRequest?.author}
                />
              </Item>

              <Item $padding="16px">
                <Additional
                  form={form}
                  transport={transport}
                  submitDisabled={disabled}
                  coopTripData={coopTripData}
                  onCommonFinish={onCommonFinish}
                  personalCar={personalCar}
                  isExternal={isExternal}
                />
              </Item>

              <Item $margin="0" $padding="16px">
                {isExternal ? (
                  <ButtonWrapper>
                    <TButton
                      type="primary"
                      $size="middle"
                      block={true}
                      disabled={disabled}
                      onClick={loadExternalPrices}
                    >
                      Далее
                    </TButton>
                  </ButtonWrapper>
                ) : (
                  <ButtonWrapper>
                    <TButton
                      type="primary"
                      htmlType="submit"
                      $size="middle"
                      className={styles.submit}
                      block={true}
                      disabled={disabled || isNoPersonalCars}
                    >
                      {requestExist ? 'Сохранить' : 'Заказать'}
                    </TButton>
                  </ButtonWrapper>
                )}
              </Item>
            </>
          ) : (
            <>
              <Back>
                <TButton
                  icon={(
                    <Arrow
                      styles={{
                        transform: 'rotate(90deg)',
                        marginTop: '7px',
                        stroke: 'var(--matterhorn)',
                        fill: 'var(--matterhorn)',
                      }}
                    />
                  )}
                  type="text"
                  $size="small"
                  block={true}
                  onClick={() => setFirstStep(true)}
                  $makeLikeLink={true}
                  style={{ justifyContent: 'left', backgroundColor: '#F2F3F6' }}
                >
                  Назад
                </TButton>
              </Back>

              {isExternal && (
                <Item>
                  <ExternalPrices
                    tariffsInfo={tariffsInfo}
                    request={tripStore.currentTripRequest}
                    form={form}
                    purpose={form.getFieldValue('purpose')}
                    setCurrentTransportType={setTransport}
                    isExternal={true}
                    externalPrices={externalPrices}
                  />
                </Item>
              )}

              <ButtonWrapper>
                <TButton
                  type="primary"
                  htmlType="submit"
                  $size="middle"
                  className={styles.submit}
                  block={true}
                  disabled={disabled || isNoPersonalCars}
                >
                  {requestExist ? 'Сохранить' : 'Заказать'}
                </TButton>
              </ButtonWrapper>
            </>
          )}
        </div>

        <PhoneModal selfStore={selfStore} empStore={empStore} />
      </Form>
    </div>
  );
});

export default CreateTripRequest;
