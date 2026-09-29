import React from 'react';
import {
  CarouselItemElement, CoopTrip, Date, Purpose, TransportPictureWrapper, TransportType, TransportTypeTitle, TransportTypeWrapper, TripInfo
} from './styledCarouselItem';
import { zoneTime } from 'utils/trips/times';
import { DATE_FORMAT } from 'constants/constants.app';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import Taxi from 'shared/components/Images/cars/taxi.svg';
import Public from 'shared/components/Images/cars/public.svg';
import Pers from 'shared/components/Images/cars/pers.svg';
import Carsh from 'shared/components/Images/cars/carsh.png';
import Transfer from 'shared/components/Images/cars/transfer.png';
import { Divider } from 'antd';
import { TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';
import { useHistory } from 'react-router-dom';
import { joinUrl } from 'utils';
import { TripRequestModel } from 'stores/Trip/models';

interface CarouselItemProps {
  item: TripRequestModel;
}

const CarouselItem: React.FC<CarouselItemProps> = ({ item }) => {
  const history = useHistory();

  const itemClickHandler = (id: string): void => {
    history.push(joinUrl(`/client/passengers/trips/list/planned/${id}`));
  };

  const changeImageForTransport = (type: TransportTypeEnum) => {
    switch (type) {
      case TransportTypeEnum.TAXI: {
        return Taxi;
      }
      case TransportTypeEnum.PERSONAL: {
        return Pers;
      }
      case TransportTypeEnum.CARSHARING: {
        return Carsh;
      }
      case TransportTypeEnum.PUBLIC: {
        return Public;
      }
      case TransportTypeEnum.GROUP_TRANSFER: {
        return Transfer;
      }
    }
  };

  return (
    <CarouselItemElement onClick={() => itemClickHandler(item.id)}>
      <Date>{zoneTime(item?.desiredDate, DATE_FORMAT.MONTH_NAME_NOT_YEAR_WITH_TIME, item?.timeZone)}</Date>
      <TransportTypeWrapper>
        <TransportPictureWrapper>
          <TransportType src={changeImageForTransport(item.transportType)} alt="TransportType" />
        </TransportPictureWrapper>
        <TripInfo>
          <TransportTypeTitle>
            {`
              ${item.transportType && TransportTypeTitlesEnum[item.transportType]}
              ${item.transportType === TransportTypeEnum.TAXI ? ` • ${TaxiClassTitlesEnum[item.taxiClass]}` : ''}
            `}
          </TransportTypeTitle>
          <CoopTrip>
            {item.coopTrip ? 'Совместная поездка' : 'Индивидуальная поездка' }
          </CoopTrip>
        </TripInfo>
      </TransportTypeWrapper>
      <Divider />
      <Purpose>
        {item.purpose.label}
      </Purpose>
    </CarouselItemElement>
  );
};

export default CarouselItem;
