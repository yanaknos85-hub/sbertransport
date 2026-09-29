import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { flatsDescription } from '../constants';
import { FlatItem } from '../FlatItem/FlatItem';
import { useGetFurniture } from '../useGetFurniture';
import * as S from './FlatList.style';

export const FlatList: FC = observer(() => {
  const { cargoStore } = useAppStoreContext();

  useGetFurniture();

  return (
    <S.Wrapper>
      {flatsDescription.map(({
        title, text, icon, type,
      }, index) => (
        <FlatItem
          key={title}
          icon={icon}
          title={title}
          text={text}
          isSelected={index === cargoStore.selectedFlat.idx}
          onClick={() => {
            cargoStore.setSelectedFlatType(index, type);
          }}
        />
      ))}
    </S.Wrapper>
  );
});
