/* eslint-disable @typescript-eslint/no-explicit-any */
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { FormInstance, useForm } from 'antd/lib/form/Form';
import { autorun, reaction, toJS } from 'mobx';
import moment, { Moment } from 'moment';
import { useCallback, useEffect, useState } from 'react';

import { useGetFrequentlyPurpose } from 'api/purposes';
import { CenterCoordinates } from 'shared/components/Map/MapComponent.types';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useGeoPosition } from 'shared/hooks/usePosition';
import { RouteModel } from 'shared/models/geo/Route.model';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  ITripTariff,
  TTripRequestNew,
  TaxiClassEnum,
  TripPurpose,
  YandexTripRequest
} from 'stores/Trip/Trip.interface';

import { UUID } from 'utils/io-ts';

import { TripPurposeList } from '../../EditTripRequestForm/hooks/useTripPurposeList';
import { TransportTypesConfig, busIdStub } from '../../TaxiClasses2/TaxiClassesConfig';
import { FormValues } from '../types/types';
import { getTimeZone } from '../utils/utils';
import { getMinCostTaxi } from 'utils/getMinCostTaxi';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { notificationService } from 'utils/showNotification';

export type TripFieldsType = Record<string, any>;

interface CreateTripeRequestHook {
  initialValues: TripFieldsType;
  getGeolocation: () => void;
  onValuesChange: (changedValues: FormValues, allValues: FormValues) => void;
  onFinish: (values: FormValues, isExternal?: boolean) => Promise<void>;
  form: FormInstance;
  purposes: TripPurpose[];
  tariffsInfo: ITripTariff[];
  saved: boolean;
  requestExist: boolean;
  hasNoRoute: boolean;
  isCalculating: boolean;
}

