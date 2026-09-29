import React from 'react';
import { useHistory } from 'react-router-dom';
import { observer } from 'mobx-react';

import { ReactComponent as WarningIcon } from 'shared/icons/warning.svg';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';

import { Container, Description, Title } from './PhoneConfirmationAlert.styled';
import { PROFILE } from 'constants/constants.routes';

const PhoneConfirmationAlert: React.FC = observer(() => {
  const history = useHistory();
  const {
    [StoreNames.selfStore]: { selfEmployee },
  } = useAppStoreContext();

  const { isPhoneConfirmed, mobilePhone } = selfEmployee;

  const onGoToProfile = (): void => {
    history.push(PROFILE);
  };

  if (mobilePhone && isPhoneConfirmed) return null;

  return (
    <Container onClick={onGoToProfile}>
      <WarningIcon />
      <div>
        <Title>{`${mobilePhone ? 'Подтвердите' : 'Добавьте'} номер телефона в личном кабинете`}</Title>
        <Description>Это необходимо для корректной работы уведомлений</Description>
      </div>
    </Container>
  );
});

export default PhoneConfirmationAlert;
