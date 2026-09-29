import { FC } from 'react';
import { useNavigate } from 'react-router';
import cn from 'classnames';

import { routes } from 'constants/routes.constants';
import Button from 'components/Button/Button';

import { ReactComponent as WarningIcon } from 'assets/icons/warning.svg';
import styles from './PhoneConfirmationAlert.module.scss';

interface Props {
  className?: string;
}

const PhoneConfirmationAlert: FC<Props> = ({ className }) => {
  const navigate = useNavigate();

  const goToPhoneConfirm = () => {
    navigate(routes.PhoneConfirm);
  };

  return (
    <Button
      block
      color="default"
      className={cn(styles.warning, [className])}
      onClick={goToPhoneConfirm}
    >
      <WarningIcon />
      <span>Подтвердить номер телефона</span>
    </Button>
  );
};

export default PhoneConfirmationAlert;
