import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';
import CargoTypeIcon from 'shared/components/Cargo/CargoTypeIcon/CargoTypeIcon';
import { CustomInputNumber } from 'shared/components/CustomInputNumber/CustomInputNumber';

import { CargoTypeCategoryNameEnum, cargoTypeNameTitle } from 'stores/CargoType/CargoType.interface';
import { CargoListItem } from 'types/Cargo';

import CargoForm from './CargoForm';
import * as S from './Cargos.style';
import { ItemOption, ItemOptionWrapper } from './ItemOption';

export interface Props {
  position: number;
  cargo: CargoListItem;
  logger: any;
  error: string;
  onEdit: (cargo: CargoListItem, onSuccess: () => void) => void;
  onDelete: (position: number) => void;
  onCancel: () => void;
}

const CargoItem: FC<Props> = observer(({
  position, cargo, logger, error, onEdit, onDelete, onCancel,
}) => {
  const {
    cargoName,
    volume,
    length,
    width,
    height,
    weight,
    cargoType,
    category,
    needPackage,
    occupiedPlacesCount,
  } = cargo;

  const [isEdit, setIsEdit] = useState(false);

  const handleDelete = () => {
    onDelete(position);
  };

  const handleSave = (_cargo: CargoListItem) => {
    onEdit(_cargo, () => {
      setIsEdit(false);
    });
  };

  const handleCancel = () => {
    setIsEdit(false);
    onCancel();
  };

  const isOversized = (
    category === CargoTypeCategoryNameEnum.BULK || category === CargoTypeCategoryNameEnum.LIQUID
  );

  if (isEdit) {
    return (
      <CargoForm
        position={position}
        cargo={cargo}
        error={error}
        initialValues={{
          cargoName,
          occupiedPlacesCount,
          needPackage,
        }}
        logger={logger}
        onCancel={handleCancel}
        onSave={handleSave}
      />
    );
  }

  return (
    <S.Container>
      <S.CargoItem>
        <S.CargoItemInner>
          <S.CargoItemContentTop>
            <S.CargoItemDescription>
              <S.CargoItemName>{cargoName}</S.CargoItemName>
              <S.CargoItemType>
                <CargoTypeIcon name={cargoType} />
                <S.CargoItemTypeName>{cargoTypeNameTitle[cargoType]}</S.CargoItemTypeName>
              </S.CargoItemType>
            </S.CargoItemDescription>
            <S.CargoItemButtons>
              <S.CargoItemBtnDelete onClick={handleDelete} />
            </S.CargoItemButtons>
          </S.CargoItemContentTop>
          <S.Divider />
          <S.CargoItemContentBottom>
            <S.CargoItemOptions>
              {!isOversized && (
                <>
                  <ItemOption
                    title="Сумма габаритов сторон, см"
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
                    // Объём приходит в мм3, отображается в м3, для расчёта тарифа уходит в см3
                    weight={volume / 1_000_000}
                  />
                </>
              )}
              <ItemOptionWrapper
                title="Количество"
              >
                <CustomInputNumber
                  width="100px"
                  height="30px"
                  min={1}
                  value={occupiedPlacesCount}
                  defaultValue={1}
                  onChange={value => { handleSave({ ...cargo, occupiedPlacesCount: +(value || 1) }); }}
                />
              </ItemOptionWrapper>
            </S.CargoItemOptions>
          </S.CargoItemContentBottom>
        </S.CargoItemInner>
      </S.CargoItem>
    </S.Container>
  );
});

export default CargoItem;
