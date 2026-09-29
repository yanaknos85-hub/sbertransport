import '../../override.scss';
import {
  Divider, Form, Input, Switch
} from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useEffect, useState } from 'react';

import additionalInformation from './additionalInformation.module.scss';
import {
  childSafetySeat, desiredtypeTransport, fieldTitles, transportationAnimal
} from '../../constants';
import Children from 'shared/components/Images/Children.svg';
import BusIcon from 'shared/components/Images/BusIcon.svg';
import Animal from 'shared/components/Images/Animal.svg';
import { ChildrensChairs } from './components/childrensChairs/ChildrensChairs';
import { Information } from 'stores/Trip/Trip.interface';

export const AdditionalInformation: FC<{ information: Information }> = observer(
  ({ information }): JSX.Element => {
    const [isVisibleDesiredtypeTransport, setIsVisibleDesiredtypeTransport] = useState(false);
    const [isVisibleTransportationAnimal, setIsVisibleTransportationAnimal] = useState(false);
    const [isVisibleChildSafetySeat, setIsVisibleChildSafetySeat] = useState(false);

    useEffect(() => {
      if (information) {
        information.bugsOversizedComment && setIsVisibleDesiredtypeTransport(true);
        information.animal && setIsVisibleTransportationAnimal(information.animal);
        information.childSeat && setIsVisibleChildSafetySeat(information.childSeat);
      }
    }, []);

    return (
      <div className="wrapper_groupTransfer">
        <span className={additionalInformation.additionalInformationTitle}>Дополнительно</span>
        <div className="wrapper_switch">
          <div className={additionalInformation.wrapper_baggageInformation}>
            <img src={Children} alt="Group" />
            <span className={additionalInformation.title}>{childSafetySeat}</span>
          </div>
          <Form.Item
            key={fieldTitles.childSeat}
            name={fieldTitles.childSeat}
            className={additionalInformation.childSafetySeat}
          >
            <Switch checked={isVisibleChildSafetySeat} onChange={() => setIsVisibleChildSafetySeat(prev => !prev)} />
          </Form.Item>
        </div>
        {isVisibleChildSafetySeat && (
          <ChildrensChairs />
        )}
        <Divider />
        <div className="wrapper_switch">
          <div className={additionalInformation.wrapper_baggageInformation}>
            <img src={BusIcon} alt="Group" />
            <span className={additionalInformation.title}>{desiredtypeTransport}</span>
          </div>
          <Switch checked={isVisibleDesiredtypeTransport} onChange={() => setIsVisibleDesiredtypeTransport(prev => !prev)} />
        </div>
        {isVisibleDesiredtypeTransport && (
          <Form.Item
            key={fieldTitles.typeVehicle}
            name={fieldTitles.typeVehicle}
          >
            <Input className={additionalInformation.open_input} placeholder="Укажите желаемый тип ТС" />
          </Form.Item>
        )}
        <Divider />
        <div className="wrapper_switch">
          <div className={additionalInformation.wrapper_baggageInformation}>
            <img src={Animal} alt="Group" />
            <span className={additionalInformation.title}>{transportationAnimal}</span>
          </div>
          <Form.Item
            key={fieldTitles.animal}
            name={fieldTitles.animal}
          >
            <Switch checked={isVisibleTransportationAnimal} onChange={() => setIsVisibleTransportationAnimal(prev => !prev)} />
          </Form.Item>
        </div>
        {isVisibleTransportationAnimal && (
          <Form.Item
            key={fieldTitles.animalComment}
            name={fieldTitles.animalComment}
          >
            <Input className={additionalInformation.open_input} placeholder="Укажите количество животных" />
          </Form.Item>
        )}
      </div>
    );
  }
);