export const useCreateTripRequest = ({
  tripPurposeList,
  mapCenter,
  availableTaxiClasses = [],
}: {
  tripPurposeList?: TripPurposeList;
  mapCenter?: CenterCoordinates;
  availableTaxiClasses?: TaxiClassEnum[];
}): CreateTripeRequestHook => {
  const [form] = useForm();

  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.limitsStore]: limitsStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.purposeStore]: purposeStore,
    [StoreNames.fraudStore]: fraudStore,
  } = useAppStoreContext();

  const { position } = useGeoPosition();
  const requestExist = !!tripStore.currentTripRequest?.isExisting;

  const defaultPurpose = useGetFrequentlyPurpose();

  // states to track route absence
  const [numberOfWaypoints, setNumberOfWaypoints] = useState(0);
  const [hasRoute, setHasRoute] = useState(false);
  const [isCalculating, setIsCalculating] = useState(false);
  const { logger } = useAppStoreContext();

  const onFinish = async (values: FormValues, isExternal?: boolean): Promise<void> => {
    form.validateFields().then(async () => {
      const waypoints = geo.calculatedRoute?.waypoints;

      if (
        values.taxiClass
        && [TaxiClassEnum.YANDEX_COMFORT, TaxiClassEnum.YANDEX_ECONOMY].includes(
          values.taxiClass as unknown as TaxiClassEnum
        )
      ) {
        if (!waypoints) return;

        const date: Moment = values.date || form.getFieldValue('date') || moment();

        const yandexTaxiRequestModel: YandexTripRequest = {
          tripDate: date.toISOString() ?? '',
          purposeId: tripPurposeList?.getById(values.purpose)?.id ?? '',
          tariff: values.taxiClass.split('_')[1] ?? values.taxiClass,
          waypoints,
        };

        await tripStore.saveYandexTrip(yandexTaxiRequestModel);
        purposeStore.setPurpose({ purposeId: '', isValid: false });
        return;
      }

      const isBus = values.tariffId === busIdStub;
      const selectedTariffId = isBus ? values.busClass : values.tariffId;
      const rangeDispatcherTransportDate = tripStore.rangeDispatcherTransportDate;
      const dispatcherTransport = tripStore.availableDispatcherTransport;
      const choosingBookingTransport = tripStore.availableChoosingBookingTransport;
      const difference
        = rangeDispatcherTransportDate && rangeDispatcherTransportDate[1].diff(rangeDispatcherTransportDate[0]);
      const selectedClassCost = tripStore.classCosts.find(({ id }) => id === selectedTariffId);

      tripStore.setProgressApplicationCreationRequest(true);

      const convertingAttachedPassengersArray = () => {
        const passengersArray: string[] = [];
        for (const key in values.listEmployees) {
          key !== 'employee0' && passengersArray.push(values.listEmployees[key].userId);
        }

        return passengersArray;
      };

      const choosingClassTypeTransport = () => {
        if (isBus) {
          return selectedClassCost?.taxiClass;
        }
        if (values.groupTransferClass) {
          return TaxiClassEnum.GROUP_TRANSFER.split('-')[0];
        }

        return values.taxiClass?.split('-')[0];
      };

      const taxiClassNormalized = choosingClassTypeTransport() as TaxiClassEnum;

      const date: Moment = form.getFieldValue('date') || moment();
      const passenger = values.passenger === 'me' ? selfStore.selfEmployee : values.employee;

      const requestOptions: string[] = (values.preferences || []).filter((element: string) => element !== 'commentary');
      const taxiClassesEnum = [
        'ECONOMY',
        'COMFORT',
        'COMFORT_PLUS',
        'BUSINESS',
        'OFFICIAL',
        'VIP_BUS',
        'SMALL_BUS',
        'MIDDLE_BUS',
        'LARGE_BUS',
      ];

      const transportType = TransportTypesConfig[taxiClassNormalized]?.transportType;

      const isCarsharing = transportType === TransportTypeEnum.CARSHARING;
      const isCoopTrip = isCarsharing ? false : values.coopTrip;

      const tariffId
        = transportType === TransportTypeEnum.GROUP_TRANSFER
          ? choosingBookingTransport
            ? choosingBookingTransport.calculated.id
            : dispatcherTransport
              ? dispatcherTransport.calculated.id
              : selectedTariffId
          : selectedTariffId;

      if (!isExternal && transportType !== TransportTypeEnum.BUS) {
        await tripStore.loadActualTariff(transportType?.toLocaleLowerCase(), tariffId as UUID);
      }
      const tariff = tripStore.actualTariff;

      const contractorId = isCarsharing
        ? selectedClassCost?.priceDetails?.carSharingCompId
        : (tariff as any)?.contractorId;

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const bonusCostOfSelectedClass = toJS(tripStore.classCosts).find(
        selectedClass => selectedClass.id === tariffId
        && selectedClass.taxiClass
        && !availableTaxiClasses.includes(selectedClass.taxiClass as TaxiClassEnum)
      )?.bonusCost;

      const requestObject: Record<string, any> = {
        author: selfStore.selfEmployee,
        passenger: passenger ?? selfStore.selfEmployee,
        transportType:
          isExternal || taxiClassesEnum.includes(taxiClassNormalized) ? TaxiClassEnum.TAXI : taxiClassNormalized,
        taxiClass: isExternal ? TaxiClassEnum.ECONOMY : taxiClassNormalized,
        passengerCount: values.passengerCount || 1,
        ...(!isExternal && { tariffId: selectedTariffId }),
        desiredDate: date.valueOf(),
        // ToDo: expected: geo.calculatedRoute - поставить вместо существующего когда бонусы будут считаться на беке. Щас бонусы расчитываются на фронте
        expected: { ...geo.calculatedRoute, outcomeCost: selectedClassCost?.outcomeCost ?? undefined },
        purpose: tripPurposeList?.getById(values.purpose),
        approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
        coopTrip: isCoopTrip,
        commentForDriver: values.commentary,
        requestOptions,
        timeZone: getTimeZone(),
        ...(contractorId && { contractorId }),
        ...(transportType === TransportTypeEnum.BUS && { busCount: 1 }),
        ...(transportType === TransportTypeEnum.BUS && { busRentDuration: values.busRentDuration }),
        joinedPassengerIds: values.listEmployees ? convertingAttachedPassengersArray() : undefined,
        source: 'WEB',
        minTariffTaxi: getMinCostTaxi(tripStore.classCosts),
        commentForPurpose: values.commentForPurpose,
        outcomeTariffId: selectedClassCost?.outcomeTariffId ?? undefined,
      };
      if (
        !taxiClassesEnum.includes(taxiClassNormalized)
        && geo.calculatedRoute
        // limitsStore.currentLimit && // Требует избыточного запроса по всем лимитам
        && !isExternal
      ) {
        const fraudComment = fraudStore.prepareFraudComment();
        // если это не такси
        const newRequestObject = {
          author: selfStore.selfEmployee,
          passenger: passenger ?? selfStore.selfEmployee,
          transportType: TransportTypesConfig[taxiClassNormalized]?.transportType,
          passengerCount: values.passengerCount || 1,
          tariffId: dispatcherTransport ? dispatcherTransport.calculated.id : selectedTariffId,
          desiredDate: rangeDispatcherTransportDate ? rangeDispatcherTransportDate[0].valueOf() : date.valueOf(),
          expected: new RouteModel({
            ...(geo.calculatedRoute as RouteModel),
            time: difference ?? geo.calculatedRoute.time,
            // cost: tripStore.personalCarsCosts.find(el => el?.id === values.personalCar?.id)?.cost,  // comment out personal car individual tariffs
            cost: choosingBookingTransport
              ? choosingBookingTransport.calculated.cost
              : dispatcherTransport
                ? dispatcherTransport.calculated.cost
                : (selectedClassCost?.cost || 0),
            outcomeCost: selectedClassCost?.outcomeCost ?? undefined,
          }),
          purpose: tripPurposeList?.getById(values.purpose),
          approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
          coopTrip: isCoopTrip,
          personalCarId: values.personalCar && values.personalCar,
          commentForDriver: values.commentary,
          requestOptions,
          taxiClass: undefined,
          timeZone: getTimeZone(),
          ...(contractorId && { contractorId }),
          ...(transportType === TransportTypeEnum.BUS && { busCount: 1 }),
          ...(transportType === TransportTypeEnum.BUS && { busRentDuration: values.busRentDuration }),
          groupTransferClass: values.groupTransferClass && values.groupTransferClass,
          information: values.information && {
            ...values.information,
            transportId: dispatcherTransport && dispatcherTransport.id,
            dateFlight: values.information.dateFlight && values.information.dateFlight.valueOf(),
            clientFullName: undefined,
            clientInfoPhone: undefined,
            addContactPhone: values.information.clientInfoPhone,
            addContactFIO: values.information.clientFullName,
          },
          joinedPassengerIds: values.listEmployees ? convertingAttachedPassengersArray() : undefined,
          source: 'WEB',
          minTariffTaxi:
            TransportTypesConfig[taxiClassNormalized]?.transportType !== TransportTypeEnum.GROUP_TRANSFER
              ? getMinCostTaxi(tripStore.classCosts)
              : undefined,
          commentForPurpose: values.commentForPurpose,
          outcomeTariffId: dispatcherTransport ? dispatcherTransport.calculated.outcomeTariffId : selectedTariffId,
          ...(fraudComment && { fraudComment }),
        };

        if (newRequestObject.expected.cost === 0) {
          logger.toMessage('error', SYSTEM_MESSAGES.tripRequestCostZero);
          return;
        }

        if (transportType === TransportTypeEnum.PERSONAL) {
          const car = tripStore.personalCars?.find(
            el => (el.id as string) === (values.personalCar as unknown as string)
          );

          if (Object.keys(car.documents).length === 0) {
            notificationService.error('Ошибка загрузки', 'Документы на автомобиль не найдены', {
              url: `/client/passengers/vehicles/${car.id}`,
              text: 'Добавить документы в личном кабинете',
            });

            return;
          }

          if (!car.documents.passportTs || car.documents.passportTs.engineVolume < 500) {
            notificationService.error('Ошибка', 'Указаны неверные данные', {
              url: `/client/passengers/vehicles/${car.id}`,
              text: 'Скорректируйте данные в личном кабинете',
            });

            return;
          }
        }

        await tripStore.saveTripRequest(JSON.parse(JSON.stringify(newRequestObject)) as any);
        purposeStore.setPurpose({ purposeId: '', isValid: false });
        tripStore.setGroupTransferInformation(null);
        return;
      }

      if (isExternal) {
        // hack for back
        await tripStore.getExternalPrices({
          ...(requestObject as TTripRequestNew),
          purpose: {
            label: tripPurposeList?.purposes[0]?.label ?? '',
            id: tripPurposeList?.purposes[0]?.id ?? '',
          },
        });
        return;
      }

      if (
        geo.calculatedRoute
        // && limitsStore.currentLimit Излишне! Требует запроса всех лимитов!
      ) {
        if (requestObject.expected.cost === 0) {
          logger.toMessage('error', SYSTEM_MESSAGES.tripRequestCostZero);
          return;
        }

        await tripStore.saveTripRequest(requestObject as TTripRequestNew);
        purposeStore.setPurpose({ purposeId: '', isValid: false });
      }
    });
  };

  const onValuesChange = useCallback(
    (changedValues: FormValues, allValues: FormValues): void => {
      if (allValues.when !== 'notnow') {
        form.resetFields(['date']);
      }

      if ('waypoints' in changedValues) {
        /* changedValues.waypoints возвращает массив с изменёнными значениями вида [empty, empty, 'value']
            поскольку empty не отличим от undefined Array.prototype.findIndex здесь не работает
            изменившееся значение в массиве всегда одно, поэтому достаточно получить первый существующий ключ массива */

        tripStore.clearClassCosts();

        const index = Number(Object.keys(changedValues.waypoints)[0]);
        const { waypoint, waitTime } = changedValues.waypoints[index];

        if (waypoint !== undefined) {
          geo.editWaypointAddress(index, waypoint, mapCenter || undefined);
        }
        if (waitTime !== undefined) {
          geo.editWaypointWaitTime(index, waitTime);
        }

        setNumberOfWaypoints(changedValues.waypoints.length);
      }
    },
    [form, geo, tripStore, mapCenter]
  );

  const getEmployeeInitVal = useCallback((): EmployeeModel => selfStore.selfEmployee, [selfStore.selfEmployee]);

  const getInitialValues: () => TripFieldsType = useCallback(
    () => {
      const request = tripStore.currentTripRequest;
      let purpose;
      const getPurposeFromList = (purposeId: string | undefined): TripPurpose | undefined => (tripStore.purposes || []).find(({ id }) => purposeId === id);

      if (requestExist) {
        purpose = getPurposeFromList(request?.purpose.id) ? request?.purpose.id : undefined;
      }

      if (defaultPurpose.data.id) {
        purpose = getPurposeFromList(defaultPurpose.data.id) ? defaultPurpose.data.id : undefined;
      }

      if (purposeStore.purpose.purposeId) {
        purpose = getPurposeFromList(purposeStore.purpose.purposeId) ? purposeStore.purpose.purposeId : undefined;
      }

      return {
        employee: getEmployeeInitVal(),
        coopTrip: true,
        when: 'now',
        waypoints: geo.waypoints.map(x => ({ waypoint: x.addressString, waitTime: x.waitTimeMinutes })),
        passenger: 'me',
        taxiClass:
          (requestExist && request?.taxiClass && TaxiClassEnum[request?.taxiClass])
          || (requestExist && request?.transportType && TransportTypeEnum[request?.transportType])
          || undefined,
        passengerCount: 1,
        date: undefined,
        purpose,
        commentary: undefined,
        preferences: [],
        busRentDuration: 1,
        busClass: undefined,
      };
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [geo, getEmployeeInitVal]
  );

  const getGeolocation = (): void => {
    geo.setCurrentAddressByCoordinates(position);
  };

  useEffect(() => {
    // FIXME sonarjs/cognitive-complexity
    const updateFormWaypoints = (): void => {
      form.setFieldsValue({
        waypoints: geo.waypoints.map(x => ({ waypoint: x.addressString, waitTime: x.waitTimeMinutes })),
      });
    };

    const setFromDisposer = reaction(
      () => geo.waypoints,
      () => {
        updateFormWaypoints();
      }
    );

    const setEmployeeDisposer = reaction(
      () => employeeStore.employeeListByOrg,
      () => {
        form.resetFields();
      }
    );

    const setAutocompleteDisposer = reaction(
      () => geo.addressAutocompleteList,
      () => {
        updateFormWaypoints();
      }
    );

    const refreshLimitsDisposer = reaction(
      () => tripStore.savedSuccessfully,
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      (saved: boolean) => {
        // if (saved) {
        //   limitsStore.refreshLimits();
        // }
      }
    );

    autorun(r => {
      const request = tripStore.currentTripRequest;
      const passenger = request?.passenger.id === selfStore.selfEmployee.id ? 'me' : 'notme';

      const preferences: string[] = request?.requestOptions ? request?.requestOptions : [];

      if (request?.commentForDriver) {
        const commentary = request?.commentForDriver;
        if (!commentary) {
          form.resetFields(['commentary']);
        }
        preferences.push('commentary');
      }

      if (form.getFieldValue('when') !== 'notnow') {
        form.resetFields(['date']);
      }

      if (request?.isExisting) {
        const personalCar = request.transportType === TransportTypeEnum.PERSONAL ? request.personalCar : null;
        form.setFieldsValue({
          coopTrip: request.coopTrip,
          when: 'notnow',
          date: moment(request.desiredDate),
          passenger,
          employee: request.passenger.fullNameWithCode,
          taxiClass:
            (request.taxiClass && TaxiClassEnum[request.taxiClass])
            || (request.transportType && TransportTypeEnum[request.transportType]),
          personalCar,
          passengerCount: 1,
          purpose: request.purpose.id,
          preferences,
          commentary: request?.commentForDriver || '',
        });
        geo.loadFromTripRequest(request);
      } else {
        form.setFieldsValue(getInitialValues());
      }
      r.dispose();
    });

    return (): void => {
      setEmployeeDisposer();
      setFromDisposer();
      setAutocompleteDisposer();
      refreshLimitsDisposer();
      tripStore.clearCurrentRequest();
      geo.clearCurrentState();
      form.resetFields(Object.keys(getInitialValues()));
    };

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [employeeStore, tripStore, geo, form, limitsStore, selfStore.selfEmployee.id, getInitialValues]);
  // FIXME react-hooks/exhaustive-deps

  useEffect(() => {
    const route = geo.calculatedRoute;
    setHasRoute(!!route);
    if (route) {
      const data = {
        distance: route.distance,
        time: route.time,
        tripDate: (form.getFieldValue('date')?.unix() ?? moment().unix()) * 1000,
        startPoint: { ...route.waypoints[0] },
        organizationId: selfStore.orgId,
        employeeId: selfStore.empId,
        waitingTime: route.waypoints[0].waitTime,
        timeZone: getTimeZone(),
        waypoints: route.waypoints,
      };
      setIsCalculating(true);
      tripStore.getCosts(data).then(() => setIsCalculating(false));
      // getPersonalCarsCosts(data, personalCars);  // comment out personal car individual tariffs
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [geo.calculatedRoute]);

  return {
    initialValues: getInitialValues(),
    getGeolocation,
    onValuesChange,
    onFinish,
    form,
    purposes: tripStore.purposes,
    tariffsInfo: tripStore.classCosts,
    saved: tripStore.savedSuccessfully,
    requestExist,
    hasNoRoute: !isCalculating && numberOfWaypoints > 1 && !hasRoute,
    isCalculating: isCalculating,
  };
};

export type useCreateRequestType = ReturnType<typeof useCreateTripRequest> & Record<string, any>;
