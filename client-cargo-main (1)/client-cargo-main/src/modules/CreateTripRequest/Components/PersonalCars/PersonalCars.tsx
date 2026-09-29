import '../../../TaxiClasses/Card/override.scss';

import React, { FC } from 'react';
import { Employee, PersonalCar } from '@sber-sbertransport/mf-core';
import {
  Form, Radio, RadioChangeEvent, Typography
} from 'antd';
import { observer } from 'mobx-react';
import { formatRubles } from 'utils';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITripTariff } from 'stores/Trip/Trip.interface';
import { MESSAGES } from 'constants/constants.app';

import { usePersonalCars } from '../../hooks/usePersonalCars';
import { PersonalCarCard } from './PersonalCarCard';

import personalCarsStyles from './personalCars.module.scss';

interface PersonalCarsProps {
  onChangePersonalCar: (event: RadioChangeEvent) => void;
  specificEmployee?: Employee;
}

// Todo: Привязать к ID
const createCostElements = (costs: ITripTariff[]): (JSX.Element | null)[] => costs.map((cost: ITripTariff) => (cost.cost ? <b>{formatRubles(Math.round(cost.cost / 100))}</b> : null));

export const PersonalCars: FC<PersonalCarsProps> = observer(
  ({ onChangePersonalCar, specificEmployee }): JSX.Element => {
    const { personalCars, costs } = usePersonalCars(specificEmployee);
    const costElements = createCostElements(costs ?? []);

    return (
      <div className={personalCarsStyles.personalCarsContainer}>
        <Form.Item
          noStyle={true}
          shouldUpdate={(prevValues, currentValues): boolean => prevValues.taxiClass !== currentValues.taxiClass}
        >
          {({ getFieldValue }): JSX.Element | null => (getFieldValue('taxiClass') === `${TransportTypeEnum.PERSONAL}-undefined` && (
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
