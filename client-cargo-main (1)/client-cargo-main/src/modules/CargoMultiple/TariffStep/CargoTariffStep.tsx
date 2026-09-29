import React, { FC, useEffect, useState } from 'react';
import { Form } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import { SpinWrapped } from 'shared/components';
import Button from 'shared/form/Button/Button';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import useIsMounted from 'shared/hooks/useIsMounted';

import { PackageItem, StepTariffValues, StepTariffValuesMulti } from 'stores/Cargo/Cargo.interface';
import { Pack } from 'stores/Cargos/types';
import { Package, PurposeEnum, TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import {
  CargoListItem,
  DeliveryUrgencyEnum,
  Point
} from 'types/Cargo';
import { DATE_FORMAT } from 'constants/constants.app';

import * as Layout from '../CargoMultiple.style';
import StepHeader from '../components/StepHeader/StepHeader';
import { DEFAULT_FORM_ERROR, STEPS } from '../constants';
import * as S from './CargoTariffStep.style';
import { getContentCargoTariffStep } from './config/getContentCargoTariffStep';
import TariffComponent from './TariffComponent';

export interface Props {
  step?: number;
  setStep: (param: number) => void;
  onSave: (values: StepTariffValuesMulti) => void;
}

const CargoTariffStep: FC<Props> = observer(props => {
  const { setStep, onSave } = props;

  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.geoStore]: geoStore,
    [StoreNames.cargoTariffStore]: cargoTariffStore,
    logger,
  } = useAppStoreContext();

  const { waypoints, calculatedRoute } = geoStore;
  const { stepCargosValues, isRegular } = cargoStore;
  const { tariffMulti: calculatedTariffMulti, tariffsListMulti } = cargoTariffStore;
  const isMounted = useIsMounted();
  const [form] = Form.useForm();
  const [errors, setErrors] = useState('');
  const [preloader, setPreloader] = useState(true);

  const { tariffMulti } = getContentCargoTariffStep();

  const packsList = cargoStore.packageForm?.packages?.reduce((acc: { id: string | undefined; count: number }[], cur: PackageItem) => {
    const pack = cargoStore.packs?.find((packageItem: Pack) => packageItem.name === cur.package);
    // todo разобраться почему id может быть undefined.
    const packItem = {
      id: pack?.id,
      count: cur.packageCount,
    };
    return [...acc, packItem];
  }, []);

  const calculateAllTariffs = async (express = false) => {
    setPreloader(true);
    try {
      await cargoTariffStore.calculateAllTariffsMulti({
        organizationId: selfStore.selfEmployee?.organizationId || '',
        startPoint: waypoints[0] as unknown as Point,
        cargoCategory: cargoTariffStore.cargoCategory || stepCargosValues.cargoList[0]?.category,
        stopPoint: waypoints[waypoints.length - 1] as unknown as Point,
        time: 0,
        distance: Number(calculatedRoute?.distance.toFixed(3)) || 0,
        weight: stepCargosValues.cargoList.reduce((acc: number, curr: CargoListItem) => acc + (curr.weight * curr.occupiedPlacesCount || 0), 0),
        maxWeightOfOnePlace: Math.max(...stepCargosValues.cargoList.map(cargoItem => cargoItem.weight)),
        volume: stepCargosValues.cargoList.reduce((acc: number, curr: CargoListItem) => acc + (curr.volume * curr.occupiedPlacesCount || 0), 0),
        express,
        countPoint: waypoints.length,
        loadersNeeded: !!cargoStore.packageForm?.loadersNeeded,
        occupiedPlacesCount: stepCargosValues.cargoList.reduce((acc: number, curr: CargoListItem) => acc + curr.occupiedPlacesCount, 0),
        packages: packsList as Package[],
        tripDate: moment(cargoStore.stepAddressValuesMulti.desiredDate).format(DATE_FORMAT.MONTH_NAME_WITH_TIME_SECONDS_REVERTED),
        includeTemplate: isRegular,
        calcType: isRegular ? PurposeEnum.REGULAR : undefined,
      });
    } finally {
      setPreloader(false);
    }
  };

  useEffect(() => {
    // calculatedRoute высчитывается автоматически когда выбраны 2 точки адресов
    // при шаге Назад также срабатывает этот эффект
    if (calculatedRoute) {
      calculateAllTariffs(cargoStore.stepTariffValues.express);
    } else {
      calculateAllTariffs();
    }
  }, [waypoints, calculatedRoute]);

  const validateFields = async (onSuccess?: (values: any) => void) => {
    try {
      const values = await form.validateFields();
      if (!isMounted()) {
        return;
      }

      if (values.errors) {
        setErrors(DEFAULT_FORM_ERROR);
        return;
      }
      if (!calculatedTariffMulti) {
        setErrors('Необходимо выбрать тариф');
        return;
      }
      setErrors('');
      if (onSuccess) {
        onSuccess(values);
      }
    } catch (err) {
      const error = err as any;
      // console.log('Failed:', error);
      setErrors(error.errorFields && error.errorFields.length ? DEFAULT_FORM_ERROR : '');
    }
  };

  useEffect(() => {
    if (calculatedTariffMulti) {
      validateFields();
    }
  }, [calculatedTariffMulti]);

  useEffect(() => {
    // Автозаполнение
    form.setFields([
      ...Object.keys(cargoStore.stepTariffValues).map(key => ({
        name: key,
        value: cargoStore.stepTariffValues[key as keyof StepTariffValues],
      })),
      {
        name: 'deliveryUrgency',
        value: cargoStore.stepTariffValues.express ? DeliveryUrgencyEnum.express : DeliveryUrgencyEnum.standart,
      },
    ]);

    // пока убираю иначе с нуля начинает показывать ошибки
    // validateFields();
  }, [cargoStore.stepTariffValues]);

  useEffect(() => {
    cargoTariffStore.setTariffMulti(null);
  }, []);

  // Сохранение в стор выбранного тарифа
  const handleChangeTariff = (idx: number) => {
    if (tariffsListMulti.length) {
      cargoTariffStore.setTariffMulti(tariffsListMulti.find(tariff => tariff.idx === idx) as TariffCost);
    }
  };

  const handleNextStep = () => {
    validateFields((values: any) => {
      // Сохранение параметров выбранного тарифа
      onSave({
        expected: calculatedRoute || null, // тут, просто потому что тут есть доступ к роуту
        tariffCost: calculatedTariffMulti || null,
        express: values.deliveryUrgency === DeliveryUrgencyEnum.express,
        comment: values.comment,
      });
    });
  };

  const isNoTariffs = !tariffsListMulti.length;

  return (
    <Layout.SideContent>
      <StepHeader
        title="Тариф"
        onChange={() => setStep(STEPS.cargos)}
      />
      <S.SideSections>
        {preloader ? (
          <SpinWrapped />
        ) : (
          <>
            {isNoTariffs
              ? (
                <S.EmptyTariffs>
                  По данному маршруту доставка не осуществляется
                </S.EmptyTariffs>
              )
              : (
                <>
                  <S.Tariffs>
                    <S.TariffsTitle>Выберите тариф</S.TariffsTitle>
                    {tariffsListMulti.map((listItem, idx) => {
                      const {
                        name, title, description, image,
                      } = tariffMulti[listItem.transportType.name];
                      return (
                        <TariffComponent
                          idx={idx}
                          key={`${name}-${idx}`}
                          id={name}
                          active={calculatedTariffMulti?.idx === idx}
                          tariff={{ ...listItem, deliveryTime: listItem?.deliveryTime * 24 * 60 * 60 * 1000 }}
                          title={title}
                          description={description}
                          image={image}
                          logger={logger}
                          onChange={handleChangeTariff}
                        />
                      );
                    })}
                  </S.Tariffs>
                </>
              )}
          </>
        )}
      </S.SideSections>
      {!preloader && (
        <Layout.SideButtons>
          <Button disabled={!!errors} onClick={handleNextStep}>
            {errors || 'Перейти к подтверждению заказа'}
          </Button>
        </Layout.SideButtons>
      )}
    </Layout.SideContent>
  );
});

export default CargoTariffStep;
