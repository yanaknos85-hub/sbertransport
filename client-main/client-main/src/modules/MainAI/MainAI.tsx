import React, {
  FC, useState, useCallback, useRef
} from 'react';

import { Link } from 'react-router-dom';

import { HOME } from 'constants/constants.routes';

import arrowLeftIcon from 'shared/icons/arrow-left.svg';
import logoSvg from 'shared/images/logo-white.svg';
import penBorderIcon from 'shared/icons/pen-border.svg';

import BackgroundDecor from './components/BackgroundDecor/BackgroundDecor';
import UserBlock from './components/UserBlock/UserBlock';
import Chat, { type ChatRef } from './components/Chat/Chat';

import styles from './MainAI.module.scss';

const APP_VERSION = 'Бета-версия';

const MainAI: FC = () => {
  const [hasMessages, setHasMessages] = useState(false);
  const chatRef = useRef<ChatRef>(null);

  const handleNewChat = useCallback(() => {
    chatRef.current?.newChat();
  }, []);

  return (
    <BackgroundDecor hasMessages={hasMessages}>
      <header className={styles.header}>
        <div className={styles.headerCol}>
          <img src={logoSvg} alt="SberTransport" />
        </div>
        <div className={styles.headerCol}>
          <button
            className={styles.newChatButton}
            onClick={handleNewChat}
          >
            <img
              src={penBorderIcon}
              alt=""
              className={styles.newChatIcon}
            />
            <span>Новый чат</span>
          </button>
        </div>
        <div className={styles.headerCol}>
          <UserBlock />
        </div>
      </header>

      <main className={styles.main}>
        <div className={`${styles.titleBlock}${hasMessages ? ` ${styles.titleBlockHidden}` : ''}`}>
          <h1 className={styles.title}>
            <span className={styles.titleLine1}>AI-помощник</span>
            <span className={styles.titleLine2}>транспортных услуг</span>
          </h1>
          <p className={styles.subtitle}>
            Привет! Я — корпоративный помощник по транспортным услугам.
            <br />
            Просто напишите, что нужно сделать, и я подключу нужный модуль: закажу перевозку
            <br />
            или доставку, забронирую парковку, сделаю заявку на обслуживание авто или просто помогу
            <br />
            с вопросом по транспорту в банке.
          </p>
        </div>

        <Chat onHasMessages={setHasMessages} ref={chatRef} />
      </main>

      <footer className={styles.footer}>
        <div className={styles.footerLeft}>
          <Link to={HOME} className={styles.backLink}>
            <img
              src={arrowLeftIcon}
              alt=""
              className={styles.backIcon}
            />
            <span>Вернуться в исходное меню</span>
          </Link>
        </div>
        <div className={styles.footerRight}>
          <span className={styles.version}>
            {APP_VERSION}
          </span>
        </div>
      </footer>
    </BackgroundDecor>
  );
};

export default MainAI;
