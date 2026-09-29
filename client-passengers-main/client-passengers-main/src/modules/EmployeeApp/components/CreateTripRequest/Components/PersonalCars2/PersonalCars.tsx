import {
  Form, Radio, RadioChangeEvent, Typography
} from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useMemo, useState } from 'react';
import { useHistory } from 'react-router-dom';
import { FormInstance } from 'antd/es/form/Form';
import { Employee, PersonalCar } from '@sber-sbertransport/mf-core';
import { StoreNames } from 'stores';

import { AppLinksStartPage, MESSAGES } from 'constants/constants.app';

import { EmployeeAppLinks } from 'modules/EmployeeApp/EmployeeApp.constants';

import { ITripTariff } from 'stores/Trip/Trip.interface';
import { formatRublesWithoutRemainder } from 'utils/MoneyUtils';

import Switch from '../../../../../../shared/components/Images/Switch.svg';
import { PersonalCarCard } from './PersonalCarCard';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import personalCarsStyles from './personalCars.module.scss';
import '../../../TaxiClasses/Card/override.scss';

interface PersonalCarsProps {
  specificEmployee?: Employee;
  tariffsInfo: ITripTariff[];
  form: FormInstance;
  setPersonalCar: (car: PersonalCar) => void;
}

const LINK_PERSONAL_CARS = `${AppLinksStartPage.EmployeesApp}/${EmployeeAppLinks.passengers}/${EmployeeAppLinks.vehicles}/add`;

// Todo: Привязать к ID
const createCostElements = (costs: ITripTariff[]): (JSX.Element | null)[] => costs.map((cost: ITripTariff) => (
  cost.cost ? <b>{formatRublesWithoutRemainder(cost.cost / 100)}</b> : null)
);

export const PersonalCars: FC<PersonalCarsProps> = observer(
  ({
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    specificEmployee, tariffsInfo, form, setPersonalCar,
  }): JSX.Element => {
    const {
      [StoreNames.tripStore]: tripStore,
    } = useAppStoreContext();
    const personalCars = tripStore.personalCars;
    const costs = useMemo(() => {
      const personalTariff = tariffsInfo.find(item => item.transportType.name === TransportTypeEnum.PERSONAL);
      return personalTariff ? personalCars.map(() => personalTariff) : [];
    }, [tariffsInfo, personalCars]);
    const costElements = createCostElements(costs ?? []);
    const [showAll, setShowAll] = useState(false);
    const history = useHistory();
    const toggleShowAll = () => {
      // eslint-disable-next-line no-unused-expressions
      personalCars.length <= 1 ? history.push(`${LINK_PERSONAL_CARS}`) : setShowAll(!showAll);
    };
    const carsToDisplay = showAll ? personalCars : personalCars.slice(0, 1);

    const onChangePersonalCar = (event: RadioChangeEvent) => {
      const car = personalCars.filter(el => el.id === event.target.value);
      setPersonalCar && setPersonalCar(car[0]);
    };

    return (
      <div className={personalCarsStyles.personalCarsContainer}>
        <Form.Item
          noStyle={true}
          shouldUpdate={(prevValues, currentValues): boolean => prevValues.taxiClass !== currentValues.taxiClass}
        >
          <Form.Item
            name="personalCar"
            className="personal-cars"
            rules={[{ required: true, message: MESSAGES.personalCarRequired }]}
          >
            {(personalCars && personalCars.length && (
              <Radio.Group className="personalCarsItem" onChange={onChangePersonalCar}>
                {carsToDisplay?.map((car: PersonalCar, i: number) => (
                  <PersonalCarCard
                    car={car}
                    key={car.id}
                    cost={costElements[i]}
                  />
                ))}
              </Radio.Group>
            )) || <Typography.Text type="danger">{MESSAGES.noPersonalCars}</Typography.Text>}
          </Form.Item>
          <div className="switchCar" onClick={toggleShowAll}>
            <img src={Switch} alt="" />
            <span>Сменить автомобиль</span>
          </div>
        </Form.Item>
      </div>
    );
  }
);
