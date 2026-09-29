import React, { FC } from 'react';
import { Divider } from 'antd';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { getWeight } from 'utils';

import * as S from './Parameters.style';

export const Parameters: FC = observer(() => {
  const {
    [StoreNames.cargoStore]: cargoStore,
  } = useAppStoreContext();

  const { cargoList } = cargoStore.stepCargosValues;

  // Фильтруем только те элементы, у которых количество > 0
  const filteredCargoList = cargoList.filter(item => item.occupiedPlacesCount > 0);

  // Подсчёт общих параметров
  const totals = filteredCargoList.reduce(
    (acc, item) => {
      acc.occupiedPlacesCount += item.occupiedPlacesCount;
      acc.weight += item.weight * item.occupiedPlacesCount;
      acc.volume += item.volume * item.occupiedPlacesCount;
      return acc;
    },
    {
      occupiedPlacesCount: 0, weight: 0, volume: 0,
    }
  );

  const {
    occupiedPlacesCount: countTotal, weight: weightTotal, volume: volumeTotal,
  } = totals;

  // Список грузов
  const description = cargoList.map(item => {
    return `${item.cargoName} ${item.occupiedPlacesCount !== 1 ? `x ${item.occupiedPlacesCount}` : ''}`;
  }).join(', ');

  const getVolume = (value = 0) => {
    const MIN_VALUE = 0;
    const resValue = value / 1e6;
    const val = resValue < MIN_VALUE ? MIN_VALUE : (resValue).toFixed(3);
    return `${val}  м³`;
  };

  return (
    <S.Wrapper>
      <S.Title>Параметры груза</S.Title>
      <S.Content>
        {description.length > 0 ? description : 'Пока ничего не выбрано'}
      </S.Content>
      <Divider style={{ margin: '8px 0' }} />
      <S.ParamWrapper>
        <S.ParamItem>
          <S.ParamTitle>Объем, м³</S.ParamTitle>
          <S.ParamValue>{getVolume(volumeTotal)}</S.ParamValue>
        </S.ParamItem>
        <S.ParamItem>
          <S.ParamTitle>Вес, кг</S.ParamTitle>
          <S.ParamValue>{getWeight(weightTotal)}</S.ParamValue>
        </S.ParamItem>
        <S.ParamItem>
          <S.ParamTitle>Кол-во, ед</S.ParamTitle>
          <S.ParamValue>{countTotal}</S.ParamValue>
        </S.ParamItem>
      </S.ParamWrapper>
    </S.Wrapper>
  );
});
