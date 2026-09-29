import React, {
  FC, useState, useEffect, useCallback
} from 'react';
import { Modal, Tabs, Divider } from 'antd';
import { observer } from 'mobx-react';
import {
  useEtrnCard,
  useAcquireLock,
  useReleaseLock,
  useCheckSigningEligibility,
  useGetEtrnTitle,
} from 'api/etrn-signature/etrn-signature';
import { useTranslation } from 'i18n';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { emptySign } from 'constants/constants.app';
import { EtrnTitle } from 'modules/Planner/Components/EtrnSignature/types';
import { TitleType } from '../../constants';
import { TitleChain } from './components/TitleChain';
import { ParticipantBlock } from './components/ParticipantBlock';
import { PepBlock } from './components/PepBlock/PepBlock';
import SignT3Modal from '../Sign/SignT3Modal';
import CargoAndRoute from './components/CargoAndRoute';

import styles from './styles.module.scss';
import cn from "classnames";

/**
 * Заглушка для type-safety пропа `title` в SignT3Modal.
 * Модалка не рендерится при `visible=false`, но TypeScript требует значение.
 * Реальный `titleData` приходит из `useGetEtrnTitle` в `openSignModal`.
 */
const FALLBACK_TITLE: EtrnTitle = { fileName: '', content: '', creationTime: '' };

/**
 * Состояние блокировки карточки ЭТрН.
 * - loading: блокировка запрашивается
 * - active: блокировка получена
 * - conflict: другой пользователь уже держит блокировку
 */
type LockStatus = 'loading' | 'active' | 'conflict';

interface Props {
  cardId: string;
  visible: boolean;
  onClose: () => void;
  onSigned?: () => void;
}

