/* eslint-disable no-unused-expressions */
import '../styles/override.scss';

import {
  Checkbox, Collapse, Form, Input, Switch
} from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import moment, { Moment } from 'moment';
import React, {
  Suspense, useEffect, useMemo, useReducer, useRef, useState
} from 'react';
import { Redirect, useRouteMatch } from 'react-router-dom';
import { UUID } from 'utils/io-ts';

import { useGetFrequentlyPurpose } from 'api/purposes';
import * as routes from 'constants/constants.routes';
import {
  EmployeeAppShortWayLinks,
  EmployeeAppShortWaySpecialLinks,
  MoscowLatLng
} from 'modules/EmployeeApp/EmployeeApp.constants';
import { SpinWrapped } from 'shared/components';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import BackArrow from 'shared/components/Images/direction/back.svg';
import { ReactComponent as PlusIcon } from 'shared/components/Images/plusIcon.svg';
import { CenterCoordinates, Coordinates } from 'shared/components/Map/MapComponent.types';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';
import { StoreNames } from 'stores/StoreNames.enum';

import { TransportTypeEnum, TransportTypeHeaderTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  TaxiClassEnum, TaxiEnum, TripPurpose, TTaxiClass, YandexTripRequest
} from 'stores/Trip/Trip.interface';
import { checkingValueObjectNotNull } from 'utils/checkingValueObjectNotNull';
import { TripRequestDate, TripRequestPurpose } from '../../EditTripRequestForm/components';
import { useTripRequestDisable, useTripRequestPurpose } from '../../EditTripRequestForm/hooks';
import { useTripPurposeList } from '../../EditTripRequestForm/hooks/useTripPurposeList';
import TaxiCreator from '../../TransportOrder/Creators/TaxiCreator';
import { useDeepLinks } from '../hooks/use_DeepLinks';
import { useCooperativeTrip } from '../hooks/useCooperativeTrip';
import { useCreateTripRequest } from '../hooks/useCreateTripRequest';
import { Back, ButtonWrapper, ItemNewDesign } from '../styles/styled';
import {
  getTimeZone,
  updateFormValuesBySelectedTransport,
  validityDateByPurpose
} from '../utils/utils';
import { Additional } from './Additional/Additional';
import { BusProperties } from './BusProperties';
import ExternalPrices from './ExternalPrices2/ExternalPrices';
import { MapRenderer } from './MapRenderer';
import { PersonalCars } from './PersonalCars2/PersonalCars';
import { PhoneModal } from './PhoneModal/PhoneModal';
import { TransportTypes } from './TransportTypes';
import { Waypoints } from './Waypoints';

import BicycleCreator from '../../TransportOrder/Creators/BicycleCreator';
import BusCreator from '../../TransportOrder/Creators/BusCreator';
import CarsharingCreator from '../../TransportOrder/Creators/CarsharingCreator';
import PersonalCreator from '../../TransportOrder/Creators/PersonalCreator';
import PublicCreator from '../../TransportOrder/Creators/PublicCreator';
import { FormValues, IProduct } from '../../TransportOrder/types';

import { useGeoPosition } from 'shared/hooks/usePosition';

import { AdditionalPassenger } from './Additional/AdditionalPassenger';
import { AdditionalPreferences } from './Additional/AdditionalPreferences';

import { useGetApprovalsPublic } from 'api/trip-requests';
import { TModal } from 'shared/ui/Modal/Modal';
import Process from '../../Evaluation/Constants/Process';
import { useTaxiTariffs } from '../../TaxiClasses2/hooks';
import TransferCreator from '../../TransportOrder/Creators/TransferCreator';
import { useAvalailableForSharing } from '../hooks/useAvailableForSharing';
import { useGroupTransferTrip } from '../hooks/useGroupTransferTrip';
import { usePersonalCars } from '../hooks/usePersonalCars';
import { usePublicTrip } from '../hooks/usePublicTrip';
import { GroupTransfer } from './GroupTransfer/GroupTransfer';
import { GroupTransferChoosingBookingInterval } from './GroupTransferChoosingBookingInterval/GroupTransferChoosingBookingInterval';
import { GroupTransferChoosingCar } from './GroupTransferChoosingCar/GroupTransferChoosingCar';
import { MassModal } from './MassModal/MassModal';
import { PublicCars } from './PublicCars/PublicCars';
import SRMMassPreviewMultiple from './SRMMassPreviewMultiple/SRMMassPreviewMultiple';

import Alert from '../../Alert/Alert';
import YandexTaxiCreator from '../../TransportOrder/Creators/YandexTaxiCreator';
import { useCheckTripSplit } from '../hooks/fraud/useCheckTripSplit';
import styles from '../styles/create.module.scss';
import { CreateYandexApplyDrawer } from './CreateYandexApplyDrawer/CreateYandexApplyDrawer';
import { SelfPaymentYandex } from './SelfPaymentYandex/SelfPaymentYandex';
import { useCheckAbsence } from '../hooks/fraud/useCheckAbsence';

