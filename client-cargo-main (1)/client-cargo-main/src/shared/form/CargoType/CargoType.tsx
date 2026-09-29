import React, { FC, useState } from 'react';
import { IEmployeeStore } from '@sber-sbertransport/mf-core';
import { AutoComplete, AutoCompleteProps, Divider } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ICargoTypeStore } from 'stores/CargoType/CargoType.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { SelectDropdownButton, StyledCargoType } from './CargoType.style';
import ModalForm from './ModalForm';

const CargoType: FC<AutoCompleteProps> = observer(({ ...rest }) => {
  const {
    [StoreNames.cargoTypeStore]: cargoTypeStore,
    [StoreNames.employeeStore]: employeeStore,
  }: { cargoTypeStore: ICargoTypeStore; employeeStore: IEmployeeStore }
    = useAppStoreContext();
  const {
    cargoTypeAutocompleteList, searchCargoType, onCargoTypeSelect,
  } = cargoTypeStore;
  const [isVisibleModal, setIsVisibleModal] = useState(false);
  const [enteredCargoName, setEnteredCargoName] = useState('');

  const { organizationId } = employeeStore.selfEmployee;

  return (
    <>
      <StyledCargoType>
        <AutoComplete
          notFoundContent="Позиция не найдена"
          {...rest}
          onSearch={value => {
            setEnteredCargoName(value);
            searchCargoType(value, organizationId);
            if (rest.onSearch) {
              rest.onSearch(value);
            }
          }}
          // @ts-ignore
          onSelect={(value, option) => {
            setEnteredCargoName('');
            // В методе onCargoTypeSelect происходит приведение размеров и объёма из мм в см
            // Так бэк принимает данные только в см
            onCargoTypeSelect(value, true);
            if (rest.onSelect) {
              rest.onSelect(value, option);
            }
          }}
          className="ant-select-customize-input"
          dropdownRender={options => (
            <>
              {options}
              <Divider />
              <SelectDropdownButton
                type="button"
                onClick={() => {
                  setIsVisibleModal(true);
                }}
              >
                <span>Создать новый груз</span>
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
        onCancel={() => {
          setIsVisibleModal(false);
          setEnteredCargoName('');
        }}
        initialCargoName={enteredCargoName}
      />
    </>
  );
});

export default CargoType;