const EtrnModal: FC<Props> = ({
  cardId, visible, onClose, onSigned,
}) => {
  const { t: { Etrn } } = useTranslation();
  const { logger } = useAppStoreContext();
  const [activeTab, setActiveTab] = useState('general');

  const [lockStatus, setLockStatus] = useState<LockStatus>('loading');

  const { data: card } = useEtrnCard(visible && cardId ? cardId : null, { suspense: false });

  const [checkEligibility, {
    data: eligibility,
    isLoading: eligibilityLoading,
    isSuccess: eligibilitySuccess,
    isError: eligibilityError,
  }] = useCheckSigningEligibility();

  const buttonsDisabled = eligibilityLoading || !eligibilitySuccess;

  const [acquireLock] = useAcquireLock();
  const [releaseLock] = useReleaseLock();

  // ==========================================================
  // Sign T3 state
  // ==========================================================
  // Перед открытием модалки подписания получаем XML титула T3 через
  // GET /etrn-cargo/:etrnId/title/ и передаём весь объект в SignT3Modal.
  // Запрос триггерится вручную по клику через `refetch()` (внутри openSignModal).
  // `enabled: false` — без клика «Подписать УКЭП» запрос не уходит.
  const { refetch: fetchTitle } = useGetEtrnTitle(cardId, { suspense: false, enabled: false });
  const [isSignModalOpen, setIsSignModalOpen] = useState(false);
  const [titleData, setTitleData] = useState<EtrnTitle | null>(null);

  const openSignModal = useCallback(async () => {
    if (!cardId) return;

    const data = await fetchTitle();

    if (!data) {
      return;
    }

    // Не открываем модалку, если бэкенд вернул пустой контент —
    // иначе КриптоПро подпишет пустую строку.
    if (!data.content) {
      logger.toMessage('error', Etrn.card.titleContentEmpty);
      return;
    }

    setTitleData(data);
    setIsSignModalOpen(true);
  }, [cardId, fetchTitle, logger, Etrn]);

  const closeSignModal = useCallback(() => {
    setIsSignModalOpen(false);
  }, []);

  const handleSigned = useCallback(() => {
    setIsSignModalOpen(false);
    releaseLock(
      { cardId },
      {
        onSettled: () => {
          onSigned?.();
          onClose?.();
        },
      }
    );
  }, [cardId, onClose, onSigned, releaseLock]);

  useEffect(() => {
    if (!visible) return;
    checkEligibility();
  }, [visible]);


  useEffect(() => {
    if (!visible) return;

    setLockStatus('loading');
    acquireLock({ cardId }, {
      onSuccess: () => {
        setLockStatus('active');
      },
      onError: err => {
        if (err?.response?.status === 409) {
          setLockStatus('conflict');
        } else {
          setLockStatus('conflict');
        }
      },
    });
  }, [visible, cardId]);

  const handleClose = useCallback(() => {
    releaseLock({ cardId }, {
      onSettled: () => {
        onClose?.();
      },
    });
  }, [cardId, onClose, releaseLock]);

  const canSign = card?.currentTitle === TitleType.T2 && lockStatus === 'active';

  return (
    <Modal
      visible={visible}
      onCancel={handleClose}
      width="880px"
      footer={null}
    >
      <div>
      {lockStatus === 'conflict' && (
          <div style={{
            color: '#ff4d4f', padding: '8px', background: '#fff2f0', border: '1px solid #ffccc7', marginBottom: '16px',
          }}
          >
            {Etrn.card.lockConflictBanner}
          </div>
        )}
        <div>{card?.humanReadableId ?? Etrn.card.humanReadableIdPlaceholder}</div>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          destroyInactiveTabPane
        >
          <Tabs.TabPane tab={Etrn.card.tabs.general} key="general">
            <div className={styles.wrapper}>
              <TitleChain titles={card?.titleChain ?? []} />
              <Divider />
              <PepBlock
                title={{
                  name: card?.sesFullName ?? emptySign,
                  role: card?.sesRole ?? emptySign,
                  date: card?.sesEventDatetime ?? emptySign,
                  id: card?.sesEventId ?? emptySign,
                }}
              />
              <Divider />
              <div className={styles.footer}>
                <ParticipantBlock data={{
                  sender: card?.senderName ?? emptySign,
                  receiver: card?.receiverName ?? emptySign,
                  contractor: card?.carrierName ?? emptySign,
                }}
                />
                <CargoAndRoute data={{
                  cargo: card?.cargoDescription ?? emptySign,
                  route: card?.route ?? emptySign,
                }}
                />
              </div>
              <div className={styles.actionBar}>
                <button
                  className={
                    cn(
                      styles.viewDocButton,
                      { [styles.disable]: buttonsDisabled }
                    )
                  }
                  disabled={buttonsDisabled}
                >
                  {Etrn.card.viewDocument}
                </button>
                <div className={styles.actionBarRight}>
                  <button className={styles.closeButton} onClick={handleClose}>
                    {Etrn.card.close}
                  </button>
                  {canSign && (
                    <button
                      className={
                        cn(
                          styles.signButton,
                          { [styles.disable]: buttonsDisabled }
                        )
                      }
                      disabled={buttonsDisabled}
                      onClick={openSignModal}
                    >
                      {Etrn.card.signUkep}
                    </button>
                  )}
                </div>
              </div>
            </div>
          </Tabs.TabPane>
          <Tabs.TabPane tab={Etrn.card.tabs.documents} key="documents">
            <div>{Etrn.card.documentsContent}</div>
          </Tabs.TabPane>
          <Tabs.TabPane tab={Etrn.card.tabs.checks} key="checks">
            <div>{Etrn.card.checksContent}</div>
          </Tabs.TabPane>
          <Tabs.TabPane tab={Etrn.card.tabs.history} key="history">
            <div>{Etrn.card.historyContent}</div>
          </Tabs.TabPane>
        </Tabs>
      </div>
      <SignT3Modal
        visible={isSignModalOpen}
        cardId={cardId}
        title={titleData ?? FALLBACK_TITLE}
        onClose={closeSignModal}
        onSigned={handleSigned}
      />
    </Modal>
  );
};

export default observer(EtrnModal);
