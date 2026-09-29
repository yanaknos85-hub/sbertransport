import React, { FC, useEffect, useState } from 'react';
import { Form, notification, Space } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import moment from 'moment';
import CargoCommentMulti from 'shared/components/Cargo/CargoCommentMulti';
import { SOMETHING_WRONG_TITLE } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { PackageItem } from 'stores/Cargo/Cargo.interface';
import { Package, PurposeEnum } from 'stores/CargoTariff/CargoTariff.interface';
import { CargoListItem, Point } from 'types/Cargo';
import { DATE_FORMAT } from 'constants/constants.app';

import { SpinWrapped } from '../../../shared/components';
import { Fields } from '../../Exchange/components/EditableField/constants';
import { ButtonStep } from '../components/ButtonStep/ButtonStep';
import StepHeader from '../components/StepHeader/StepHeader';
import {
  DEFAULT_FORM_ERROR, RELOCATION_ERROR, STEPS
} from '../constants';
import { AdditionalServices } from './AdditionalServices/AdditionalServices';
import { FlatList } from './Flats/FlatList/FlatList';
import { Furniture } from './Furniture/Furniture';
import { Parameters } from './Parameters/Parameters';
import * as S from './RelocationCargosStep.style';

interface Props {
  setStep: (param: number) => void;
}

export const RelocationCargosStep: FC<Props> = observer(props => {
  const { setStep } = props;

  const [form] = useForm<Fields>();
  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.geoStore]: geoStore,
    [StoreNames.cargoTariffStore]: cargoTariffStore,
  } = useAppStoreContext();

  const { waypoints, calculatedRoute } = geoStore;
  const { stepCargosValues, isRegular } = cargoStore;

  const [errors, setErrors] = useState('');
  const [preloader, setPreloader] = useState(false);

  /* Прокрутка вверх при переходе на 2-й шаг переезда  */
  const scrollTop = () => {
    const content = document.querySelector('#layout_relocation');
    if (content) {
      content.scrollIntoView({ behavior: 'smooth' });
    }
  };

  // Следим за изменениями в cargoList и очищаем ошибку при наличии заполненных элементов
  useEffect(() => {
    const hasOccupiedPlaces = cargoStore.stepCargosValues.cargoList.some(
      item => item.occupiedPlacesCount > 0
    );

    if (hasOccupiedPlaces && errors === DEFAULT_FORM_ERROR) {
      setErrors('');
    }
  }, [cargoStore.stepCargosValues.cargoList, errors]);

  const validateFields = () => {
    try {
      const isFillFurniture = cargoStore.stepCargosValues.cargoList.some(
        cargoItem => cargoItem.occupiedPlacesCount > 0
      );

      if (!isFillFurniture) {
        setErrors(DEFAULT_FORM_ERROR);
        return false;
      }
      return true;
    } catch (error: any) {
      setErrors(error.errorFields && error.errorFields.length ? RELOCATION_ERROR : '');
      return false;
    }
  };

  const handleNextStep = async () => {
    const isValid = validateFields();

    if (!isValid) {
      return;
    }
    setPreloader(true);
    try {
      const packsList = cargoStore.packageForm?.packages?.reduce((acc: { id: string | undefined; count: number }[], cur: PackageItem) => {
        const packItem = {
          id: cur?.package,
          count: cur.packageCount,
        };
        return [...acc, packItem];
      }, []);

      await cargoTariffStore.calculateAllTariffsMulti({
        organizationId: selfStore.selfEmployee?.organizationId || '',
        startPoint: waypoints[0] as unknown as Point,
        stopPoint: waypoints[waypoints.length - 1] as unknown as Point,
        time: 0,
        distance: calculatedRoute?.distance || 0,
        weight: stepCargosValues.cargoList.reduce((acc: number, curr: CargoListItem) => acc + (curr.weight * curr.occupiedPlacesCount || 0), 0),
        volume: stepCargosValues.cargoList.reduce((acc: number, curr: CargoListItem) => acc + (curr.volume * curr.occupiedPlacesCount || 0), 0),
        occupiedPlacesCount: stepCargosValues.cargoList.reduce((acc: number, curr: CargoListItem) => acc + curr.occupiedPlacesCount, 0),
        maxWeightOfOnePlace: Math.max(...stepCargosValues.cargoList.map(cargoItem => cargoItem.weight)),
        express: false,
        countPoint: waypoints.length,
        loadersNeeded: form.getFieldValue('loaders'),
        packages: packsList as Package[],
        tripDate: moment(cargoStore.stepAddressValuesMulti.desiredDate).format(DATE_FORMAT.MONTH_NAME_WITH_TIME_SECONDS_REVERTED),
        includeTemplate: isRegular,
        calcType: PurposeEnum.RELOCATION,
        // Потребуются когда допишут бэк
        // carTransportNeeded: form.getFieldValue('car'),
        // liftNeeded: form.getFieldValue('lift'),
      });

      if (cargoTariffStore.tariffsListMulti.length > 0) {
        cargoTariffStore.setTariffMulti(cargoTariffStore.tariffsListMulti[0]);
        cargoStore.setStepTariffValuesMulti({
          expected: calculatedRoute || null,
          tariffCost: cargoTariffStore.tariffsListMulti[0] || null,
        });
        setStep(STEPS.final);
      }
    } catch (e) {
      notification.error({
        message: 'Заявка на переезд',
        description: SOMETHING_WRONG_TITLE,
      });
    } finally {
      setPreloader(false);
    }
  };

  useEffect(() => {
    scrollTop();
  }, [scrollTop]);

  return (
    <>
      {preloader ? (
        <S.SpinWrapper>
          <SpinWrapped />
        </S.SpinWrapper>
      ) : (
        <S.SideContent>
          <Form form={form} id="layout_relocation">
            <Space size={20} direction="vertical">
              <StepHeader
                title="Параметры груза"
                onChange={() => {
                  setStep(STEPS.address);
                }}
              />
              <S.SpaceStyled
                size={20}
                direction="vertical"
              >
                <FlatList />
                <Furniture
                  onCargoChange={() => {
                    const hasOccupiedPlaces = cargoStore.stepCargosValues.cargoList.some(
                      item => item.occupiedPlacesCount > 0
                    );
                    if (hasOccupiedPlaces && errors === DEFAULT_FORM_ERROR) {
                      setErrors('');
                    }
                  }}
                />
                <Parameters />
              </S.SpaceStyled>
              <AdditionalServices form={form} />
              <CargoCommentMulti />
            </Space>
          </Form>
          <ButtonStep
            errors={errors}
            handleNextStep={handleNextStep}
          />
        </S.SideContent>
      )}
    </>
  );
});
