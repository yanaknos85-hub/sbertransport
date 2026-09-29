
import { InfoCircleOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import classNames from 'classnames';
import React from 'react';
import { RUBLE_SIGN } from 'constants/constants.app';
import { BonusesSvg } from 'modules/EmployeeApp/components/Bonuses/assets/images/svg';
import { SpinWrapped } from 'shared/components';
import { LimitBar } from 'shared/components/LimitBar/LimitBar';
import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITaxi, TTaxiClass } from 'stores/Trip/Trip.interface';

import { getAvailablePercentage } from '../../CreateTripRequest/utils/utils';
import { LimitRequestModal } from '../../LimitsPage/LimitRequestModal/LimitRequestModal';
import styles from './card.module.scss';
import { useCardContentContext } from './CardContent';
import { useCard } from './useCard';

export const InfoButton: React.FC<{
  disabled?: boolean;
  name: string;
}> = ({ disabled, name }) => {
  const cardInfo = useCard({ name });
  return (
    <div className={styles.info}>
      {disabled && (
        <Button
          className={styles.info__ant_btn_circle}
          shape="circle"
          onClick={(): void => cardInfo.onInfoClick()}
          icon={<InfoCircleOutlined />}
          style={{
            border: 'none', boxShadow: 'none', color: '#A8ABB3',
          }}
        />
      )}
    </div>
  );
};

export const NotifyPushButton: React.FC<{
  limitSharing?: LimitSharing;
  disabled?: boolean;
}> = ({ limitSharing, disabled }) => {
  const logger = useCardContentContext();

  return (
    // eslint-disable-next-line react/jsx-no-useless-fragment
    <>
      {/* FIXME react/jsx-no-useless-fragment */}
      {disabled && !limitSharing && (
        <Button
          size="small"
          className={styles.notifyButton}
          onClick={(): void => {
            logger.toMessage('warning', 'Функционал push-уведомлений предстоит разработать');
          }}
        >
          Уведомить
        </Button>
      )}
    </>
  );
};

export const CostInfo: React.FC<{
  type: LIMIT_TYPE;
  cost?: number;
  name?: ITaxi['name'];
  transportType: TransportTypeEnum;
  isBonus?: boolean;
  hasBonus?: boolean;
  tariffId?: string;
  step?: number;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
}> = ({
  cost, transportType, name, isBonus, hasBonus, step,
}) => {
  const price = cost || 0;
  const priceToRub = price > 0 ? Math.round(price / 100) : 0;
  const approximateCost = step === 2 ? `от ${priceToRub}` : priceToRub;

  const svgStyle = {
    width: 25, height: 20, transform: 'translate(-1, 1)',
  };

  const getCost = (): JSX.Element | null => {
    if ([TransportTypeEnum.PUBLIC, TransportTypeEnum.PERSONAL].includes(transportType)) {
      return null;
    }

    return cost && priceToRub ? (
      <span style={{
        fontWeight: 600, display: 'flex', fontFamily: 'SB Sans Text Bold',
      }}
      >
        {!isBonus ? (
          <span style={{ margin: '0 12px', color: hasBonus ? '#909090' : '#262626' }}>
            {`${approximateCost} ${RUBLE_SIGN}`}
          </span>
        ) : (
          <span style={{ display: 'flex', color: '#262626' }}>
            <div className={styles.bonusCost}>доплата</div>
            {priceToRub}
            <span>{BonusesSvg(svgStyle).Bonus()}</span>
          </span>
        )}
      </span>
    ) : null;
  };

  return (
    <>
      <div className={styles.name}>
        {name}
        <div className={styles.name__content}>
          {/* <div className={type === LIMIT_TYPE.DEPARTMENT ? styles.name__corporate : styles.name__personal} /> */}
          {getCost()}
        </div>
      </div>

      {transportType === TransportTypeEnum.PERSONAL && (
        <div className={styles.publicTransportCostInfoContainer}>
          <span className={styles.personalClass}>
            от 100
            {RUBLE_SIGN}
          </span>
        </div>
      )}

      {transportType === TransportTypeEnum.PUBLIC && (
        <div className={styles.publicTransportCostInfoContainer}>
          <span className={styles.publicClass}>
            от 30
            {RUBLE_SIGN}
          </span>
        </div>
      )}
    </>
  );
};

export const TransportPicture: React.FC<{
  transportType: string;
  taxiClass?: TTaxiClass;
}> = ({ transportType, taxiClass }) => {
  const transportTypeCasted = `${transportType}` as keyof typeof styles;
  return (
    <div
      className={classNames(
        (taxiClass && styles[taxiClass]) || (transportTypeCasted && styles[transportTypeCasted]),
        styles.taxi,
        styles.taxiClass
      )}
    />
  );
};

export const LimitSharingBar: React.FC<{ limitSharing: LimitSharing; type: LIMIT_TYPE }> = ({ limitSharing, type }) => {
  const availablePercentage = limitSharing ? getAvailablePercentage(limitSharing) : 0;
  return (
    <React.Suspense fallback={<SpinWrapped />}>
      <LimitBar
        limitType={type}
        percent={availablePercentage}
        showInfo={false}
        className={styles.limit_bar}
      />
    </React.Suspense>
  );
};

export const LimitRequestButton: React.FC<{
  type: LIMIT_TYPE;
  disabled?: boolean;
  transportType: TransportTypeEnum;
  limitSharing?: LimitSharing;
  cost?: number;
  isDisabledByPosition?: boolean;
  limitAvailable?: boolean;
  isExternal?: boolean;
}> = ({
  type, disabled, transportType, limitSharing, cost, isDisabledByPosition, limitAvailable, isExternal,
}) => (
  <>
    {((disabled && !isDisabledByPosition && limitSharing && cost) || !limitAvailable) && !isExternal && (
      <LimitRequestModal
        type={type}
        transportType={transportType}
        percent={100}
      />
    )}
  </>
);
