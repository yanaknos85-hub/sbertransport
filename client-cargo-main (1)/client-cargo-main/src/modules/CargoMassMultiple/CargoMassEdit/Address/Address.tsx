/* eslint-disable react/destructuring-assignment */
import React, { FC, useState } from 'react';
import { FormInstance } from 'antd/es/form/Form';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { RequestListItem } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import {
  Item, ItemContent, ItemLabel, ItemWaypoint, List
} from './Address.style';
import { useFields } from './useFields';

interface Props {
  data: RequestListItem;
  form: FormInstance;
  onAddressInteractionChange: (isInteracting: boolean) => void;
}

type AddressSide = 'sender' | 'recipient';

const Address: FC<Props> = ({
  data,
  form,
  onAddressInteractionChange,
}) => {
  const [isBlurSender, setIsBlurSender] = useState(false);
  const [isSelectedSender, setIsSelectedSender] = useState(false);

  const [isBlurRecipient, setIsBlurRecipient] = useState(false);
  const [isSelectedRecipient, setIsSelectedRecipient] = useState(false);

  const { [StoreNames.employeeStore]: employeeStore } = useAppStoreContext();

  const field = useFields(data);

  const handleEmployeeSelect = (side: AddressSide) => {
    if (side === 'sender') {
      setTimeout(() => {
        const employee = employeeStore.employeeAutocompleteSelected[0];

        if (employee) {
          form.setFields([{ name: field.senderPhone.name, value: employee.mobilePhone }]);
        }
      }, 0);

      return;
    }

    setTimeout(() => {
      const employee = employeeStore.employeeAutocompleteSelected[1];

      if (employee) {
        form.setFields([{ name: field.recipientPhone.name, value: employee.mobilePhone }]);
      }
    }, 0);
  };

  const handleAddressBlur = (side: AddressSide) => {
    if (side === 'sender') {
      setIsBlurSender(true);
      return;
    }

    setIsBlurRecipient(true);
  };

  const handleAddressSearch = (side: AddressSide, value: string) => {
    if (value.length <= 3) {
      return;
    }

    onAddressInteractionChange(true);

    if (side === 'sender') {
      setIsSelectedSender(true);
      return;
    }

    setIsSelectedRecipient(true);
  };

  const handleAddressSelect = (side: AddressSide) => {
    onAddressInteractionChange(false);

    if (side === 'sender') {
      setIsBlurSender(false);
      setIsSelectedSender(false);
      return;
    }

    setIsBlurRecipient(false);
    setIsSelectedRecipient(false);
  };

  return (
    <List>
      <Item>
        <ItemLabel>
          <ItemWaypoint color="green">A</ItemWaypoint>
          {' '}
          Откуда
        </ItemLabel>

        <ItemContent>
          <FormField
            {...field.senderAddress}
            params={{
              isTooltip: isSelectedSender && isBlurSender,
              onBlur: () => handleAddressBlur('sender'),
              onSelect: () => handleAddressSelect('sender'),
              onSearch: (value: string) => handleAddressSearch('sender', value),
            }}
          />

          <FormField
            {...field.senderName}
            params={{
              onSelect: () => handleEmployeeSelect('sender'),
            }}
          />

          <FormField {...field.senderPhone} />
          <FormField {...field.senderOrganization} />
          <FormField {...field.sourceLoaders} />
        </ItemContent>
      </Item>

      <Item>
        <ItemLabel>
          <ItemWaypoint color="blue">B</ItemWaypoint>
          {' '}
          Куда
        </ItemLabel>

        <ItemContent>
          <FormField
            {...field.recipientAddress}
            params={{
              isTooltip: isSelectedRecipient && isBlurRecipient,
              onBlur: () => handleAddressBlur('recipient'),
              onSelect: () => handleAddressSelect('recipient'),
              onSearch: (value: string) => handleAddressSearch('recipient', value),
            }}
          />

          <FormField
            {...field.recipientName}
            params={{
              onSelect: () => handleEmployeeSelect('recipient'),
            }}
          />

          <FormField {...field.recipientPhone} />
          <FormField {...field.recipientOrganization} />
        </ItemContent>
      </Item>
    </List>
  );
};

export default Address;
