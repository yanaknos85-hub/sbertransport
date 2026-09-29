/* eslint-disable no-console */
/* eslint-disable react-hooks/exhaustive-deps */
import { useEffect, useState } from 'react';
import moment from 'moment';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';

import { FILL_FORM } from '../constants';

// Этот хук используется только для автозаполнения, для теста
// Поэтому не будет  на продакшене в нем встроены ограничения FILL_FORM

const useFill = (): {
  filledAddress: boolean;
  filledCargos: boolean;
} => {
  const {
    [StoreNames.selfStore]: selfStore,
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.cargoStore]: cargoStore,
    [StoreNames.cargoTypeStore]: cargoTypeStore,
    [StoreNames.geoStore]: geoStore,
  } = useAppStoreContext();

  const [fillAddressLoaded, setFillAddressLoaded] = useState(false);
  const [fillEmployeesLoaded, setFillEmployeesLoaded] = useState(false);
  const [fillCargosLoaded, setFillCargosLoaded] = useState(false);

  const address = {
    senderName: selfStore.selfEmployee.fullNameWithCode || '',
    senderPhone: selfStore.selfEmployee.mobilePhone || '',
    senderAddress: '',
    senderOrganization: '',
    sourceLoaders: true,
    desiredDate: moment(new Date()),

    recipientName: '',
    recipientAddress: '',
    recipientPhone: '',
    recipientOrganization: '',
    destinationLoaders: false,
  };

  useEffect(() => {
    if (FILL_FORM) {
      cargoStore.setStepAddressValues({
        ...cargoStore.stepAddressValues,
        ...address,
      });

      geoStore.searchLocation('Москва');
      employeeStore.searchEmployees('Поля');
      cargoTypeStore.searchCargoType(' ');
    }
  }, []);

  useEffect(() => {
    const setFillEmployees = async () => {
      const list = ['Василий Сергеевич Поляков (1729581)'];

      if (!list.every((r: any) => employeeStore.employeeAutocompleteList.map(x => x.fullNameWithCode).includes(r))) {
        console.log('FillForm: employees не найдены');
        return;
      }
      await employeeStore.onEmployeeSelect(list[0], 0, true);
      await employeeStore.onEmployeeSelect(list[0], 1, true);
    };

    if (FILL_FORM && !fillEmployeesLoaded && employeeStore.employeeAutocompleteList.length) {
      console.log('FillForm: выбираю employees...');
      setFillEmployees();
    }
  }, [employeeStore.employeeAutocompleteList]);

  useEffect(() => {
    if (
      FILL_FORM
      && !fillEmployeesLoaded
      && employeeStore.employeeAutocompleteSelected[0]
      && employeeStore.employeeAutocompleteSelected[1]
    ) {
      cargoStore.setStepAddressValues({
        ...cargoStore.stepAddressValues,
        sender: employeeStore.employeeAutocompleteSelected[0],
        recipient: employeeStore.employeeAutocompleteSelected[1],
      });
      console.log('FillForm:  employee выбраны');
      setFillEmployeesLoaded(true);
    }
  }, [employeeStore.employeeAutocompleteSelected.length]);

  useEffect(() => {
    const setFillAddress = async () => {
      const list = ['Тихорецкий бульвар, 1 ст6, Москва', 'Комсомольская площадь, 2, Москва'];
      if (!list.every((r: any) => geoStore.addressAutocompleteList.map(x => x.addressString).includes(r))) {
        console.log('FillForm: адреса не найдены');
        return;
      }
      await geoStore.onAddressSelect(list[0], '', 0, true);
      await geoStore.onAddressSelect(list[1], '', 1, true);
    };
    if (FILL_FORM && !fillAddressLoaded && geoStore.addressAutocompleteList.length) {
      console.log('FillForm: выбираю адреса...');
      setFillAddress();
    }
  }, [geoStore.addressAutocompleteList]);

  useEffect(() => {
    if (FILL_FORM && !fillAddressLoaded && geoStore.waypoints[0].addressString && geoStore.waypoints[1].addressString) {
      cargoStore.setStepAddressValues({
        ...cargoStore.stepAddressValues,
        senderAddress: geoStore.waypoints[0].addressString,
        recipientAddress: geoStore.waypoints[1].addressString,
      });
      console.log('FillForm: адреса выбраны');
      setFillAddressLoaded(true);
    }
  }, [geoStore.waypoints]);

  useEffect(() => {
    const setFillCargoType = async () => {
      const item = 'Стол';
      const list = [item];
      if (!list.every((r: any) => cargoTypeStore.cargoTypeAutocompleteList.map(x => x.name).includes(r))) {
        console.log('FillForm: грузы не найдены');
        return;
      }
      await cargoTypeStore.onCargoTypeSelect(item, true);
    };
    if (FILL_FORM && !fillCargosLoaded && cargoTypeStore.cargoTypeAutocompleteList.length) {
      console.log('FillForm: выбираю грузы...');
      setFillCargoType();
    }
  }, [cargoTypeStore.cargoTypeAutocompleteList]);

  useEffect(() => {
    if (FILL_FORM && !fillCargosLoaded && cargoTypeStore.cargoType) {
      const cargo = cargoTypeStore.cargoType;

      cargoStore.setStepCargosValues({
        cargoList: [
          {
            position: 0,
            id: cargo.id,
            cargoName: cargo.name,
            cargoType: cargo.type,
            cargoCategory: cargo.category,
            category: cargo.category,
            width: cargo.width,
            length: cargo.length,
            height: cargo.height,
            volume: cargo.volume,
            weight: cargo.weight,
            occupiedPlacesCount: 1,
            needPackage: true,
          },
        ],
      });
      console.log('FillForm: грузы выбраны');
      setFillCargosLoaded(true);
    }
  }, [cargoTypeStore.cargoType]);

  return {
    filledAddress: fillAddressLoaded && fillEmployeesLoaded,
    filledCargos: fillCargosLoaded,
  };
};

export default useFill;
