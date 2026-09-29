/* eslint-disable @typescript-eslint/no-explicit-any */
import { PersonalCar } from '@sber-sbertransport/mf-core';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Store } from 'antd/lib/form/interface';
import { useCallback, useEffect } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { useAddPersonalCar, useEditPersonalCar } from 'api/personalCars';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { UpdateWithOsago, useSpecificCar } from 'shared/hooks/useSpecificCar';
import { StoreNames } from 'stores/StoreNames.enum';

interface IUsePersonalCar {
  onFinish(values: Store): void;
  goToPersonalCarsList(): void;
  form: FormInstance;
  osagoHandler: UpdateWithOsago<PersonalCar>;
  specificCar?: PersonalCar;
}

export const usePersonalCar = (): IUsePersonalCar => {
  const history = useHistory();
  const match = useRouteMatch<any>();

  const { [StoreNames.employeeStore]: employeeStore, [StoreNames.tripStore]: tripStore } = useAppStoreContext();
  const { selfEmployee } = employeeStore;

  const [form] = Form.useForm();
  const { personalCarId } = match.params;
  const { personalCars } = tripStore;

  const goToPersonalCarsList = useCallback((): void => history.push('./'), [history]);

  const { specificCar, updateWithOsago } = useSpecificCar({ personalCars, personalCarId });
  const osagoHandler: UpdateWithOsago<PersonalCar> = updateWithOsago(selfEmployee);

  useEffect(
    (): void => form.setFieldsValue({
      ...form.getFieldsValue(),
      ...(specificCar as PersonalCar),
    }),
    [form, specificCar]
  );

  const [addPersonalCar] = useAddPersonalCar(selfEmployee);
  const [editPersonalCar] = useEditPersonalCar(selfEmployee);

  function onFinish(car: PersonalCar): void {
    const model = {
      id: car?.id,
      transportType: car?.transportType,
      brandName: car?.brandName,
      ...(car?.color ? { color: car.color } : {}),
      model: car?.model,
      registrationCertificate: car?.registrationCertificate,
      registrationNumber: car?.registrationNumber,
      engineVolume: car?.engineVolume,
      insuranceNumber: car?.insuranceNumber,
      employeeId: selfEmployee?.id || '',
      passengerSeatsCount: car?.passengerSeatsCount,
      ownerInfo: car?.ownerInfo,
      persDataAccept: car?.persDataAccept || false,
    };

    if (!model.id) {
      addPersonalCar(model).then(() => {
        goToPersonalCarsList();
      });
    } else {
      editPersonalCar(model).then(() => {
        goToPersonalCarsList();
      });
    }
  }

  return {
    onFinish,
    form,
    goToPersonalCarsList,
    specificCar,
    osagoHandler,
  };
};
