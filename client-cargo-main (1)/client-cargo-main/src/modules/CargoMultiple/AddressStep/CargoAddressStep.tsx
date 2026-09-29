/* eslint-disable react-hooks/exhaustive-deps */
import React, { FC, useEffect, useState } from 'react';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { Form, Tooltip } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import useIsMounted from 'shared/hooks/useIsMounted';

import { StepAddressValues } from 'stores/Cargo/Cargo.interface';
import { StepAddressValuesMulti } from 'stores/Cargos/typesMulti';
import { StoreNames } from 'stores/StoreNames.enum';
import * as routes from 'constants/constants.routes';

import CargoMassPreviewMultiple from '../../CargoMassMultiple/CargoMassPreviewMultiple/CargoMassPreviewMultiple';
import { MassModal } from '../../CargoMassMultiple/MassModal/MassModal';
import * as Layout from '../CargoMultiple.style';
import { ButtonStep } from '../components/ButtonStep/ButtonStep';
import StepHeader from '../components/StepHeader/StepHeader';
import { DEFAULT_FORM_ERROR } from '../constants';
import Info from '../static/images/info.png';
import { AddressForm } from './AddressForm/AddressForm';
import * as S from './CargoAddressStep.style';

interface Props {
  step?: number;
  setStep: (param: number) => void;
  onSave: (values: any) => void;
}

const CargoAddressStep: FC<Props> = observer(props => {
  const { onSave } = props;

  const {
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.cargosStore]: cargosStore,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();
  const IS_PERSONAL_DEVICE = configStore.env.IS_PERSONAL_DEVICE;
  const history = History();
  const isMounted = useIsMounted();
  const [form] = Form.useForm();

  const [showCargoMassModal, setShowCargoMassModal] = useState(false);
  const [errors, setErrors] = useState('');
  const [isVisible, setVisible] = useState(false);
  const [firstAttempt, setFirstAttempt] = useState(false);
  const [prevIsRelocation, setPrevIsRelocation] = useState(cargoStore.isRelocation);

  const validateFields = async (onSuccess: (values: StepAddressValues) => void, force = false) => {
    try {
      if (!firstAttempt && !force) {
        return;
      }
      const values = await form.validateFields();
      if (!isMounted()) {
        return;
      }
      if (values.errors) {
        setErrors(DEFAULT_FORM_ERROR);
        return;
      }
      setErrors('');
      if (onSuccess) {
        onSuccess(values);
      }
    } catch (error: any) {
      setErrors(error.errorFields && error.errorFields.length ? DEFAULT_FORM_ERROR : '');
    }
  };

  useEffect(() => {
    cargosStore.resetSettings();
    cargosStore.resetListPathName();
  }, []);

  useEffect(() => {
    // Автозаполненение
    // Поскольку отправитель сразу выставлен как self, то надо его сразу выбрать
    employeeStore.onEmployeeSelect(employeeStore.selfEmployee.fullNameWithCode, 0);

    form.setFields(
      Object.keys(cargoStore.stepAddressValuesMulti).map(key => ({
        name: key,
        value: cargoStore.stepAddressValuesMulti[key as keyof StepAddressValuesMulti],
      }))
    );
    // пока убираю иначе с нуля начинает показывать ошибки
    // validateFields(fieldNameList);
  }, [cargoStore.stepAddressValuesMulti]);

  useEffect(() => {
    if (prevIsRelocation && !cargoStore.isRelocation) {
      const waypoints = form.getFieldValue('waypoints') || [];
      waypoints.forEach((_: any, index: number) => {
        form.setFields([
          {
            name: ['waypoints', index, 'entrance'], value: '', touched: false,
          },
          {
            name: ['waypoints', index, 'floor'], value: '', touched: false,
          },
          {
            name: ['waypoints', index, 'flat'], value: '', touched: false,
          },
        ]);
      });
    }
    setPrevIsRelocation(cargoStore.isRelocation);
  }, [cargoStore.isRelocation, form]);

  const handleRegular = (value: boolean) => {
    cargoStore.setRegularOrder(value);
    if (value) {
      cargoStore.setRelocationOrder(false);
    }
  };

  const handlePurpose = (value: boolean) => {
    cargoStore.setRelocationOrder(value);
    if (value) {
      cargoStore.setRegularOrder(false);
    }
    cargoStore.setCargoList([]);
    cargoStore.savePackageForm({
      packages: [],
    });
  };

  const handleNextStep = () => {
    if (!firstAttempt) {
      setFirstAttempt(true);
    }
    validateFields((values: any) => {
      onSave({
        ...values,
      });
    }, !firstAttempt).then();
  };

  const handleOpenModal = () => {
    setVisible(true);
  };

  const handleCloseModal = () => {
    setVisible(false);
  };

  return (
    <Layout.SideContent>
      <StepHeader>
        <S.Header>
          <S.Title>Доставка</S.Title>
          {!IS_PERSONAL_DEVICE && (
            <S.Button onClick={handleOpenModal}>
              Массовая доставка
            </S.Button>
          )}
        </S.Header>
        <S.ControlsContainer>
          <S.Controls>
            <S.Switch checked={cargoStore.isRegular} onChange={handleRegular} />
            <S.SwitchTitle>
              Регулярная доставка
            </S.SwitchTitle>
            <Tooltip title="Оформление доставок по графику">
              <S.Img src={Info} alt="info icon" />
            </Tooltip>
            <S.Controls>
              <S.Switch checked={cargoStore.isRelocation} onChange={handlePurpose} />
              <S.SwitchTitle>
                Релокация
              </S.SwitchTitle>
              <Tooltip title="Оформление доставок на переезд">
                <S.Img src={Info} alt="info icon" />
              </Tooltip>
            </S.Controls>
          </S.Controls>
        </S.ControlsContainer>
        {showCargoMassModal && (
          <CargoMassPreviewMultiple
            onConfirm={() => {
              setShowCargoMassModal(false);
              history.push(routes.MASS_CARGO_CREATE);
            }}
            onClose={() => setShowCargoMassModal(false)}
          />
        )}
      </StepHeader>
      <AddressForm form={form} validateFields={validateFields} />
      <ButtonStep errors={errors} handleNextStep={handleNextStep} />
      <MassModal
        onUpload={() => setShowCargoMassModal(true)}
        isVisible={isVisible}
        onClose={handleCloseModal}
        setVisible={setVisible}
      />
    </Layout.SideContent>
  );
});

export default CargoAddressStep;
