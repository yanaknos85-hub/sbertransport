import { useCallback, useEffect } from 'react';
import { FormInstance, useForm } from 'antd/lib/form/Form';
import { autorun, reaction, toJS } from 'mobx';
import moment, { Moment } from 'moment';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useGeoPosition } from 'shared/hooks/usePosition';
import { RouteModel } from 'shared/models/geo/Route.model';

import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  ITripTariff, TaxiClassEnum, TripPurpose,
  TTripRequestNew
} from 'stores/Trip/Trip.interface';
import { taxiClassIsClassic } from 'utils/trips';

import { TripPurposeList } from '../../EditTripRequestForm/hooks/useTripPurposeList';
import { TransportTypesConfig } from '../../TaxiClasses/TaxiClassesConfig';
import { FormValues } from '../types/types';
import { getTimeZone } from '../utils/utils';
import { usePersonalCars } from './usePersonalCars';
import { usePersonalCarsCosts } from './usePersonalCarsCosts';

export type TripFieldsType = Record<string, any>;

interface CreateTripeRequestHook {
  initialValues: TripFieldsType;
  getGeolocation: () => void;
  onValuesChange: (changedValues: FormValues, allValues: FormValues) => void;
  onFinish: (values: FormValues, isExternal?: boolean) => void;
  form: FormInstance;
  purposes: TripPurpose[];
  tariffsInfo: ITripTariff[];
  saved: boolean;
  requestExist: boolean;
}

