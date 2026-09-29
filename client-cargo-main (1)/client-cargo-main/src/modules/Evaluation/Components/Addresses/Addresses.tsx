import React, { FC } from 'react';
import { Link } from 'react-router-dom';
import { FireFilled } from '@ant-design/icons';
import { Badge } from 'antd';
import AddressBlockMulti from 'shared/components/Cargo/AddressBlockMulti/AddressBlockMulti';
import CargoTariffTag from 'shared/components/Cargo/CargoTariffTag/CargoTariffTag';
import TButton from 'shared/ui/Button/Button';

import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import {
  AddressSide, BottomRow,
  CargoTariffDivFeedback,
  Content, DateSide, MiddleRow, TopRow
} from './Address.style';
import { convertDate, getTariffText } from './utils';

interface Props {
  request: CargoRequestModel;
  onClickDetailedHandler?: (id: string) => void;
}

export const Addresses: FC<Props> = ({ request, onClickDetailedHandler }) => {
  return (
    <Content>
      <TopRow>
        <CargoTariffDivFeedback>
          {request?.express ? (
            <Badge
              count={(
                <FireFilled
                  style={{
                    color: '#f5222d',
                    fontSize: '7px',
                    backgroundColor: '#f2f3f6',
                    borderRadius: '5px',
                  }}
                />
              )}
              offset={[request.transportType === TransportTypeEnum.DEDICATED ? -155 : -145, 16]}
            />
          ) : (
            ''
          )}
          <CargoTariffTag express="true">
            {getTariffText(request)}
          </CargoTariffTag>
        </CargoTariffDivFeedback>
        <DateSide>
          <div>
            Доставлено
            {' '}
            <span>{convertDate(request.desiredDate)}</span>
          </div>
        </DateSide>
      </TopRow>
      <MiddleRow>
        <AddressSide>
          <AddressBlockMulti waypoints={request?.waypoints} />
        </AddressSide>
      </MiddleRow>
      <BottomRow>
        <Link to={request.id}>
          <TButton
            $size="small"
            style={{
              backgroundColor: 'inherit',
              color: '#10BF6A',
              border: 'none',
              boxShadow: 'none',
            }}
            onClick={(): void => onClickDetailedHandler && onClickDetailedHandler(request.id)}
          >
            Подробнее
          </TButton>
        </Link>
      </BottomRow>
    </Content>
  );
};
