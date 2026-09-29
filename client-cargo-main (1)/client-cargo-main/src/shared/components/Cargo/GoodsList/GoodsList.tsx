import React from 'react';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import * as S from './GoodsList.style';

export const GoodsList = () => {
  const {
    [StoreNames.cargoStore]: cargoStore,
  } = useAppStoreContext();

  const { packages } = cargoStore.packageForm;

  const packageList = packages ? packages.map(item => (
    `${item.packageTitle} x ${item.packageCount}`
  )) : [];

  if (packageList.length === 0) {
    return null;
  }

  return (
    <S.Wrapper>
      {packageList.map(item => (
        <S.Item key={item}>{item}</S.Item>
      ))}
    </S.Wrapper>
  );
};
