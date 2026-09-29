/* eslint-disable react/destructuring-assignment */
import React, { FC } from 'react';
import Icon from 'shared/components/Cargo/CargoTypeIcon/CargoTypeIcon';

import { CargoTypeCategoryNameEnum } from 'stores/CargoTariff/CargoTariff.interface';
import { cargoTypeNameTitle } from 'stores/CargoType/CargoType.interface';
import { CargoListItem } from 'types/Cargo';

import * as S from './Cargos.style';
import { ItemOption } from './ItemOption';

interface Props {
  data: CargoListItem[];
  onDelete: (id: number) => void;
}

const Cargos: FC<Props> = ({ data, onDelete }) => (
  <S.List>
    {data.map(cargo => {
      const isOversized = cargo.cargoCategory === CargoTypeCategoryNameEnum.LIQUID || cargo.cargoCategory === CargoTypeCategoryNameEnum.BULK;
      return (
        <S.Item key={cargo.cargoType + cargo.cargoName}>
          <S.Inner top={true}>
            <S.Name>
              {cargo.cargoName}
              {' '}
              <S.Delete len={data.length} onClick={() => onDelete(cargo.position)} />
            </S.Name>
            <S.Caption>
              <Icon name={cargo.cargoType} />
              <S.Type>{cargoTypeNameTitle[cargo.cargoType]}</S.Type>
            </S.Caption>
          </S.Inner>
          <S.Divider />
          <S.Inner bottom={true}>
            <S.Params>
              {!isOversized && (
                <ItemOption
                  title="Габариты"
                  width={cargo.width}
                  length={cargo.length}
                  height={cargo.height}
                />
              )}
              {!isOversized && (
                <ItemOption
                  title="Масса"
                  weight={cargo.weight}
                />
              )}
              {isOversized && (
                <ItemOption
                  title="Объем, м3"
                  weight={cargo.volume / 1_000_000_000}
                />
              )}
              <S.Param>
                <S.ParamLabel>Количество</S.ParamLabel>
                <S.ParamValue>{cargo.occupiedPlacesCount}</S.ParamValue>
              </S.Param>
            </S.Params>
          </S.Inner>
        </S.Item>
      );
    })}
  </S.List>
);

export default Cargos;
