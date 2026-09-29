import { Employee, PersonalCar } from '@sber-sbertransport/mf-core';
import '../../../TaxiClasses/Card/override.scss';

import {
  Form, Radio, RadioChangeEvent, Typography
} from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useMemo } from 'react';
import { StoreNames } from 'stores';

import { MESSAGES } from 'constants/constants.app';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripTariff } from 'stores/Trip/Trip.interface';
import { formatRubles } from 'utils';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { PersonalCarCard } from './PersonalCarCard';

import personalCarsStyles from './personalCars.module.scss';

interface PersonalCarsProps {
  onChangePersonalCar: (event: RadioChangeEvent) => void;
  specificEmployee?: Employee;
  tariffsInfo: ITripTariff[];
}

// Todo: Привязать к ID
const createCostElements = (costs: ITripTariff[]): (JSX.Element | null)[] => costs.map((cost: ITripTariff) => (cost.cost ? <b>{formatRubles(Math.round(cost.cost / 100))}</b> : null));

export const PersonalCars: FC<PersonalCarsProps> = observer(
  ({
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    onChangePersonalCar, specificEmployee, tariffsInfo,
  }): JSX.Element => {
    const {
      [StoreNames.tripStore]: tripStore,
    } = useAppStoreContext();
    const { personalCars } = tripStore;
    const costs = useMemo(() => {
      const personalTariff = tariffsInfo.find(item => item.transportType.name === TransportTypeEnum.PERSONAL);
      return personalTariff ? personalCars.map(() => personalTariff) : [];
    }, [tariffsInfo, personalCars]);
    const costElements = createCostElements(costs ?? []);

    return (
      <div className={personalCarsStyles.personalCarsContainer}>
        <Form.Item
          noStyle={true}
          shouldUpdate={(prevValues, currentValues): boolean => prevValues.taxiClass !== currentValues.taxiClass}
        >
          {({ getFieldValue }): JSX.Element | null => (getFieldValue('taxiClass')?.startsWith(`${TransportTypeEnum.PERSONAL}-undefined`) && (
          <Form.Item
            name="personalCar"
            className="personal-cars"
            rules={[{ required: true, message: MESSAGES.personalCarRequired }]}
          >
            {(personalCars && personalCars.length && (
            <Radio.Group onChange={onChangePersonalCar}>
              {personalCars?.map((car: PersonalCar, i: number) => (
                <PersonalCarCard
                  car={car}
                  key={car.id}
                  cost={costElements[i]}
                />
              ))}
            </Radio.Group>
            )) || <Typography.Text type="danger">{MESSAGES.noPersonalCars}</Typography.Text>}
          </Form.Item>
          ))
          || null}
        </Form.Item>
      </div>
    );
  }
);