export const useCreateTripRequest = ({
  tripPurposeList,
}: {
  tripPurposeList?: TripPurposeList;
}): CreateTripeRequestHook => {
  const [form] = useForm();

  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.limitsStore]: limitsStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const { position } = useGeoPosition();
  const { getPersonalCarsCosts } = usePersonalCarsCosts();
  const { personalCars } = usePersonalCars();
  const requestExist = !!tripStore.currentTripRequest?.isExisting;

  const onFinish = (values: FormValues, isExternal?: boolean): void => {
    const taxiClassNormalized = values.taxiClass?.split('-')[0] as TaxiClassEnum;
    const isClassicTaxi = taxiClassIsClassic(taxiClassNormalized);

    if (isClassicTaxi && selfStore.selfEmployee.mobilePhone.length === 0) {
      selfStore.updatePhoneStatus(!selfStore.isRequiredPhone);

      return;
    }

    const date: Moment = form.getFieldValue('date') || moment();
    const passenger
      = values.passenger === 'me'
        ? selfStore.selfEmployee
        : employeeStore.employeeListByOrg.find(x => x.fullNameWithCode === values.employee);

    const requestOptions: string[] = (values.preferences || []).filter((element: string) => element !== 'commentary');
    const taxiClassesEnum = ['ECONOMY', 'COMFORT', 'BUSINESS', 'VIP_BUS', 'SMALL_BUS', 'MIDDLE_BUS', 'LARGE_BUS'];

    const contractorId = tripStore.tariffsTaxi.find(tariff => tariff.id === values.tariffId)?.contractorId;
    const isCarsharing = TransportTypesConfig[taxiClassNormalized]?.transportType === TransportTypeEnum.CARSHARING;
    const isCoopTrip = isCarsharing ? false : values.coopTrip;

    // находим выбранный тариф (tariffId) - забираем его доплату (bonusCost из tripStore)
    const tariffsForBusinessAndComfort = tripStore.tariffsTaxi.filter(
      tariff => tariff.taxiClass === TaxiClassEnum.COMFORT || tariff.taxiClass === TaxiClassEnum.BUSINESS
    );

    const tariffId = tariffsForBusinessAndComfort.find(tariff => tariff.id === values.tariffId)?.tariffId;

    const bonusCostOfSelectedClass = toJS(tripStore.classCosts).find(
      selectedClass => selectedClass.id === tariffId
    )?.bonusCost;

    const requestObject: Record<string, any> = {
      author: selfStore.selfEmployee,
      passenger: passenger ?? selfStore.selfEmployee,
      transportType: taxiClassesEnum.includes(taxiClassNormalized) ? TaxiClassEnum.TAXI : taxiClassNormalized,
      taxiClass: isExternal ? TaxiClassEnum.ECONOMY : taxiClassNormalized,
      passengerCount: values.passengerCount || 1,
      tariffId: values.tariffId,
      desiredDate: date.valueOf(),
      // ToDo: expected: geo.calculatedRoute - поставить вместо существующего когда бонусы будут считаться на беке. Щас бонусы расчитываются на фронте
      expected: { ...geo.calculatedRoute, bonusCost: bonusCostOfSelectedClass },
      purpose: tripPurposeList?.getById(values.purpose),
      approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
      coopTrip: isCoopTrip,
      commentForDriver: values.commentary,
      requestOptions,
      timeZone: getTimeZone(),
      ...(contractorId && { contractorId }),
    };
    if (
      !taxiClassesEnum.includes(taxiClassNormalized)
      && geo.calculatedRoute
      && limitsStore.currentLimit
      && !isExternal
    ) {
      // если это не такси
      const newRequestObject = {
        author: selfStore.selfEmployee,
        passenger: passenger ?? selfStore.selfEmployee,
        transportType: TransportTypesConfig[taxiClassNormalized]?.transportType,
        passengerCount: values.passengerCount || 1,
        tariffId: values.tariffId,
        desiredDate: date.valueOf(),
        expected: new RouteModel({
          ...(geo.calculatedRoute as RouteModel),
          cost: tripStore.personalCarsCosts.find(el => el?.id === values.personalCar?.id)?.cost,
        }),
        purpose: tripPurposeList?.getById(values.purpose),
        approvedBy: employeeStore.employeeListByOrgMapped[selfStore.selfEmployee.supervisorId],
        coopTrip: isCoopTrip,
        personalCarId: (values.personalCar && values.personalCar.id) || '',
        commentForDriver: values.commentary,
        requestOptions,
        taxiClass: undefined,
        timeZone: getTimeZone(),
      };
      tripStore.saveTripRequest(JSON.parse(JSON.stringify(newRequestObject)) as any);
      return;
    }

    if (isExternal) {
      // hack for back
      tripStore.getExternalPrices({
        ...(requestObject as TTripRequestNew),
        purpose: {
          label: tripPurposeList?.purposes[0]?.label ?? '',
          id: tripPurposeList?.purposes[0]?.id ?? '',
        },
      });
      return;
    }

    if (geo.calculatedRoute && limitsStore.currentLimit) {
      tripStore.saveTripRequest(requestObject as TTripRequestNew);
    }
  };

  const onValuesChange = (changedValues: FormValues, allValues: FormValues): void => {
    if (allValues.when !== 'notnow') {
      form.resetFields(['date']);
    }

    const commentary = allValues.preferences?.includes('commentary');

    if (!commentary) {
      form.resetFields(['commentary']);
    }

    if ('waypoints' in changedValues) {
      /* changedValues.waypoints возвращает массив с изменёнными значениями вида [empty, empty, 'value']
            поскольку empty не отличим от undefined Array.prototype.findIndex здесь не работает
            изменившееся значение в массиве всегда одно, поэтому достаточно получить первый существующий ключ массива */

      tripStore.clearClassCosts();

      const index = Number(Object.keys(changedValues.waypoints)[0]);
      const { waypoint, waitTime } = changedValues.waypoints[index];

      if (waypoint !== undefined) {
        geo.editWaypointAddress(index, waypoint);
      }
      if (waitTime !== undefined) {
        geo.editWaypointWaitTime(index, waitTime);
      }
    }
  };

  const getEmployeeInitVal = useCallback(
    (): string => selfStore.selfEmployee.fullNameWithCode,
    [selfStore.selfEmployee]
  );

  const getInitialValues: () => TripFieldsType = useCallback(
    () => {
      const request = tripStore.currentTripRequest;

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
        purpose: requestExist ? request?.purpose.id : undefined,
        commentary: undefined,
        preferences: [],
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
      (saved: boolean) => {
        if (saved) {
          limitsStore.refreshLimits();
        }
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
    if (route) {
      const data = {
        distance: route.distance,
        time: route.time,
        tripDate: form.getFieldValue('date')?.unix() ?? moment().unix(),
        startPoint: { ...route.waypoints[0] },
        organizationId: selfStore.orgId,
        waitingTime: route.waypoints[0].waitTime,
        intermediateWaitingTime: route.waypoints.reduce((acc, value) => acc + value.waitTime, 0),
      };
      tripStore.getCosts(data);
      getPersonalCarsCosts(data, personalCars);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [geo.calculatedRoute, personalCars]);
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
  };
};

export type useCreateRequestType = ReturnType<typeof useCreateTripRequest> & Record<string, any>;
