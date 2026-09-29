import React, {
  FC, useCallback,
  useEffect, useState
} from 'react';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { CargoListItem } from 'types/Cargo';

import * as S from './Furniture.style';
import { FurnitureItem } from './FurnitureItem/FurnitureItem';

interface FurnitureProps {
  onCargoChange?: () => void;
}

export const Furniture: FC<FurnitureProps> = observer(({ onCargoChange }) => {
  const { [StoreNames.cargoStore]: cargoStore } = useAppStoreContext();
  const [items, setItems] = useState<CargoListItem[]>([]);

  // Инициализация данных из store
  useEffect(() => {
    const savedFurniture = cargoStore.stepCargosValues.cargoList;

    if (savedFurniture.length > 0) {
      setItems(savedFurniture);
    }
  }, [cargoStore.stepCargosValues.cargoList]); // Запускается только при монтировании

  // Обработка изменения количества
  const handleChange = useCallback((id: string, newValue?: number) => {
    setItems(prevItems => {
      const updatedItems = prevItems.map(item => {
        if (item.id === id) {
          return { ...item, occupiedPlacesCount: Number(newValue) };
        }
        return item;
      });

      cargoStore.setCargoList(updatedItems);

      if (onCargoChange) {
        onCargoChange();
      }

      return updatedItems;
    });
  }, [cargoStore, onCargoChange]);

  return (
    <S.Wrapper>
      <S.Title>Вещи, мебель и техника</S.Title>
      {items.length > 0 ? (
        items.map(item => (
          <FurnitureItem
            key={item.id}
            title={item.cargoName}
            occupiedPlacesCount={item.occupiedPlacesCount}
            onChange={newValue => {
              handleChange(item.id, newValue);
            }}
          />
        ))
      ) : (
        <div>Нет доступных предметов</div>
      )}
    </S.Wrapper>
  );
});
