import { FC } from 'react';
import { Button } from 'antd-mobile';
import { observer } from 'mobx-react';
import { useAppStore } from 'stores/stores.context';
import styles from './OnlineButton.module.scss';

const OnlineButton: FC = observer(() => {
  const { mainLayoutStore } = useAppStore();

  const toggleOnline = () => {
    mainLayoutStore.askConfirmVehicle();
  };

  return (
    <Button
      block
      color="primary"
      size="large"
      className={styles['online-button']}
      onClick={toggleOnline}
    >
      Выйти на линию
    </Button>
  );
});

export default OnlineButton;
