import { FC } from 'react';
import { createPortal } from 'react-dom';
import { ReactComponent as RouteIcon } from 'assets/icons/routeIcon.svg';
import styles from './RouteButton.module.scss';

interface RouteButtonProps {
  open: () => void;
}

const RouteButton: FC<RouteButtonProps> = ({ open }) => {
  return createPortal(
    <div className={styles.routeButton} onClick={open}>
      <RouteIcon />
    </div>,
    document.getElementById('root')!
  );
};

export default RouteButton;