const { Panel } = Collapse;

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const TransportOrder: React.FC<{ transportType: TransportTypeEnum }> = observer(({ transportType }) => {
  const {
    [StoreNames.geoStore]: geo,
    [StoreNames.tripStore]: tripStore,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.employeeStore]: empStore,
    [StoreNames.corporateStore]: corporateStore,
    [StoreNames.addressStore]: addressStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
    [StoreNames.purposeStore]: purposeStore,
  } = useAppStoreContext();

  const { position, error: positionError } = useGeoPosition();
  const [mapIsCentered, setMapCenter] = useState(false);
  const match = useRouteMatch<{ reqId: UUID }>();
  const [selectedQuantity, setSelectedQuantity] = useState('1');
  const [visibleCommentaryForPurpose, setVisibleCommentaryForPurpose] = useState(false);

  const { selfEmployee } = selfStore;
  /**
   * publicApprovals - "настройки согласований" можно менять из корп. клиента. Привязаны к organizationId.
   * Сейчас можно создавать заявку только сотрудникам одной организации.
   * Если появится возможность создавать завку сотрудникам другой организации, нужно будет доставать
   * organizationId организации, сотруднику которой создаётся поездка
   */
  const { data: publicApprovals } = useGetApprovalsPublic(selfEmployee.organizationId);

  useEffect(() => {
    // 100 раз подумай прежде чем открывать этот запрос!
    // В мейне он и так дергается 1 раз при инициализации. Как минимум можно брать оттуда оставив это условие, если очень надо
    // if(!limitsStore.limitsIsLoaded || true) {
    //   limitsStore.getLimits()
    // }
    tripStore.loadCoopTripsSettings(selfStore.orgId); // нужно чтобы цветные плашки рисовать
    tripStore.loadPurposesList(selfStore.orgId); // нужно чтобы был список целей поездок для сохранения
    addressStore.loadFavoriteList();
    addressStore.loadFrequentList();
    transportTypesStore.getAvailableTransportTypes();
    tripStore.clearFraud();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    // eslint-disable-next-line @typescript-eslint/no-shadow
    const doCenter = (position: [number, number]) => {
      setTimeout(() => {
        geo.setCurrentCoordinates(position);
        setMapCenter(true);
      }, 100);
    };

    // Если геолокация не определеляется - берем из недавних адресов
    if (!mapIsCentered && positionError && addressStore.selfFrequentList.length) {
      const { latitude, longitude } = addressStore.selfFrequentList[0];
      doCenter([latitude, longitude]);

      // Или определяем по браузеру
    } else if (!mapIsCentered && position.join() !== MoscowLatLng.join()) {
      doCenter(position);
    }
    // Или оставляем центр Москвы по умолчанию
  }, [position, positionError, addressStore.selfFrequentList, mapIsCentered, geo]);

  const [product, setProduct] = useReducer((state: IProduct, action: TransportTypeEnum) => {
    switch (action) {
      case TransportTypeEnum.TAXI:
        return new TaxiCreator().factoryMethod(state);
      case TransportTypeEnum.PERSONAL:
        return new PersonalCreator().factoryMethod(state);
      case TransportTypeEnum.PUBLIC:
        return new PublicCreator().factoryMethod(state);
      case TransportTypeEnum.GROUP_TRANSFER:
        return new TransferCreator().factoryMethod(state);
      case TransportTypeEnum.CARSHARING:
        return new CarsharingCreator().factoryMethod(state);
      case TransportTypeEnum.BUS:
        return new BusCreator().factoryMethod(state);
      case TransportTypeEnum.BICYCLE:
        return new BicycleCreator().factoryMethod(state);
      case TransportTypeEnum.YANDEX:
        return new YandexTaxiCreator().factoryMethod(state);
      default:
        return new TaxiCreator().factoryMethod(state);
    }
  }, new TaxiCreator().factoryMethod());

  useEffect(() => {
    setProduct(transportType);
  }, [transportType]);

  const mapCenterRef = useRef<CenterCoordinates>();

  const {
    step,
    isExternal,
    personalCar,
    isCreating,
    busCreateEnabled = true,
    currentAddressInput = 0,
    isExternalPriceLoading,
    transportType: productTransportType,
    subClass,
    waypoints,
    externalProvider,

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    setStep = () => {},
    setExternal,
    setPersonalCar,
    setBusCreateEnabled,
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    setCurrentAddressInput = () => {},
    setExternalPriceLoading,
    onFinish: onCommonFinish,
    setSubClass,
    setExternalProvider,
  } = product || {};

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const methodProps = {
    setStep,
  };

  const tripPurposeList = useTripPurposeList(tripStore.currentTripRequest);
  const { isAvailableForSharing } = useAvalailableForSharing(productTransportType);
  const [isYandexDrawerOpen, setYandexDrawerOpen] = useState(false);

  const [visibleGroupTransferForm, setVisiblevisibleGroupTransferForm] = useState(false);
  const [visibleGroupTransferChoosingCar, setVisibleGroupTransferChoosingCar] = useState(false);
  const [visibleGroupTransferChoosingBookingInterval, setVisibleGroupTransferChoosingBookingInterval] = useState(false);
  const [prevWhenValue, setPrevWhenValue] = useState<string | undefined>();

  const { purposes } = tripPurposeList;

  const availableTaxiClasses = corporateStore.positionsMapped[selfStore.posId]?.availableClasses || [];

  const isSecondStep = step === 2;
  const isThirdStep = step === 3;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const isFifthStep = step === 5;

  let doubleStep = 0;

  if (!isAvailableForSharing && (isThirdStep || isFifthStep)) {
    doubleStep = 1;
  }

  const {
    initialValues, form, tariffsInfo, saved, onValuesChange, onFinish, requestExist, hasNoRoute, isCalculating,
  }
    = useCreateTripRequest({
      tripPurposeList,
      mapCenter: mapCenterRef.current,
      availableTaxiClasses,
    });

  const isYandexTaxi = productTransportType === TransportTypeEnum.YANDEX;

  const onFinishYandexTaxi = async (values: FormValues) => {
    await form.validateFields();
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
    }
  };

  const submitYandexOrder = async () => {
    await onCommonFinish(
      onFinishYandexTaxi,
      {
        waypoints,
        ...form.getFieldsValue(),
        taxiClass: subClass,
        tariffId: tariff?.id,
      },
      externalLinks,
      applyCoopTrip,
      tariffsInfo,
      coopTripData,
      step,
      undefined
    );
  };

  const createYandexTaxiOrder = () => {
    if (!isYandexTaxi) return;

    form.validateFields().then(() => {
      setYandexDrawerOpen(true);
    });
  };

  const {
    childForm, isSaveCompensation, setIsSaveCompensation, okHandler, generalSum, setGeneralSum,
  }
    = usePublicTrip(form);

  const { groupTransferForm } = useGroupTransferTrip();

  useEffect(() => {
    // а так мы пытаемся избавится от antd, пока частично :)
    form.setFieldsValue({ transportType: productTransportType });
    form.setFieldsValue({ taxiClass: subClass });
  }, [product.transportType, product.subClass, form, productTransportType, subClass]);

  const defaultPurpose = useGetFrequentlyPurpose();
  const checkPurpose = form.getFieldValue('purpose');
  const tripPurpose = useTripRequestPurpose(
    checkPurpose
      ? checkPurpose
      : purposeStore.purpose.purposeId
        ? purposeStore.purpose.purposeId
        : defaultPurpose.data.id
  );
  const coopTripData = useCooperativeTrip({ tripPurposeList });
  const { purposeValidity } = tripPurpose;

  useEffect(() => {
    const checkWhen = form.getFieldValue('when');
    checkWhen !== 'now' && form.setFieldsValue({ when: 'notnow' });
    const getPurposeFromList = (purposeId: string | undefined): TripPurpose | undefined => (tripStore.purposes || []).find(({ id }) => purposeId === id);
    const isPurpose = form.getFieldValue('purpose');
    !isPurpose
    && form.setFieldsValue({ purpose: getPurposeFromList(defaultPurpose.data.id) ? defaultPurpose.data.id : undefined });
  }, [defaultPurpose.data.id, form, tripStore.purposes]);

  const personalTransport = form.getFieldValue('personalCar');
  const isPersonalTransport = !personalTransport && productTransportType === TransportTypeEnum.PERSONAL;

  const { applyCoopTrip } = coopTripData;

  const expected = geo.calculatedRoute;
  const from = expected?.segments[0].coordinates[0];
  const to
    = expected?.segments[expected?.segments.length - 2].coordinates[
      expected?.segments[expected?.segments.length - 2].coordinates.length - 1
    ];

  const externalLinks = useDeepLinks(externalProvider, subClass as TaxiEnum | undefined, from, to);

  const { disabled, isNoPersonalCars } = useTripRequestDisable({
    transport: productTransportType === TransportTypeEnum.YANDEX ? TransportTypeEnum.TAXI : productTransportType,
    subClass,
    tripPurpose,
    tariffsCosts: tripStore?.classCosts,
    request: tripStore.currentTripRequest,
    isExternal,
    from,
    to,
  });

  const loadExternalPrices = async () => {
    setStep(2);

    if (isExternal && setExternalPriceLoading) {
      const updatedData = updateFormValuesBySelectedTransport(form.getFieldsValue(), tariffsInfo);
      setExternalPriceLoading(true);
      await onFinish(updatedData, true);
      setExternalPriceLoading(false);
    }
  };

  const { passenger, employee } = form.getFieldsValue();
  const [personalCarsEmployee, setPersonalCarsEmployee] = useState();
  const [onRefetchPersonalCars, setOnRefetchPersonalCars] = useState(false);
  const { personalCars, refetch: refetchPersonalCars } = usePersonalCars(
    passenger === 'notme'
      ? personalCarsEmployee
        ? personalCarsEmployee
        : employee
      : tripStore.currentTripRequest?.author,
    tariffsInfo
  );

  useMemo(() => {
    tripStore.setPersonalCars(personalCars);
  }, [personalCars]);

  const onCenterChanged = (center: Coordinates) => {
    mapCenterRef.current = [center.latitude, center.longitude];
  };

  const handleExternalChange = (data: boolean) => {
    if (data) {
      // Включаем "Поездка в личных целях" - сохраняем текущее значение when и переводим его на notnow
      setPrevWhenValue(form.getFieldValue('when'));
      form.setFieldsValue({ when: 'notnow' });
    } else if (prevWhenValue) {
      // Выключаем "Поездка в личных целях" - восстанавливаем предыдущее значение when
      form.setFieldsValue({ when: prevWhenValue });
      setPrevWhenValue(undefined);
    }

    setExternal && setExternal(data);
    form.setFieldsValue({
      taxiClass: '',
      coopTrip: !data,
    });
  };

  const isSecondAvailable
    = step === 1
    && geo.calculatedRoute?.distance
    && (form.getFieldValue('purpose') || defaultPurpose.data.id || isExternal)
    && (form.getFieldValue('when') === 'now'
    || (form.getFieldValue('when') === 'notnow' && !form.getFieldError('date').length));
  const isSecondAvailableForNow
    = step === 1
    && geo.calculatedRoute?.distance
    && (form.getFieldValue('purpose') || defaultPurpose.data.id || isExternal);
  const isThirdAvailable = step === 2;

  const { tariff }
    = useTaxiTariffs({
      tariffsInfo,
      option: {
        transportType: productTransportType ?? TransportTypeEnum.TAXI,
        taxiClass: subClass,
        name: '',
        waitingTime: 0,
      },
    }) || {};

  const {
    isChecking: isTripSplitChecking,
    check: checkTripSplit,
    ModalElement: TripSplitModal,
  } = useCheckTripSplit(form);

  const {
    isAbsenceConflict,
    AbsenceConflictAlert,
    AbsenceWarningModal,
    isChecking: isAbsenceChecking,
    check: checkAbsence,
    onFormValuesChange: handleValuesChangedForAbsence,
  } = useCheckAbsence(form, isExternal);

  const handleNextStep = (taxiClass?: TTaxiClass) => {
    (async () => {
      if (step === 1) {
        try {
          await checkAbsence();
        } catch {
          return;
        }
      }

      if (step === 1) {
        const route = geo.calculatedRoute;

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

          await tripStore.getCosts(data);
        }
      }

      if (tripStore.classCosts.length === 0) {
        setCheckingTariffsActive(true);
        setCheckingProcessTariffs(Process.START);
        return;
      }

      if (productTransportType === TransportTypeEnum.GROUP_TRANSFER && step === 3) {
        taxiClass !== 'TRANSFER_CAR_CHOICE'
          ? setVisiblevisibleGroupTransferForm(true)
          : visibleGroupTransferChoosingCar
            ? setVisiblevisibleGroupTransferForm(true)
            : setVisibleGroupTransferChoosingCar(true);

        if (taxiClass !== 'TRANSFER_CAR_CHOICE') {
          setVisiblevisibleGroupTransferForm(true);
        } else {
          if (visibleGroupTransferChoosingCar) {
            return setVisiblevisibleGroupTransferForm(true);
          }
          setVisibleGroupTransferChoosingCar(true);
        }

        setStep(step + 1);
        return;
      }

      if (step === 1 && EmployeeAppShortWayLinks.some(el => match.url.includes(el))) {
        if (EmployeeAppShortWaySpecialLinks.some(el => match.url.includes(el))) {
          setSubClass(
            transportType === TransportTypeEnum.GROUP_TRANSFER
              ? (TransportTypeEnum.TRANSFER as unknown as TaxiEnum)
              : (transportType as unknown as TaxiEnum)
          );
        }

        return setStep(step + 2);
      }

      if (form.getFieldValue('when') === 'now' && step === 1) {
        form.setFieldsValue({ when: 'now' });
        return setStep(2);
      }

      return isThirdStep ? setStep(step + 1 + doubleStep) : setStep(step + 1);
    })();
  };

  const handlePreviousStep = () => {
    tripStore.clearFraud();
    if (step === 2) {
      setProduct(transportType);
    }

    if (step === 3 && EmployeeAppShortWayLinks.some(el => match.url.includes(el))) {
      return setStep(step - 2);
    }

    if (
      step === 4
      && (visibleGroupTransferForm || visibleGroupTransferChoosingCar || visibleGroupTransferChoosingBookingInterval)
    ) {
      if (visibleGroupTransferChoosingBookingInterval) {
        setVisibleGroupTransferChoosingBookingInterval(false);
        return setVisibleGroupTransferChoosingCar(true);
      }
      if (visibleGroupTransferForm && tripStore.availableDispatcherTransport) {
        setVisiblevisibleGroupTransferForm(false);
        return setVisibleGroupTransferChoosingCar(true);
      }
      setVisiblevisibleGroupTransferForm(false);
      setVisibleGroupTransferChoosingCar(false);
    }

    return isFifthStep ? setStep(step - 1 - doubleStep) : setStep(step - 1);
  };

  const createOrder = (data?: FormValues, isTripSearching?: boolean, isSuitabelTrip?: boolean) => {
    (async () => {
      if (productTransportType === TransportTypeEnum.PERSONAL && !isSuitabelTrip) {
        try {
          await checkTripSplit();
        } catch {
          return;
        }
      }

      if (productTransportType !== TransportTypeEnum.PUBLIC) {
        if (productTransportType === TransportTypeEnum.GROUP_TRANSFER) {
          const formData = {
            ...form.getFieldsValue(),
            ...groupTransferForm.getFieldsValue(),
            groupTransferClass: subClass,
          };

          groupTransferForm.validateFields().then(() => {
            onCommonFinish
            && onCommonFinish(
              onFinish,
              {
                waypoints,
                ...formData,
                taxiClass: subClass,
                tariffId: tariff?.id,
              },
              externalLinks,
              applyCoopTrip,
              tariffsInfo,
              coopTripData,
              step,
              isSuitabelTrip
            );
          });
        } else {
          onCommonFinish
          && onCommonFinish(
            onFinish,
            {
              waypoints,
              ...form.getFieldsValue(),
              taxiClass: subClass,
              tariffId: tariff?.id,
            },
            externalLinks,
            applyCoopTrip,
            tariffsInfo,
            coopTripData,
            step,
            isSuitabelTrip
          );
        }
      } else {
        okHandler();
      }
    })();
  };

  const isPickedTaxiOnThirdStep
    = step === 3
    && (productTransportType === TransportTypeEnum.TAXI
    || productTransportType === TransportTypeEnum.PERSONAL
    || productTransportType === TransportTypeEnum.CARSHARING
    || productTransportType === TransportTypeEnum.PUBLIC
    || productTransportType === TransportTypeEnum.BUS
    || productTransportType === TransportTypeEnum.YANDEX);

  const selectedCounterpartyTariff = checkingValueObjectNotNull(externalLinks);

  const [process, setProcess] = useState<Process>(Process.CLOSE);
  const [checkingProcessTariffs, setCheckingProcessTariffs] = useState<Process>(Process.CLOSE);
  const [checkingTariffsActive, setCheckingTariffsActive] = useState(false);

  useEffect(() => {
    setCheckingTariffsActive(false);
  }, [tariffsInfo]);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const disableNextStep
    = (step === 1 && !isSecondAvailable) || (step === 2 && !isThirdAvailable) || !purposeValidity?.isValid;

  const handleCarsharingConfirm = () => {
    setProcess(Process.CLOSE);
    handleNextStep();
    return Promise.resolve(200);
  };

  const handleCarsharingClose = () => {
    setProcess(Process.CLOSE);
  };

  const handleTariffsConfirm = () => {
    setCheckingProcessTariffs(Process.CLOSE);
    return Promise.resolve(200);
  };

  const handleTariffsClose = () => {
    setCheckingProcessTariffs(Process.CLOSE);
  };

  const disabledNextButton
    = (step === 1 && !isSecondAvailable)
    || (step === 2 && !isThirdAvailable)
    || !purposeValidity?.isValid
    || !checkPurpose
    || checkingTariffsActive
    || isCalculating
    || isAbsenceChecking
    || isAbsenceConflict;

  const disabledSubmit
    = disabled
    || isNoPersonalCars
    || isCreating
    || !busCreateEnabled
    || isPersonalTransport
    || tripStore.progressApplicationCreationRequest
    || isSaveCompensation
    || isTripSplitChecking
    || tripStore.isSaveFile;

  useEffect(() => {
    if (purposeStore.purpose.purposeId) {
      form.setFieldsValue({ purpose: purposeStore.purpose.purposeId });
    }

    return () => {
      tripStore.clearYandexRequest();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const [passengerCount, setPassengerCount] = useState(0);

  useEffect(() => {
    setPassengerCount(form.getFieldValue('passengerCount') || 0);
  }, [form.getFieldValue('passengerCount')]);

  const validityDateGoNow = validityDateByPurpose(purposes, purposeValidity.purposeId);
  const disabledNextButtonGoingNow
    = validityDateGoNow
    || !isSecondAvailableForNow
    || !purposeValidity?.isValid
    || !isSecondAvailable
    || !checkPurpose
    || checkingTariffsActive
    || isCalculating
    || isAbsenceChecking
    || isAbsenceConflict;

  useEffect(() => {
    if (tripStore.groupTransferInformation) {
      setVisibleCommentaryForPurpose(true);
      form.setFieldsValue({
        date: tripStore.groupTransferInformation.choosingTime,
        commentForPurpose: tripStore.groupTransferInformation.commentForPurpose,
      });
    }
  }, []);

  // const userDepId = selfStore.depId;
  // const userDep = corporateStore.getDepartment(userDepId);
  // const currentUserId = selfStore.empId;
  // const isDepartmentHead = (): boolean => userDep?.departmentHeadId === currentUserId;
  const [showSRMMassModal, setShowSRMMassModal] = useState(false);
  const [isVisible, setVisible] = useState(false);

  // const handleOpenModal = () => {
  //   setVisible(true);
  // };

  const handleCloseModal = () => {
    setVisible(false);
  };

  useEffect(() => {
    refetchPersonalCars();
  }, [onRefetchPersonalCars]);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const handleValuesChangePersonalCars = (changedValues, allValues) => {
    if ('employee' in changedValues) {
      setPersonalCarsEmployee(changedValues.employee);
      setOnRefetchPersonalCars(!onRefetchPersonalCars);
    }
    if ('passenger' in changedValues) {
      if (changedValues.passenger !== 'notme') {
        setOnRefetchPersonalCars(!onRefetchPersonalCars);
      }
    }
  };

  const handleValuesChange = (changedValues, allValues) => {
    onValuesChange(changedValues, allValues);
    handleValuesChangePersonalCars(changedValues, allValues);
    handleValuesChangedForAbsence(changedValues);
  };

  return saved ? (
    <Redirect to={`${routes.TRANSPORT_2_0_SUCCESS}`} />
  ) : (
    <div className={classNames(styles.formWrapper, 'transport-order')}>
      <Form
        layout="vertical"
        form={form}
        name="createRequest"
        size="middle"
        initialValues={initialValues}
        onValuesChange={handleValuesChange}
        style={{
          height: 'inherit',
          position: 'relative',
          display: 'flex',
        }}
      >
        <div className={classNames(styles.mapWrapper)}>
          <MapRenderer geo={geo} onCenterChanged={onCenterChanged} />
        </div>

        <div className={classNames(styles.menuWrapper)}>
          {/* {!!isDepartmentHead && ( */}
          {/* <div className={classNames(styles.menuLoader)}> */}
          {/*  <div className={classNames(styles.loaderContainer)}> */}
          {/*    <div className={classNames(styles.loaderHeaderText)}>Перевозка сотрудников</div> */}
          {/*    <button className={classNames(styles.loaderButton)} onClick={handleOpenModal}>Массовые заявки</button> */}
          {/* </div> */}
          {/* </div> */}
          {/* )} */}
          <div className={classNames(styles.menu)}>
            <div>
              {step !== 1 && (
                <Back className="button-back">
                  <TButton
                    icon={<img src={BackArrow} alt="" />}
                    type="text"
                    $size="small"
                    block={true}
                    onClick={() => handlePreviousStep()}
                    $makeLikeLink={true}
                  >
                    {productTransportType && step > 2 ? TransportTypeHeaderTitlesEnum[productTransportType] : 'Назад'}
                  </TButton>
                </Back>
              )}

              {step === 1 && (
                <ItemNewDesign $margin="16px 0 0 0">
                  <Waypoints
                    currentAddressInput={currentAddressInput}
                    setCurrentAddressInput={setCurrentAddressInput}
                    style={{ display: step !== 1 ? 'none' : 'block' }}
                  />

                  {step === 1 && (
                    <>
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
                          className={styles.addAddressButton}
                          block={true}
                          onClick={geo.addWaypoint}
                          $makeLikeLink={true}
                        >
                          Добавить адрес
                        </TButton>
                      )}

                      {hasNoRoute && (
                        <Alert
                          className={styles.noRoute}
                          type="error"
                          showIcon
                          message="Адрес не найден"
                          description="Введите полный адрес"
                        />
                      )}
                    </>
                  )}
                </ItemNewDesign>
              )}

              <ItemNewDesign $padding="16px" style={{ display: step !== 1 ? 'none' : 'block' }}>
                <div style={{ display: 'flex' }}>
                  <Switch
                    defaultChecked={isExternal}
                    checked={isExternal}
                    onChange={handleExternalChange}
                  />

                  <ItemNewDesign $padding="0 0 0 12px" $margin="0px">
                    Поездка в личных целях
                  </ItemNewDesign>
                </div>

                <ItemNewDesign
                  $padding="0px"
                  $margin="0px"
                  style={{ display: step === 1 && !isExternal ? 'block' : 'none' }}
                >
                  <TripRequestPurpose
                    tripPurposeList={tripPurposeList}
                    tripPurpose={tripPurpose}
                    form={form}
                  />
                </ItemNewDesign>

                <ItemNewDesign
                  $padding="0px"
                  $margin="0px"
                  style={{ display: step === 1 && !isExternal ? 'block' : 'none' }}
                >
                  <div className={styles.commentaryForPurpose}>
                    <Checkbox
                      onClick={() => {
                        setVisibleCommentaryForPurpose(value => !value);
                      }}
                      checked={visibleCommentaryForPurpose}
                    />
                    <span>Комментарий к цели</span>
                  </div>
                  {visibleCommentaryForPurpose && (
                    <Form.Item name="commentForPurpose">
                      <Input.TextArea
                        maxLength={250}
                        showCount
                        rows={4}
                        placeholder="Оставьте свой комментарий"
                      />
                    </Form.Item>
                  )}
                </ItemNewDesign>
              </ItemNewDesign>

              <ItemNewDesign
                $padding="0 16px"
                $margin="0 16px"
                style={{ display: step === 1 && !isExternal ? 'block' : 'none' }}
              >
                {!isExternal && (
                  <TripRequestDate
                    purposes={purposes}
                    form={form}
                    disabled={false}
                    tripPurpose={tripPurpose}
                    style={{ margin: '12px 0' }}
                  />
                )}
              </ItemNewDesign>

              <ItemNewDesign
                $padding="16px"
                style={{ display: step === 1 && !isExternal ? 'block' : 'none' }}
                className="additional-passenger"
              >
                <AdditionalPassenger disabled={false} />
              </ItemNewDesign>

              {AbsenceConflictAlert}

              {isThirdStep && productTransportType === TransportTypeEnum.YANDEX && (
                <ItemNewDesign>
                  <SelfPaymentYandex />
                </ItemNewDesign>
              )}

              {!isExternal && (isSecondStep || isThirdStep || step === 4) && (
                <ItemNewDesign>
                  {step === 3
                  && productTransportType !== TransportTypeEnum.PUBLIC
                  && productTransportType !== TransportTypeEnum.GROUP_TRANSFER
                  && productTransportType !== TransportTypeEnum.YANDEX && (
                  <Additional
                    form={form}
                    transport={productTransportType}
                    submitDisabled={disabled}
                    coopTripData={coopTripData}
                    onCommonFinish={createOrder}
                    personalCar={personalCar}
                    isExternal={isExternal}
                    step={step}
                    disabled={false}
                    productTransportType={productTransportType}
                    selectedQuantity={selectedQuantity}
                    setSelectedQuantity={setSelectedQuantity}
                    passengerCount={passengerCount}
                  />
                  )}
                  {((productTransportType !== TransportTypeEnum.PERSONAL
                  && productTransportType !== TransportTypeEnum.PUBLIC
                  // && productTransportType !== TransportTypeEnum.GROUP_TRANSFER
                  && !visibleGroupTransferForm
                  && !visibleGroupTransferChoosingCar
                  && !visibleGroupTransferChoosingBookingInterval)
                  || (step !== 3
                  && !visibleGroupTransferForm
                  && !visibleGroupTransferChoosingCar
                  && !visibleGroupTransferChoosingBookingInterval)) && (
                  <>
                    <ErrorBoundary>
                      <Suspense fallback={<SpinWrapped />}>
                        <TransportTypes
                          tariffsInfo={tariffsInfo}
                          externalPrices={tripStore.externalPrices}
                          request={tripStore.currentTripRequest}
                          form={form}
                          purpose={form.getFieldValue('purpose')}
                          availableTaxiClasses={availableTaxiClasses}
                          isExternal={isExternal}
                          product={product}
                          setProduct={setProduct}
                          setStep={() => setStep(1 + doubleStep)}
                          setSubClass={setSubClass}
                          setProcess={setProcess}
                          loadExternalPrices={loadExternalPrices}
                          handleNextStep={handleNextStep}
                        />
                      </Suspense>
                    </ErrorBoundary>
                    <TModal
                      properties={{
                        title: 'Начать поездку вы можете после согласования заявки',
                        confirmButton: { text: 'Понятно' },
                        declineButton: { text: 'Отменить' },
                        handleConfirm: handleCarsharingConfirm,
                        style: {
                          height: 264,
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
                      <div className={styles.modalContainerCarsharing}>
                        <p className={styles.modalContainerCarsharing__title}>
                          При старте аренды до согласования, поездка оплачивается самостоятельно
                        </p>
                      </div>
                    </TModal>
                  </>
                  )}
                  {!isExternal && step === 3 && productTransportType === TransportTypeEnum.PERSONAL && (
                    <div
                      style={{
                        display:
                          !isExternal && step === 3 && productTransportType === TransportTypeEnum.PERSONAL
                            ? 'block'
                            : 'none',
                      }}
                    >
                      <PersonalCars
                        form={form}
                        tariffsInfo={tariffsInfo}
                        setPersonalCar={setPersonalCar}
                        specificEmployee={tripStore.currentTripRequest?.author}
                      />
                    </div>
                  )}
                  {!isExternal && step === 3 && productTransportType === TransportTypeEnum.PUBLIC && (
                    <Suspense fallback={<SpinWrapped />}>
                      <PublicCars
                        childForm={childForm}
                        publicApprovals={publicApprovals}
                        setIsSaveCompensation={setIsSaveCompensation}
                        setGeneralSum={setGeneralSum}
                        generalSum={generalSum}
                      />
                    </Suspense>
                  )}
                  {!isExternal
                  && visibleGroupTransferForm
                  && productTransportType === TransportTypeEnum.GROUP_TRANSFER && (
                  <GroupTransfer
                    form={form}
                    groupTransferForm={groupTransferForm}
                    tariffId={tariff?.id}
                    setVisiblevisibleGroupTransferForm={setVisiblevisibleGroupTransferForm}
                  />
                  )}
                  {visibleGroupTransferChoosingCar && productTransportType === TransportTypeEnum.GROUP_TRANSFER && (
                    <GroupTransferChoosingCar
                      form={form}
                      groupTransferForm={groupTransferForm}
                      setVisibleGroupTransferChoosingCar={setVisibleGroupTransferChoosingCar}
                      setVisibleGroupTransferChoosingBookingInterval={setVisibleGroupTransferChoosingBookingInterval}
                      setVisiblevisibleGroupTransferForm={setVisiblevisibleGroupTransferForm}
                    />
                  )}
                  {visibleGroupTransferChoosingBookingInterval
                  && productTransportType === TransportTypeEnum.GROUP_TRANSFER && (
                  <GroupTransferChoosingBookingInterval
                    groupTransferForm={groupTransferForm}
                    setVisibleGroupTransferChoosingBookingInterval={setVisibleGroupTransferChoosingBookingInterval}
                    setVisiblevisibleGroupTransferForm={setVisiblevisibleGroupTransferForm}
                  />
                  )}
                  {!isExternal && productTransportType === TransportTypeEnum.BUS && step === 3 && (
                    <ItemNewDesign>
                      <BusProperties
                        setSubClass={setSubClass}
                        form={form}
                        onValidate={setBusCreateEnabled}
                      />
                    </ItemNewDesign>
                  )}
                  {step === 3
                  && productTransportType !== TransportTypeEnum.CARSHARING
                  && productTransportType !== TransportTypeEnum.PUBLIC
                  && productTransportType !== TransportTypeEnum.GROUP_TRANSFER
                  && productTransportType !== TransportTypeEnum.YANDEX && (
                  <Collapse
                    ghost={true}
                    expandIconPosition="right"
                    defaultActiveKey={1}
                  >
                    <Panel
                      className="collapse-comment"
                      header="Комментарий"
                      key="1"
                    >
                      <Form.Item name="commentary">
                        <Input.TextArea
                          showCount
                          maxLength={productTransportType !== TransportTypeEnum.BUS ? 255 : undefined}
                          rows={4}
                        />
                      </Form.Item>
                    </Panel>
                  </Collapse>
                  )}

                  {tripStore.fraud && (
                    <Alert
                      type="warning"
                      showIcon
                      message="Невозможно создать заявку"
                      description={tripStore.fraud}
                      className={styles.fraud}
                    />
                  )}
                </ItemNewDesign>
              )}

              <div style={{ display: isExternal && step === 2 ? 'block' : 'none' }}>
                <ItemNewDesign>
                  {isExternalPriceLoading ? (
                    <SpinWrapped />
                  ) : (
                    <ExternalPrices
                      tariffsInfo={tariffsInfo}
                      request={tripStore.currentTripRequest}
                      form={form}
                      purpose={form.getFieldValue('purpose')}
                      // eslint-disable-next-line @typescript-eslint/no-empty-function
                      setCurrentTransportType={() => {}}
                      isExternal={true}
                      externalPrices={tripStore.externalPrices}
                      product={product}
                      setProduct={setProduct}
                      setStep={setStep}
                      setSubClass={setSubClass}
                      setExternalProvider={setExternalProvider}
                    />
                  )}
                </ItemNewDesign>

                <ItemNewDesign
                  $margin="16px"
                  $padding="1px 16px 16px"
                  $position="absolute"
                  $bottom="0px"
                  $width="calc(100% - 32px)"
                >
                  <ButtonWrapper>
                    <TButton
                      type="primary"
                      htmlType="submit"
                      $size="middle"
                      className={styles.submit}
                      block={true}
                      disabled={
                        disabled
                        || isNoPersonalCars
                        || isCreating
                        || !busCreateEnabled
                        || isTripSplitChecking
                        || (step === 2 && productTransportType === TransportTypeEnum.TAXI
                          ? isExternalPriceLoading
                          : false)
                          || !selectedCounterpartyTariff
                      }
                      onClick={() => createOrder()}
                    >
                      Заказать
                    </TButton>
                  </ButtonWrapper>
                </ItemNewDesign>
              </div>

              {/* <ItemNewDesign $padding="16px" style={{ display: step === 4 ? 'block' : 'none' }}>
              <Additional
                form={form}
                transport={productTransportType}
                submitDisabled={disabled}
                coopTripData={coopTripData}
                onCommonFinish={createOrder}
                personalCar={personalCar}
                isExternal={isExternal}
                step={step}
                disabled={false}
              />
            </ItemNewDesign> */}

              {step === 5 && !isExternal && <AdditionalPreferences disabled={false} />}
            </div>
            {(isPickedTaxiOnThirdStep || step === 5 || (visibleGroupTransferForm && step !== 3)) && !isExternal && (
              <div className={styles.orderButton}>
                <ItemNewDesign>
                  <ButtonWrapper>
                    <TButton
                      type="primary"
                      htmlType="submit"
                      $size="middle"
                      className={styles.submit}
                      block={true}
                      disabled={disabledSubmit}
                      onClick={() => (isYandexTaxi ? createYandexTaxiOrder() : createOrder())}
                    >
                      {requestExist ? 'Сохранить' : 'Заказать'}
                    </TButton>
                  </ButtonWrapper>
                </ItemNewDesign>
              </div>
            )}

            {!isPickedTaxiOnThirdStep
            && !visibleGroupTransferChoosingBookingInterval
            && !visibleGroupTransferForm
            && !visibleGroupTransferChoosingCar
            && step !== 3
            && ((step !== 2 && isExternal) || (step !== 5 && !isExternal)) && (
            <ItemNewDesign $margin="12px" $padding="1px 16px 16px">
              {step !== 2 && (
              <ButtonWrapper>
                <TButton
                  type="primary"
                  $size="middle"
                  block={true}
                  disabled={
                          form.getFieldValue('when') === 'now' ? disabledNextButtonGoingNow : disabledNextButton
                        }
                  onClick={isExternal ? loadExternalPrices : () => handleNextStep()}
                  className={styles.confirmTimeButton}
                >
                  Далее
                </TButton>
              </ButtonWrapper>
              )}
            </ItemNewDesign>
            )}
          </div>
        </div>

        <PhoneModal selfStore={selfStore} empStore={empStore} />
        {checkingTariffsActive && (
          <TModal
            properties={{
              title: 'Нет активных тарифов',
              confirmButton: { text: 'Понятно' },
              declineButton: { text: 'Отменить', hide: true },
              handleConfirm: handleTariffsConfirm,
              style: {
                height: 264,
                margin: '30px 0 0 0',
                padding: '0 24px 24px 24px',
              },
              withoutScrolls: true,
            }}
            process={checkingProcessTariffs}
            onClose={handleTariffsClose}
            isPoppup={false}
            isReadOnly={false}
          >
            <div className={styles.modalContainerCarsharing}>
              <p className={styles.modalContainerCarsharing__title}>
                Создание обращения недоступно, нет активных тарифов. Обратитесь к ответственному по сопровождению
                транспортного обеспечения в вашей организации
              </p>
            </div>
          </TModal>
        )}
      </Form>
      {showSRMMassModal && (
        <SRMMassPreviewMultiple
          onConfirm={() => {
            setShowSRMMassModal(false);
          }}
          onClose={() => setShowSRMMassModal(false)}
        />
      )}
      <MassModal
        onUpload={() => setShowSRMMassModal(true)}
        isVisible={isVisible}
        onClose={handleCloseModal}
        setVisible={setVisible}
      />
      <CreateYandexApplyDrawer
        isOpen={isYandexDrawerOpen}
        setOpen={setYandexDrawerOpen}
        onSubmit={submitYandexOrder}
      />
      {TripSplitModal}
      {AbsenceWarningModal}
    </div>
  );
});

export default TransportOrder;
