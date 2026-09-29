import { useEffect, useState, type FC } from 'react';
import Modal from 'components/Modal/Modal';
import styles from './InstallPWA.module.scss';
import toShare from 'assets/images/to-share.png';
import toHome from 'assets/images/to-home.png';
import savePWA from 'assets/images/save-pwa.png';

const IS_ASKED = 'IS_ASKED';

const isAsked = localStorage.getItem(IS_ASKED) === 'true';

const InstallPWA: FC = () => {
  const [isVisible, setIsVisible] = useState(false);
  const [isIos, setIsIos] = useState(false);
  const [isStandalone, setIsStandalone] = useState(false);

  useEffect(() => {
    const standalone = window.matchMedia('(display-mode: standalone)').matches;

    setIsStandalone(standalone);

    const ios = /iphone|ipad|ipod/i.test(window.navigator.userAgent);
    setIsIos(ios);

    if (ios) {
      setIsVisible(true);
    }

    const handler = (e: Event) => {
      e.preventDefault();
      setIsVisible(true);
    };

    window.addEventListener('beforeinstallprompt', handler);

    return () => {
      window.removeEventListener('beforeinstallprompt', handler);
    };
  }, []);

  const close = () => {
    setIsVisible(false);
    localStorage.setItem(IS_ASKED, 'true');
  };

  if (isStandalone || isAsked || !isIos) return null;

  return (
    <Modal
      visible={isVisible}
      className={styles.modal}
      onClose={close}
      showCloseButton
      content={(
        <div className={styles.container}>
          <span className={styles.header}>Чтобы установить приложение:</span>
          <ol className={styles.steps}>
            <li className={styles.step}>
              <span>В нижней части браузера нажмите "Поделиться"</span>
              <img
                className={styles.image}
                src={toShare}
                alt="to-share"
              />
            </li>
            <li className={styles.step}>
              <span>Пролистайте вниз и нажмите "На экран "Домой""</span>
              <img
                className={styles.image}
                src={toHome}
                alt="to-home"
              />
            </li>
            <li className={styles.step}>
              <span>Нажмите "Добавить"</span>
              <img
                className={styles.image}
                src={savePWA}
                alt="save-pwa"
              />
            </li>
          </ol>
        </div>
            )}
    />
  );
};

export default InstallPWA;
