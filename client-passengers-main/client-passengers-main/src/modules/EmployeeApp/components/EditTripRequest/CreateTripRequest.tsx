/* eslint-disable @typescript-eslint/no-explicit-any */
// import '../styles/override.scss';
import { PersonalCar } from '@sber-sbertransport/mf-core';
import {
  Form, RadioChangeEvent, Switch, Typography
} from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import React, { useEffect, useRef, useState } from 'react';
import { Redirect } from 'react-router-dom';

import { useGetFrequentlyPurpose } from 'api/purposes';
import * as routes from 'constants/constants.routes';

import { SpinWrapped } from 'shared/components';
import Arrow from 'shared/components/Images/direction/Arrow';
import { ReactComponent as PlusIcon } from 'shared/components/Images/view/plus.svg';
import { CenterCoordinates, Coordinates } from 'shared/components/Map/MapComponent.types';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';
import { TModal } from 'shared/ui/Modal/Modal';
import { StoreNames } from 'stores/StoreNames.enum';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { TaxiClassEnum } from 'stores/Trip/Trip.interface';

import { Additional } from '../CreateTripRequest/Components/Additional/Additional';
import { AdditionalPassenger } from '../CreateTripRequest/Components/Additional/AdditionalPassenger';
import { AdditionalPreferences } from '../CreateTripRequest/Components/Additional/AdditionalPreferences';
import { BusProperties } from '../CreateTripRequest/Components/BusProperties';
import ExternalPrices from '../CreateTripRequest/Components/ExternalPrices/ExternalPrices';
import { MapRenderer } from '../CreateTripRequest/Components/MapRenderer';
import { PersonalCars } from '../CreateTripRequest/Components/PersonalCars/PersonalCars';
import { useCooperativeTrip } from '../CreateTripRequest/hooks/useCooperativeTrip';
import { TripRequestDate, TripRequestPurpose } from '../EditTripRequestForm/components';
import { useTripRequestDisable, useTripRequestPurpose } from '../EditTripRequestForm/hooks';
import { useTripPurposeList } from '../EditTripRequestForm/hooks/useTripPurposeList';
import { useCreateTripRequest } from '../CreateTripRequest/hooks/useCreateTripRequest';
import { useDeepLinks } from '../CreateTripRequest/hooks/useDeepLinks';
import { useTransport } from '../CreateTripRequest/hooks/useTransport';
import {
  Back,
  ButtonWrapper,
  Item,
  Side,
  SideButtons,
  SideContent,
  SideFake,
  SideInner,
  SideSections
} from '../CreateTripRequest/styles/styled';
import { FormValues } from '../CreateTripRequest/types/types';
import { updateFormValuesBySelectedTransport } from '../CreateTripRequest/utils/utils';
import { PhoneModal } from '../CreateTripRequest/Components/PhoneModal/PhoneModal';
import Process from '../Evaluation/Constants/Process';
import styles from '../styles/create.module.scss';
import { TransportTypesConfig, busIdStub } from '../TaxiClasses/TaxiClassesConfig';
import { TransportTypes } from './TransportTypes';
import { Waypoints } from '../CreateTripRequest/Components/Waypoints';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { useAvalailableForSharing } from '../CreateTripRequest/hooks/useAvailableForSharing';

