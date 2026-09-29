import { ILogger } from '@sber-sbertransport/mf-core';
import { AutoComplete, AutoCompleteProps, Divider } from 'antd';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ICargoTypeStore } from 'stores/CargoType/CargoType.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { SelectDropdownButton, StyledCargoType } from './CargoType.style';
import ModalForm from './ModalForm';

const CargoType: FC<AutoCompleteProps> = observer(({ ...rest }) => {
  const { [StoreNames.cargoTypeStore]: cargoTypeStore, logger }: { cargoTypeStore: ICargoTypeStore; logger: ILogger }
    = useAppStoreContext();
  const {
    cargoTypeAutocompleteList, searchCargoType, postCargoType, onCargoTypeSelect,
  } = cargoTypeStore;
  const [isVisibleModal, setIsVisibleModal] = useState(false);

  return (
    <>
      <StyledCargoType>
        <AutoComplete
          notFoundContent="Позиция не найдена"
          {...rest}
          onSearch={value => {
            searchCargoType(value);
            if (rest.onSearch) {
              rest.onSearch(value);
            }
          }}
          onSelect={(value, option) => {
            onCargoTypeSelect(value);
            if (rest.onSelect) {
              rest.onSelect(value, option);
            }
          }}
          className="ant-select-customize-input"
          dropdownRender={options => (
            <>
              {options}
              <Divider />
              <SelectDropdownButton type="button" onClick={() => setIsVisibleModal(true)}>
                <span>Другое</span>
              </SelectDropdownButton>
            </>
          )}
        >
          {cargoTypeAutocompleteList.map(({ id, name }) => (
            <AutoComplete.Option
              value={name}
              key={id}
              title={name}
            >
              {name}
            </AutoComplete.Option>
          ))}
        </AutoComplete>
      </StyledCargoType>
      <ModalForm
        visible={isVisibleModal}
        onSave={() => setIsVisibleModal(false)}
        onCancel={() => setIsVisibleModal(false)}
        postCargoType={postCargoType}
        logger={logger}
      />
    </>
  );
});

export default CargoType;
