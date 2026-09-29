import React, { FC } from 'react';
import { Divider } from 'antd';
import { observer } from 'mobx-react';
import CargoTypeIcon from 'shared/components/Cargo/CargoTypeIcon/CargoTypeIcon';

import { CargoTypeCategoryNameEnum, cargoTypeNameTitle } from 'stores/CargoType/CargoType.interface';
import { CargoListItem } from 'types/Cargo';

import {
  Item,
  ItemBody,
  ItemDescription,
  ItemName,
  ItemParams,
  ItemSection,
  ItemType,
  ItemTypeName
} from './CargoList.style';
import { ItemOption } from './ItemOption';

interface Props {
  data: CargoListItem;
}

const CargoItem: FC<Props> = observer(({ data }) => {
  const {
    cargoName, length, width, height, weight, volume, occupiedPlacesCount, cargoType, cargoCategory,
  } = data;

  const isOversized = (
    cargoCategory === CargoTypeCategoryNameEnum.BULK || cargoCategory === CargoTypeCategoryNameEnum.LIQUID
  );

  return (
    <Item>
      <ItemBody>
        <ItemSection>
          <ItemDescription>
            <ItemName>{cargoName}</ItemName>
            <ItemType>
              <CargoTypeIcon name={cargoType} />
              <ItemTypeName>{cargoTypeNameTitle[cargoType]}</ItemTypeName>
            </ItemType>
          </ItemDescription>
        </ItemSection>
        <Divider style={{ margin: '12px 0' }} />
        <ItemSection>
          <ItemParams>
            {!isOversized && (
              <>
                <ItemOption
                  title="Габариты, см"
                  length={length}
                  width={width}
                  height={height}
                />
                <ItemOption
                  title="Вес, кг"
                  weight={weight}
                />
              </>
            )}
            {isOversized && (
              <>
                <ItemOption
                  title="Объём м3"
                  // Необходимо делить, чтобы получить корректные данные на карточке в м3
                  // Объём приходит в мм3, отображается в м3, для расчёта тарифа уходит в см3
                  weight={volume / 1_000_000}
                />
              </>
            )}
            <ItemOption
              title="Количество"
              occupiedPlacesCount={occupiedPlacesCount}
            />
          </ItemParams>
        </ItemSection>
      </ItemBody>
    </Item>
  );
});

export default CargoItem;