const CreateTripRequest: React.FC<any> = observer(() => {
  const {
    [StoreNames.addressStore]: addressStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
    [StoreNames.geoStore]: geo,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.employeeStore]: empStore,
  } = useAppStoreContext();

  useEffect(() => {
    // 100 раз подумай прежде чем открывать этот запрос!
    // В мейне он и так дергается 1 раз при инициализации. Как минимум можно брать оттуда оставив это условие, если очень надо
    // if(!limitsStore.limitsIsLoaded || !IS_REMOTE) {
    //   limitsStore.getLimits()
    // }
    tripStore.loadCoopTripsSettings(selfStore.orgId); // нужно чтобы цветные плашки рисовать
    tripStore.loadPurposesList(selfStore.orgId); // нужно чтобы был список целей поездок для сохранения
    addressStore.loadFavoriteList();
    addressStore.loadFrequentList();
    transportTypesStore.getAvailableTransportTypes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Нет смысла тянуть availableClasses из organisation/postion (corporateStore.init) т.к карточки все по умолчанию заблокированы,
  // а после выбора адреса они рисуются из tariffs/calculate где учитывается доступность в зависимости от служебного положения
  // const availableTaxiClasses = corporateStore.positionsMapped[selfStore.posId]?.availableClasses || [];
  const availableTaxiClasses = [...Object.keys(TransportTypesConfig)] as TaxiClassEnum[];

  const [isFirstStep, setFirstStep] = useState(true);
  const [isExternal, setExternal] = useState(false);
  const [personalCar, setPersonalCar] = useState<PersonalCar>();
  const [currentAddressInput, setCurrentAddressInput] = useState(0);
  const mapCenterRef = useRef<CenterCoordinates>();
  const [isCreating, setIsCreating] = useState(false);

  const tripPurposeList = useTripPurposeList(tripStore.currentTripRequest);

  const { purposes } = tripPurposeList;

  const {
    initialValues, form, tariffsInfo, saved, onValuesChange, onFinish, requestExist, hasNoRoute,
  }
    = useCreateTripRequest({
      tripPurposeList, mapCenter: mapCenterRef.current, availableTaxiClasses,
    });

  const { transport, setTransport } = useTransport(initialValues.taxiClass);
  const tripPurpose = useTripRequestPurpose(initialValues.purpose);
  const coopTripData = useCooperativeTrip({ tripPurposeList });
  const defaultPurpose = useGetFrequentlyPurpose();

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

  useEffect(() => {
    form.setFieldsValue({ purpose: defaultPurpose.data.id });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const [busCreateEnabled, setBusCreateEnabled] = useState(true);

  const { applyCoopTrip } = coopTripData;

  const commonTransportType = transport?.split('-')[0];

  const {
    deeplinkCitymobil, deeplinkUber, deeplinkYandex,
  } = useDeepLinks(form, from, to);
  const { isAvailableForSharing } = useAvalailableForSharing(commonTransportType as TransportTypeEnum);

  const [isExternalPricesLoading, setIsExternalPricesLoading] = useState(false);
  const loadExternalPrices = async () => {
    setFirstStep(false);
    if (isExternal) {
      const updatedData = updateFormValuesBySelectedTransport(form.getFieldsValue(), tariffsInfo);
      setIsExternalPricesLoading(true);
      await onFinish(updatedData, true);
      setIsExternalPricesLoading(false);
    }
  };

  const onCommonFinish = async (
    data: FormValues,
    isTripSearching?: boolean,
    isSuitabelTrip?: boolean,
    currentJoiningTrip?: TripSuitableModel
  ): Promise<void> => {
    setIsCreating(true);

    const { coopTrip, coopTripId } = data;
    const updatedData = updateFormValuesBySelectedTransport(data, tariffsInfo);

    if (isExternal) {
      // Создание поездки через контрагента
      setIsCreating(false);
      window.location.href = deeplinkCitymobil || deeplinkUber || deeplinkYandex || window.location.href;
      return;
    }

    if (!coopTrip) {
      // Создание индивидуальной поездки
      await onFinish(updatedData);
      setIsCreating(false);
      return;
    }

    if (isTripSearching && !coopTripId) {
      // Поиск совместной поездки
      applyCoopTrip(updatedData, isTripSearching, coopTripId, tariffsInfo);
    } else if (!isTripSearching && coopTripId) {
      // Присоединиться к поездке
      applyCoopTrip(updatedData, false, coopTripId, tariffsInfo, currentJoiningTrip);
    } else {
      // Создание совместной поездки
      await onFinish(updatedData);
    }

    setIsCreating(false);
  };

  const onChangePersonalCar = (event: RadioChangeEvent) => setPersonalCar(event.target.value);
  const onCenterChanged = (center: Coordinates) => {
    mapCenterRef.current = [center.latitude, center.longitude];
  };

  const handleExternalChange = (data: boolean) => {
    setExternal(data);
    setTransport(undefined);
    form.setFieldsValue({ taxiClass: '' });
  };

  const [process, setProcess] = useState<Process>(Process.CLOSE);

  const handleCarsharingConfirm = () => {
    setProcess(Process.CLOSE);
    return Promise.resolve(200);
  };

  const handleCarsharingClose = () => {
    setProcess(Process.CLOSE);
  };

  return saved ? (
    <Redirect to={`${routes.TRIPS_CREATE_SUCCESS}`} />
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
          <MapRenderer geo={geo} onCenterChanged={onCenterChanged} />
        </div>

        <SideFake />
        <Side>
          <SideInner>
            <SideContent>
              {isFirstStep ? (
                <>
                  <SideSections>
                    <Item $padding="0">
                      <Waypoints
                        currentAddressInput={currentAddressInput}
                        setCurrentAddressInput={setCurrentAddressInput}
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

                      {hasNoRoute && (
                        <Typography.Text className={styles.noRouteWarning}>Маршрут не построен</Typography.Text>
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
                        onChange={handleExternalChange}
                        checkedChildren="В личных целях"
                        unCheckedChildren="За счет компании"
                      />
                    </Item>

                    {!isExternal && (
                      <Item $padding="0">
                        <TripRequestPurpose
                          defaultValue={defaultPurpose.data}
                          tripPurposeList={tripPurposeList}
                          tripPurpose={tripPurpose}
                          form={form}
                        />
                      </Item>
                    )}

                    {!isExternal && (
                      <>
                        <Item>
                          <TransportTypes
                            tariffsInfo={tariffsInfo}
                            externalPrices={tripStore.externalPrices}
                            request={tripStore.currentTripRequest}
                            form={form}
                            purpose={form.getFieldValue('purpose')}
                            setCurrentTransportType={setTransport}
                            availableTaxiClasses={availableTaxiClasses}
                            isExternal={isExternal}
                            setProcess={setProcess}
                          />
                          <PersonalCars
                            tariffsInfo={tariffsInfo}
                            onChangePersonalCar={onChangePersonalCar}
                            specificEmployee={tripStore.currentTripRequest?.author}
                          />
                        </Item>

                        {!isExternal && form.getFieldValue('taxiClass')?.endsWith(busIdStub) && (
                          <Item>
                            <BusProperties form={form} onValidate={setBusCreateEnabled} />
                          </Item>
                        )}

                        {!isExternal && isAvailableForSharing && (
                          <>
                            <Item $padding="16px">
                              <Additional
                                form={form}
                                transport={transport}
                                submitDisabled={disabled}
                                coopTripData={coopTripData as any}
                                onCommonFinish={onCommonFinish}
                                personalCar={personalCar}
                              />
                            </Item>
                          </>
                        )}

                        <Item $padding="16px" className="additional-passenger">
                          <AdditionalPassenger disabled={false} />
                        </Item>

                        <Item $padding="16px">
                          <AdditionalPreferences disabled={false} />
                        </Item>

                        <TModal
                          properties={{
                            title: 'Начать поездку вы можете после согласования заявки',
                            confirmButton: { text: 'Ознакомлен' },
                            declineButton: { text: 'Отменить', hide: true },
                            handleConfirm: handleCarsharingConfirm,
                            style: {
                              width: 300,
                              height: 264,
                              justifyContent: 'center',
                              textAlign: 'center',
                              margin: '30px 0 0 0',
                              padding: '0 24px 24px 24px',
                            },
                            withoutScrolls: true,
                          }}
                          process={process}
                          onClose={handleCarsharingClose}
                          isPoppup={false}
                          isReadOnly={false}
                        >
                          <div className={styles.modalContainer}>
                            <p className={styles.modalContainer__title}>
                              При старте аренды до согласования, поездка оплачивается самостоятельно
                            </p>
                          </div>
                        </TModal>
                      </>
                    )}
                  </SideSections>
                  <SideButtons>
                    {isExternal ? (
                      <ButtonWrapper>
                        <TButton
                          style={{ maxWidth: '418px' }}
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
                          style={{ maxWidth: '418px' }}
                          type="primary"
                          htmlType="submit"
                          $size="middle"
                          className={styles.submit}
                          block={true}
                          disabled={disabled || isNoPersonalCars || isCreating || !busCreateEnabled}
                        >
                          {requestExist ? 'Сохранить' : 'Заказать'}
                        </TButton>
                      </ButtonWrapper>
                    )}
                  </SideButtons>
                </>
              ) : (
                <>
                  <SideSections>
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
                        {isExternalPricesLoading ? (
                          <SpinWrapped />
                        ) : (
                          <ExternalPrices
                            tariffsInfo={tariffsInfo}
                            request={tripStore.currentTripRequest}
                            form={form}
                            purpose={form.getFieldValue('purpose')}
                            setCurrentTransportType={setTransport}
                            isExternal={true}
                            externalPrices={tripStore.externalPrices}
                            // eslint-disable-next-line @typescript-eslint/no-empty-function
                            setProduct={() => {}}
                          />
                        )}
                      </Item>
                    )}
                  </SideSections>
                  <SideButtons>
                    <ButtonWrapper>
                      <TButton
                        style={{ maxWidth: '418px' }}
                        type="primary"
                        htmlType="submit"
                        $size="middle"
                        className={styles.submit}
                        block={true}
                        disabled={
                          disabled || isNoPersonalCars || isCreating || !busCreateEnabled || isExternalPricesLoading
                        }
                      >
                        {requestExist ? 'Сохранить' : 'Заказать'}
                      </TButton>
                    </ButtonWrapper>
                  </SideButtons>
                </>
              )}
            </SideContent>
          </SideInner>
        </Side>
        <PhoneModal selfStore={selfStore} empStore={empStore} />
      </Form>
    </div>
  );
});

export default CreateTripRequest;
