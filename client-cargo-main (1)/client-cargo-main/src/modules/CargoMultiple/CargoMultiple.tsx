import React, { FC, useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { observer } from 'mobx-react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { RequestType } from 'shared/models/types';
import Modal from 'shared/ui/Modal/Modal';

import {
  PackageItem,
  StepCargosValues,
  StepFinalValues,
  StepTariffValuesMulti
} from 'stores/Cargo/Cargo.interface';
import { Pack } from 'stores/Cargos/types';
import {
  AddressListMultiForm,
  ContactMulti,
  OrderRequestMulti, OrderRequestRegularMulti,
  StepAddressValuesMulti,
  WaypointMulti
} from 'stores/Cargos/typesMulti';
import { Package, TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { DATE_FORMAT } from 'constants/constants.app';
import { CARGOS, REGULAR_CARGOS } from 'constants/constants.routes';

import { MapRenderer } from '../CreateTripRequest/Components/MapRenderer';
import CargoAddressStep from './AddressStep/CargoAddressStep';
import * as Styled from './CargoMultiple.style';
import CargosStep from './CargosStep/CargosStep';
import {
  FILL_FORM, INITIAL_STEP, STEPS
} from './constants';
import CargoFinalStep from './FinalStep/CargoFinalStep';
import useFill from './hooks/useFill';
import { RelocationCargosStep } from './RelocationCargosStep/RelocationCargosStep';
import CargoTariffStep from './TariffStep/CargoTariffStep';

const CreateCargo: FC = observer(() => {
  const history = History();
  const [step, setStep] = useState(INITIAL_STEP);
  const [preloader, setPreloader] = useState(false);
  const [modalError, setModalError] = useState('');

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const queryStep = queryParams.get('step');

  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.geoStore]: geoStore,
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.cargoTariffStore]: cargoTariffStore,
    [StoreNames.cargoTypeStore]: cargoTypeStore,
    [StoreNames.employeeStore]: employeeStore,
  } = useAppStoreContext();
  const {
    stepAddressValues,
    stepCargosValues,
    stepTariffValuesMulti,
    periodValues,
    stepAddressValuesMulti,
    isRegular,
  } = cargoStore;

  const SOURCE_APP = 'WEB';

  // Сбрасываем текущие стейты
  useEffect(() => {
    if (queryStep) {
      setStep(+queryStep);
      return;
    }
    cargoStore.clearStepValues();
    geoStore.clearCurrentState();
    cargoTariffStore.clearState();
    cargoTypeStore.clearState();
  }, [queryStep]);

  const { filledAddress, filledCargos } = useFill();

  useEffect(() => {
    if (FILL_FORM) {
      setPreloader(true);

      if (filledAddress && filledCargos) {
        setPreloader(false);
      }
    }
  }, [filledAddress, filledCargos]);

  const doneRequest = () => {
    if (isRegular) {
      history.push(REGULAR_CARGOS);
    } else {
      history.push(CARGOS);
    }
  };

  // useEffect(() => {
  //   console.log(`STEPS STORE. Current: ${SWAP_STEPS[step]}`, {
  //     addressMulti: toJS(stepAddressValuesMulti),
  //     address: toJS(stepAddressValues),
  //     cargos: toJS(stepCargosValues),
  //     tariff: toJS(stepTariffValues),
  //     final: toJS(stepFinalValues),
  //   });
  // }, [step]);

  const scrollBottom = () => {
    const content = document.querySelector('[data-scroll]');
    if (content) {
      setTimeout(() => {
        content.scrollTo({ top: window.innerHeight, behavior: 'smooth' });
      }, 100);
    }
  };

  const handleSaveAddressMulti = (values: StepAddressValuesMulti) => {
    cargoStore.setStepAddressValuesMulti(values);

    const { sender } = stepAddressValues;

    const waypointsData = values.waypoints?.reduce((acc: any[], cur: AddressListMultiForm, idx): WaypointMulti[] => {
      const contact = [sender, ...employeeStore.employeeAutocompleteSelected]
        .find(employee => employee?.fullNameWithCode === cur.name);

      const addressContact: ContactMulti = {
        fullName: contact?.fullNameWithCode || '',
        mobilePhone: values?.waypoints[idx].phone,
        employeeId: contact?.userId,
        organizationId: contact?.organizationId,
        firstName: contact?.firstName,
        lastName: contact?.lastName,
        patronymic: contact?.patronymic,
      };

      const freeAddressContact: ContactMulti = {
        fullName: values?.waypoints[idx].name,
        mobilePhone: values?.waypoints[idx].phone,
      };

      const contacts = contact ? [addressContact] : [freeAddressContact];

      if (cur.extraContactName && cur.extraContactPhone) {
        const extraAddressContact: ContactMulti = {
          fullName: cur.extraContactName,
          mobilePhone: cur.extraContactPhone,
        };
        contacts.push(extraAddressContact);
      }

      // Для сравнения адресов оставляем только буквы, цифры, тире и дефис
      const cleanText = text => text?.replace(/[^а-яА-Яa-zA-Z0-9\-–]/g, '');

      const address = geoStore.waypoints.find(waypoint => {
        if (cargoStore.isRepeatOrder) {
          return cleanText(waypoint.addressStringRepresentation) === cleanText(cur.address);
        } else {
          return cleanText(waypoint.addressString) === cleanText(cur.address);
        }
      });

      if (address) {
        address.entrance = values.waypoints[idx]?.entrance;
        address.floor = values.waypoints[idx]?.floor;
        address.flat = values.waypoints[idx]?.flat;
        address.addressStringRepresentation = address.getFormattedAddressWithDetails();
      }
      const point = {
        ...address,
        addressStringRepresentation: address?.addressStringRepresentation,
        orderingIndex: idx,
        contacts: contacts,
        type: values.waypoints && values.waypoints[idx]?.type,
        organization: values?.waypoints[idx]?.organization,
        _addressString: address?.getFormattedAddressWithDetails(),
      };
      return [...acc, point];
    }, []);
    cargoStore.addAddress([...waypointsData]);

    setStep(STEPS.cargos);
  };

  const handleSaveCargos = (values: StepCargosValues) => {
    cargoStore.setStepCargosValues(values);
    setStep(STEPS.tariff);
  };

  const handleSaveTariff = (values: StepTariffValuesMulti) => {
    cargoStore.setStepTariffValuesMulti(values);
    setStep(STEPS.final);
  };

  const handleSaveFinal = async (values: StepFinalValues) => {
    cargoStore.setStepFinalValues(values); // Хотя это и не обязательно

    const { cargoList } = stepCargosValues;
    const { cost } = values;
    const {
      periodType, dayOfWeek, weekOfMonth, monthOfQuartal, beginDate, endDate,
    } = periodValues;

    const cargoDetails = cargoList
      .filter(cargo => cargo.occupiedPlacesCount > 0)
      .map(cargo => ({
        ...cargo,
        position: cargo.position + 1,
      }));

    const desiredDateFormattedMulti
      = moment(stepAddressValuesMulti.desiredDate)
        .format(DATE_FORMAT.MONTH_NAME_WITH_TIME_SECONDS_REVERTED)
        .valueOf();

    // Создание списка упаковок для запроса создания заявки.
    // Тут какая-то странная логика.
    // Нужно ли это? Нужно смотреть создание разовой или регулярной заявки.
    const packsList = cargoStore.packageForm?.packages?.reduce((acc: { id: string | undefined; count: number; name?: string }[], cur: PackageItem) => {
      const pack = cargoStore.packs?.find((packageItem: Pack) => packageItem.id === cur.package);
      // todo разобраться почему id может быть undefined.
      const packItem = {
        id: pack?.id,
        count: cur.packageCount,
        name: pack?.name,
      };
      return [...acc, packItem];
    }, []);

    const { idx, ...calculatedTariff } = stepTariffValuesMulti?.tariffCost as TariffCost;

    const requestRegularData: OrderRequestRegularMulti = {
      period: {
        periodType,
        dayOfWeek,
        weekOfMonth,
        monthOfQuartal,
        beginDate,
        endDate,
      },
      template: {
        desiredDate: desiredDateFormattedMulti,
        author: selfStore.selfEmployee,
        organizationId: selfStore.selfEmployee.organizationId,
        cargoDetails,
        waypoints: cargoStore.waypointsListMulti,
        calculatedTariff: calculatedTariff as any,
        source: SOURCE_APP,
        loadersNeeded: cargoStore.packageForm.loadersNeeded,
        cost,
        packages: packsList as Package[],
        distance: Number(geoStore.calculatedRoute?.distance.toFixed(3)),
        segments: geoStore.calculatedRoute?.segments,
        comment: cargoStore.comment,
      },
      active: true,
      source: SOURCE_APP,
    };

    const requestMulti: OrderRequestMulti = {
      desiredDate: desiredDateFormattedMulti,
      author: selfStore.selfEmployee,
      organizationId: selfStore.selfEmployee.organizationId,
      cargoDetails,
      waypoints: cargoStore.waypointsListMulti,
      calculatedTariff: calculatedTariff as any,
      source: SOURCE_APP,
      loadersNeeded: cargoStore.packageForm.loadersNeeded,
      loaders: calculatedTariff.loaders,
      cost,
      packages: packsList as Package[],
      distance: Number(geoStore.calculatedRoute?.distance.toFixed(3)),
      segments: geoStore.calculatedRoute?.segments,
      comment: cargoStore.comment,
      ...(cargoStore.isRelocation && { internalNote: stepAddressValuesMulti.internalNote }),
      ...(cargoStore.isRelocation && { requestType: RequestType.RELOCATION }),
    };

    setPreloader(true);

    try {
      if (isRegular) {
        await cargoStore.postRegularCargoDataMulti(requestRegularData);
      } else {
        await cargoStore.postDataMulti(requestMulti);
        // Снятие флага повторной заявки
        cargoStore.setRepeatOrder(false);
      }
      doneRequest();
    } catch (err) {
      setPreloader(false);
    }
  };

  if (preloader) {
    return <SpinWrapped />;
  }

  if (step === STEPS.final) {
    return (
      <>
        <Modal
          title="Ошибка"
          visible={!!modalError}
          onOk={() => setModalError('')}
          cancelButtonProps={{ style: { display: 'none' } }}
        >
          {modalError}
        </Modal>
        <CargoFinalStep
          step={step}
          setStep={setStep}
          onSave={handleSaveFinal}
          isRegular={!!isRegular}
        />
      </>
    );
  }

  // В зависимости от условий(переезд/не переезд) отображаем разные компоненты
  const cargoStepComponent = cargoStore.isRelocation
    ? <RelocationCargosStep setStep={setStep} />
    : (
      <CargosStep
        setStep={setStep}
        scrollBottom={scrollBottom}
        onSave={handleSaveCargos}
      />
    );

  return (
    <Styled.Layout>
      <Styled.Map>
        <MapRenderer
          geo={geoStore}
          zoomControlPosition="bottomleft"
          locateControlPosition="bottomleft"
        />
      </Styled.Map>
      <Styled.Side>
        <Styled.SideInner>
          {step === STEPS.address && (
            <CargoAddressStep
              setStep={setStep}
              onSave={handleSaveAddressMulti}
            />
          )}
          {step === STEPS.cargos && (
            cargoStepComponent
          )}
          {step === STEPS.tariff && (
            <CargoTariffStep
              setStep={setStep}
              onSave={handleSaveTariff}
            />
          )}
        </Styled.SideInner>
      </Styled.Side>
    </Styled.Layout>
  );
});

export default CreateCargo;
