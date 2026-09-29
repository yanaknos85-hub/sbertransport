/* eslint-disable react-hooks/exhaustive-deps */
import React, { FC, useEffect, useState } from 'react';
import { notification } from 'antd';
import { observer } from 'mobx-react';
import Button from 'shared/form/Button/Button';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StepCargosValues } from 'stores/Cargo/Cargo.interface';
import { cargoTypeCategoryNameTitle } from 'stores/CargoType/CargoType.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { CargoListItem } from 'types/Cargo';
import { DEFAULT_ERROR } from 'constants/constants.app';

import * as Layout from '../CargoMultiple.style';
import StepHeader from '../components/StepHeader/StepHeader';
import { STEPS } from '../constants';
import PlusIcon from '../static/images/plus24.png';
import CargoForm from './CargoForm';
import CargoItem from './CargoItem';
import * as S from './Cargos.style';
import PackageForm from './PackageForm/PackageForm';

export interface Props {
  step?: number;
  setStep: (param: number) => void;
  scrollBottom: () => void;
  onSave: (values: StepCargosValues) => void;
  onPackageFormSave?: (formData: { loaders: number; packages: { package: string; packageCount: number } }) => void;
}

const CargosStep: FC<Props> = observer(props => {
  const {
    setStep, scrollBottom, onSave,
  } = props;

  const { [StoreNames.cargoStore]: cargoStore, logger } = useAppStoreContext();

  const { cargoList } = cargoStore.stepCargosValues;

  const [formIsOpen, setFormIsOpen] = useState(true);

  const [errors, setErrors] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<number, string>>({});

  const validateFields = (onSuccess?: () => void) => {
    if (!cargoList.length) {
      setErrors(DEFAULT_ERROR);
      return;
    }

    setErrors('');

    if (onSuccess) {
      onSuccess();
    }
  };

  const setFieldErrorByKey = (position: number, text = '') => {
    setFieldErrors({ ...fieldErrors, [position]: text });
  };

  const clearFieldError = () => setFieldErrors({});

  const existCargoText = (name: string) => `Груз "${name}" уже добавлен`;

  const differentCargoType = (name: string, category: string) => (
    `Груз "${name}" отличается по категории. Категория груза ${name} - "${cargoTypeCategoryNameTitle[category]}"`
  );

  const showCargoExistWarn = (description: string) => {
    notification.success({
      message: 'Внимание!',
      description,
    });
  };

  const handleAddCargo = (cargo: CargoListItem) => {
    if (cargoList.find(item => item.cargoName === cargo.cargoName)) {
      // setFieldErrorByKey(cargoList.length, existCargoText(cargo.cargoName)); // пока не требуется выводить как ошибку
      showCargoExistWarn(existCargoText(cargo.cargoName));
      return;
    }

    if (cargoList.find(item => item.cargoCategory !== cargo.category)) {
      showCargoExistWarn(differentCargoType(cargo.cargoName, cargo.cargoCategory));
      return;
    }

    if (cargo) {
      setFieldErrorByKey(cargoList.length);
      cargoStore.setCargoList([...cargoList, cargo]);
      setFormIsOpen(false);
    }
  };

  const handleEditCargo = (cargo: CargoListItem, onSuccess: () => void) => {
    if (cargoList.find(item => item.cargoName === cargo.cargoName && item.position !== cargo.position)) {
      // setFieldErrorByKey(cargo.position, existCargoText(cargo.cargoName)); // пока не требуется выводить как ошибку
      showCargoExistWarn(existCargoText(cargo.cargoName));
      return;
    }
    setFieldErrorByKey(cargo.position);
    cargoStore.setCargoList(cargoList.map(item => (item.position === cargo.position ? cargo : item)));
    onSuccess();
  };

  const handleDeleteCargo = (position: number) => {
    setFieldErrorByKey(position);
    setFormIsOpen(false);
    cargoStore.setCargoList(cargoList.filter(item => item.position !== position).map((item, i) => ({ ...item, position: i })));
  };

  const handleOpenForm = () => {
    setFormIsOpen(true);
    clearFieldError();
    scrollBottom();
  };

  const handleCloseForm = () => {
    setFormIsOpen(false);
    clearFieldError();
    scrollBottom();
  };

  const handleCancelForm = () => {
    clearFieldError();
  };

  useEffect(() => {
    validateFields();
    scrollBottom();

    if (cargoList.length) {
      setFormIsOpen(false);
    }
  }, [cargoList.length]);

  const handleNextStep = () => {
    validateFields(() => {
      onSave({ cargoList });
    });
  };

  return (
    <Layout.SideContent>
      <StepHeader
        title="Что Вы отправляете?"
        onChange={() => {
          setStep(STEPS.address);
          cargoStore.setStepCargosValues({ cargoList });
        }}
      />
      <Layout.SideSections data-scroll={true}>
        {cargoList.map(cargo => (
          <CargoItem
            key={cargo.id}
            position={cargo.position}
            error={fieldErrors[cargo.position]}
            cargo={cargo}
            logger={logger}
            onEdit={handleEditCargo}
            onDelete={handleDeleteCargo}
            onCancel={handleCancelForm}
          />
        ))}
        {formIsOpen && (
          <CargoForm
            position={cargoList.length} // номер позиции добавляемого груза
            error={fieldErrors[cargoList.length]}
            logger={logger}
            onSave={handleAddCargo}
            onDelete={handleCloseForm}
          />
        )}
      </Layout.SideSections>
      {!formIsOpen && (
        <Layout.SideButtons>
          <S.ButtonAddCargo type="text" onClick={() => handleOpenForm()}>
            <span>
              <S.Image src={PlusIcon} alt="plus icon" />
              Добавить груз
            </span>
          </S.ButtonAddCargo>
        </Layout.SideButtons>
      )}
      <PackageForm />
      <Layout.SideButtons>
        <Button disabled={!!errors || formIsOpen} onClick={handleNextStep}>
          {errors || 'Далее'}
        </Button>
      </Layout.SideButtons>
    </Layout.SideContent>
  );
});

export default CargosStep;
