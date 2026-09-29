import React, { FC } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import cn from 'classnames';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useUiContext } from 'shared/components/UI';
import Rate from 'shared/form/Rate/Rate';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import TButton from 'shared/ui/Button/Button';

import { EXCHANGE } from 'constants/constants.routes';

import { ListItemExchange } from '../../../types';
import { RubleSymbolComponent } from './RubleSymbolComponent';

import styles from './OrderControls.module.scss';

interface Props {
  request: ListItemExchange;
  showModal: () => void;
}

export const OrderControls: FC<Props> = props => {
  const {
    request, showModal,
  } = props;

  const history = useHistory();
  const { type } = useParams<{ type: string; id: string }>();

  const {
    [StoreNames.exchangeStore]: exchangeStore,
  } = useAppStoreContext();

  const { denyExchange, takeToWorkExchange } = exchangeStore;

  const { isMobile } = useUiContext();

  const handleTakeToWork = async (id: string) => {
    await takeToWorkExchange(id, type);
  };

  const handleDecline = async (id: string) => {
    await denyExchange(id, type);
  };

  const goToDetailPage = id => {
    history.push(`${EXCHANGE}/${type}/${id}`);
  };

  let buttonText = '';
  let buttonAction;

  switch (type) {
    case 'available': // доступная заявка
      buttonText = 'Взять в работу';
      buttonAction = () => handleTakeToWork(request.id);
      break;
    case 'non_terminal': // заявки в работе
      buttonText = 'Отказаться';
      buttonAction = () => handleDecline(request.id);
      break;
    default:
      buttonText = '';
  }

  return (
    <div className={cn(styles.right, {
      [styles['rightMobile']]: isMobile,
    })}
    >
      <div className={styles.priceBlock}>
        <div className={styles.title}>Оплата за доставку</div>
        <div className={cn(styles.price, {
          [styles['priceMobile']]: isMobile,
        })}
        >
          <RubleSymbolComponent value={request.cost} />
        </div>
      </div>
      <div className={cn(styles.buttons, {
        [styles['buttonsMobile']]: isMobile,
        [styles['buttonsFinished']]: type === 'terminal' && isMobile,
      })}
      >
        {request.evaluation ? (
          <div onClick={showModal} style={{ cursor: 'pointer' }}>
            <Rate
              disabled
              value={request.evaluation.rating}
              className={styles.rate}
            />
          </div>
        ) : (
          <TButton
            $size="small"
            onClick={buttonAction}
          >
            {buttonText}
          </TButton>
        )}
        <TButton
          $size="small"
          className={styles.more}
          onClick={() => goToDetailPage(request.id)}
        >
          Подробнее
        </TButton>
      </div>
    </div>
  );
};
