import React, { useEffect, useState, FC } from 'react';
import { Link } from 'react-router-dom';
import {
  QuickOrderWidgetTitle,
  ServiceButton,
  ServiceType,
  TransportTypeQuickOrderWidget,
  WrapperAbsenceService,
  WrapperQuickOrderWidget,
  WrapperQuickOrderWidgetTitle,
  WrapperService
} from './styledQuickOrderWidget';
import { Select } from 'antd';
import './styles.scss';
import {
  QuickOrderServiceEnum,
  QuickOrderServiceLinks,
  QuickOrderServiceTitle
} from 'modules/DashboardCards/constants/general.constants';
import Taxi from 'shared/components/Images/cars/taxi.svg';
import Public from 'shared/components/Images/cars/public.svg';
import Pers from 'shared/components/Images/cars/pers.svg';
import Carsh from 'shared/components/Images/cars/carsh.png';
import Transfer from 'shared/components/Images/cars/transfer.png';
import Bus from 'shared/components/Images/cars/bus.svg';

interface QuickOrderWidgetProps {
  fullWidth?: boolean;
}

const QuickOrderWidget: FC<QuickOrderWidgetProps> = ({ fullWidth }) => {
  const [services, setServices] = useState<string[]>([]);

  const quickOrderService = [
    {
      value: QuickOrderServiceEnum.TAXI, label: QuickOrderServiceTitle[QuickOrderServiceEnum.TAXI], disabled: services.length >= 2 && !services.includes(QuickOrderServiceEnum.TAXI) && true,
    },
    {
      value: QuickOrderServiceEnum.PUBLIC, label: QuickOrderServiceTitle[QuickOrderServiceEnum.PUBLIC], disabled: services.length >= 2 && !services.includes(QuickOrderServiceEnum.PUBLIC) && true,
    },
    {
      value: QuickOrderServiceEnum.PERSONAL, label: QuickOrderServiceTitle[QuickOrderServiceEnum.PERSONAL], disabled: services.length >= 2 && !services.includes(QuickOrderServiceEnum.PERSONAL) && true,
    },
    {
      value: QuickOrderServiceEnum.BUS, label: QuickOrderServiceTitle[QuickOrderServiceEnum.BUS], disabled: services.length >= 2 && !services.includes(QuickOrderServiceEnum.BUS) && true,
    },
    {
      value: QuickOrderServiceEnum.TRANSFER, label: QuickOrderServiceTitle[QuickOrderServiceEnum.TRANSFER], disabled: services.length >= 2 && !services.includes(QuickOrderServiceEnum.TRANSFER) && true,
    },
    {
      value: QuickOrderServiceEnum.CARSHARING, label: QuickOrderServiceTitle[QuickOrderServiceEnum.CARSHARING], disabled: services.length >= 2 && !services.includes(QuickOrderServiceEnum.CARSHARING) && true,
    },
  ];

  useEffect(() => {
    const QuickOrderServices = localStorage.getItem('QuickOrderServices') ? localStorage.getItem('QuickOrderServices').split(',') : [];
    setServices(QuickOrderServices);
  }, []);

  const changeImageForTransport = (type: QuickOrderServiceEnum) => {
    switch (type) {
      case QuickOrderServiceEnum.TAXI: {
        return Taxi;
      }
      case QuickOrderServiceEnum.PERSONAL: {
        return Pers;
      }
      case QuickOrderServiceEnum.CARSHARING: {
        return Carsh;
      }
      case QuickOrderServiceEnum.PUBLIC: {
        return Public;
      }
      case QuickOrderServiceEnum.TRANSFER: {
        return Transfer;
      }
      case QuickOrderServiceEnum.BUS: {
        return Bus;
      }
    }
  };

  const handleChangeServices = e => {
    localStorage.setItem('QuickOrderServices', e);
    setServices(e);
  };

  return (
    <WrapperQuickOrderWidget fullWidth={fullWidth}>
      <WrapperQuickOrderWidgetTitle>
        <QuickOrderWidgetTitle>
          Быстрый заказ
        </QuickOrderWidgetTitle>
        <Select
          mode="multiple"
          value={services}
          onChange={handleChangeServices}
          dropdownClassName="QuickOrderWidget_customDropdown"
          className="QuickOrderWidget_customSelect"
          options={quickOrderService}
        />
      </WrapperQuickOrderWidgetTitle>
      {services.length >= 1 ? services.map((el: QuickOrderServiceEnum) => (
        <WrapperService fullWidth={fullWidth}>
          <div>
            <TransportTypeQuickOrderWidget src={changeImageForTransport(el)} alt="TransportType" />
            <ServiceType>
              {QuickOrderServiceTitle[el]}
            </ServiceType>
          </div>
          <Link to={QuickOrderServiceLinks[el]}>
            <ServiceButton>
              Заказать
            </ServiceButton>
          </Link>
        </WrapperService>
      ))
        : (
          <WrapperAbsenceService>
            Нет избранных сервисов
          </WrapperAbsenceService>
        )}
    </WrapperQuickOrderWidget>
  );
};

export default QuickOrderWidget;
