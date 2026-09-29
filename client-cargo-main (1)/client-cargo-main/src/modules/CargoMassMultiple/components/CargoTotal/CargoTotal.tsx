import React, { FC, ReactNode } from 'react';
import { Divider } from 'antd';
import TTypography from 'shared/components/Cargo/TTypography';
import { ReactComponent as Distance } from 'shared/images/cargo/total_distance.svg';
import { ReactComponent as Volume } from 'shared/images/cargo/total_volume.svg';
import { ReactComponent as Weight } from 'shared/images/cargo/total_weight.svg';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TotalSizes, transportTypeTitles } from 'types/Cargo';
import { declOfNum } from 'utils/declOfNum';
import * as utils from 'utils/Misc';

import {
  Container,
  Detailing,
  DetailingTextLine,
  Total
} from './CargoTotal.style';
import CargoTotalItem from './CargoTotalItem';
import { DetailingText } from './DetailingText';
import { TotalCost } from './TotalCost';

interface Props {
  distance: number;
  sizes: TotalSizes;
  allCost: number;
  tariffCost: {
    totalCost: number;
    details: {
      [TransportTypeEnum.COURIER]: {
        cost: number;
        count: number;
      };
      [TransportTypeEnum.DEDICATED]: {
        cost: number;
        count: number;
      };
      [TransportTypeEnum.INTERREGIONAL]: {
        cost: number;
        count: number;
      };
      [TransportTypeEnum.INDIVIDUAL]: {
        cost: number;
        count: number;
      };
    };
  };
  countRequests: number;
  bottomElement?: ReactNode;
  topElement?: ReactNode;
}

const CargoTotal: FC<Props> = props => {
  const {
    distance = 0,
    sizes,
    tariffCost,
    countRequests,
    bottomElement,
    topElement,
    allCost,
  } = props;
  const { weight = 0, volume = 0 } = sizes;

  const totalDistance = utils.getDistance(distance);
  const totalWeight = utils.getWeight(weight);
  const totalVolume = utils.getVolume(volume);

  const labelCountRequests = declOfNum(countRequests, ['заявка', 'заявки', 'заявок']);

  const courierTariff = tariffCost.details[TransportTypeEnum.COURIER];
  const dedicatedTariff = tariffCost.details[TransportTypeEnum.DEDICATED];
  const interregionalTariff = tariffCost.details[TransportTypeEnum.INTERREGIONAL];
  const individualTariff = tariffCost.details[TransportTypeEnum.INDIVIDUAL];

  return (
    <Container>
      <DetailingTextLine>
        <TTypography
          size="20px"
          weight={600}
          font="SB Sans Interface"
        >
          Итого
        </TTypography>
        <TTypography size="14px" color="#adadad">
          {countRequests}
          {' '}
          {labelCountRequests}
        </TTypography>
      </DetailingTextLine>
      <Total>
        <CargoTotalItem
          img={<Weight />}
          data={totalWeight}
          description="вес"
        />
        <CargoTotalItem
          img={<Volume />}
          data={totalVolume}
          description="объём"
        />
        <CargoTotalItem
          img={<Distance />}
          data={totalDistance}
          description="расстояние"
        />
      </Total>
      <Divider style={{ marginTop: '8px' }} />
      {topElement}
      <Detailing>
        {!!courierTariff.count && (
          <DetailingText
            mb="16px"
            courierTariff={courierTariff}
            title={transportTypeTitles[TransportTypeEnum.COURIER]}
          />
        )}
        {!!dedicatedTariff.count && (
          <DetailingText
            mb="16px"
            courierTariff={dedicatedTariff}
            title={transportTypeTitles[TransportTypeEnum.DEDICATED]}
          />
        )}
        {!!interregionalTariff.count && (
          <DetailingText
            mb="24px"
            courierTariff={interregionalTariff}
            title={transportTypeTitles[TransportTypeEnum.INTERREGIONAL]}
          />
        )}
        {!!individualTariff.count && (
          <DetailingText
            mb="24px"
            courierTariff={individualTariff}
            title={transportTypeTitles[TransportTypeEnum.INDIVIDUAL]}
          />
        )}
      </Detailing>
      <TotalCost allCost={allCost} title="Общая стоимость" />
      {bottomElement}
    </Container>
  );
};

export default CargoTotal;
