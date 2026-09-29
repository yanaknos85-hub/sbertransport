import React, { FC } from 'react';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { StoreNames, useAppStore } from 'stores';
import { SDO_SUPPORT_PHONE, SUPPORT_PHONE } from 'constants/constants.app';
import {
  Wrapper, StyledTitle, StyledText, StyledPhone, StyledButton
} from './AccessError.styled';

export const AccessError: FC = () => {
  const { [StoreNames.configStore]: configStore } = useAppStore();
  const { authStore } = useAppStoreContext();

  const logout = () => {
    authStore.logout();
  };

  const phone = configStore.env.IS_SDO ? SDO_SUPPORT_PHONE : SUPPORT_PHONE;

  return (
    <Wrapper>
      <StyledTitle level={3}>К сожалению у вас недостаточно прав.</StyledTitle>
      <StyledText>
        При необходимости обратитесь в Поддержку:
        <StyledPhone href={`tel:${phone.code}`}>{phone.title}</StyledPhone>
      </StyledText>
      <StyledButton onClick={logout} type="primary">
        Выйти
      </StyledButton>
    </Wrapper>
  );
};
