import './override.scss';

import React, { useEffect } from 'react';
import { ILogger } from '@sber-sbertransport/mf-core';
import { Radio } from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import * as R from 'ramda';
import { chooseColorByPercent, filterColorsValues } from 'shared/components/LimitBar/LimitBar';
import { letterEndingMinutes } from 'shared/hooks/letterEndingFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { useGetLimitColorsPercentInfo } from 'api/limit-settings';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { usePersonalCars } from '../../CreateTripRequest/hooks/usePersonalCars';
import { getAvailablePercentage } from '../../CreateTripRequest/utils/utils';
import { ICardProps } from './Card';
import {
  CostInfo, InfoButton, LimitRequestButton, NotifyPushButton, TransportPicture
} from './components';
import {
  ClassHeader, FlexItem, LimitStatus, WaitingTime
} from './styled';

import styles from './card.module.scss';

const CardContentContext = React.createContext<ILogger | null>(null);

export function useCardContentContext(): ILogger {
  const logger = React.useContext<ILogger | null>(CardContentContext);
  if (!logger) {
    throw new Error('There was an attention of ILogger usage before initiation.');
  }
  return logger;
}

export const CardContent: React.FC<{ props: ICardProps }> = observer(({ props }) => {
  const { logger } = useAppStoreContext();

  const {
    config,
    value,
    type = LIMIT_TYPE.EMPLOYEE,
    limitSharing,
    isDisabledByPosition,
    externalPrices,
    externalTaxiClass,
    hasBonus,
    isBonus,
    economyTaxiCost,
    setShowCarToolTrip,
  } = props;
  const {
    name, taxiClass, disabled, waitingTime, transportType,
  } = config;
  const { cost, bonusCost } = value || {};

  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();

  const { bonuses } = limitsStore;

  useEffect(() => {
    limitsStore.getAccountBonuses();
  }, [limitsStore]);

  const { personalCars } = usePersonalCars();

  /* Вынужденная мера. К рефакторингу!!! */
  // getCostBonusesOdds - проверка на достаточность бонусов на счете
  const getCostBonusesOdds = () => !!(cost && bonuses?.balance && bonuses?.balance - (bonusCost || 0) >= 0);
  // getActiveBonuses - проверка на включенные или отключенные бонусы
  const getActiveBonuses = () => (isBonus && !disabled ? !getCostBonusesOdds() : disabled);
  // getNotActiveBonuses - делает неактивнымы тарифы с бонусами и накидывает стили серые
  const getNotActiveBonuses = () => isBonus && !disabled && !getCostBonusesOdds();
  const radioClass = classNames(styles.card, disabled && styles.card__disabled, styles.card__label, 'no-input-radio');

  const limitRequestButton = (
    <LimitRequestButton
      type={type}
      disabled={disabled}
      transportType={transportType}
      limitSharing={limitSharing}
      cost={cost}
      isDisabledByPosition={isDisabledByPosition}
    />
  );

  const { data: limitColorsPercentValues } = useGetLimitColorsPercentInfo();
  const currentTypeColorsPercentValues = filterColorsValues(type, limitColorsPercentValues);

  const colorsPercent = R.mergeAll(
    currentTypeColorsPercentValues.map(element => ({
      [element.name as string]: element.value,
    }))
  );

  const checkPersonalCars = () => personalCars && personalCars.length && limitRequestButton;
  const waitingTimeString = waitingTime > 0 && `≈${waitingTime} ${letterEndingMinutes(waitingTime)}`;

  const colorsObjectSpecified: any = {};
  const shortObjectsKeys = (localValue: string, key: string): void => {
    // eslint-disable-next-line no-param-reassign
    key = key.substring('EMP_LIMIT_'.length, key.length);
    // FIXME no-param-reassign
    colorsObjectSpecified[key] = localValue;
  };

  R.forEachObjIndexed(shortObjectsKeys as any, colorsPercent);

  const percent = getAvailablePercentage(limitSharing);

  const color = chooseColorByPercent(percent, colorsObjectSpecified);

  const externalPrice = externalPrices?.find(
    p => p.provider === config.taxiClass && p.taxiClass === externalTaxiClass
  )?.price;

  const getCostRegular = () => {
    const withoutBonuses = cost || (externalPrice ? externalPrice * 100 : undefined);

    return isBonus
      && hasBonus
      && cost
      && bonuses?.balance
      && bonuses?.balance - cost > 0
      && economyTaxiCost
      && cost > economyTaxiCost
      ? withoutBonuses
      : economyTaxiCost;
  };

  const getCostBonuses = () => isBonus && hasBonus && cost && getCostBonusesOdds() && economyTaxiCost && cost - economyTaxiCost;
  const toolTipShow: (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => void = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>
  ): void => setShowCarToolTrip && setShowCarToolTrip(e.currentTarget.getBoundingClientRect().top);

  const toolTipClose: () => void = (): void => setShowCarToolTrip && setShowCarToolTrip(null);

  return (
    <div
      className={`${getNotActiveBonuses() && styles.wrapperOpacity} ${styles.wrapper}`}
      onMouseEnter={getNotActiveBonuses() ? toolTipShow : undefined}
      onMouseLeave={getNotActiveBonuses() ? toolTipClose : undefined}
    >
      <Radio
        className={radioClass}
        value={`${taxiClass}-${externalTaxiClass}`}
        disabled={getActiveBonuses()}
        style={{ display: 'flex' }}
      >
        <FlexItem>
          <TransportPicture transportType={transportType} taxiClass={taxiClass} />
        </FlexItem>

        <FlexItem style={{ flex: '1.6 1' }}>
          <ClassHeader>{name}</ClassHeader>
          {limitSharing ? <LimitStatus $percent={percent} $color={color} /> : <div className={styles.limit_bar} />}
        </FlexItem>

        <FlexItem style={{ flex: '1.8 1' }}>
          <CostInfo
            type={type}
            transportType={transportType}
            cost={getCostRegular()}
            hasBonus={getCostBonuses() ? hasBonus : false}
            isBonus={false}
          />

          {taxiClass && ['COMFORT', 'BUSINESS'].includes(taxiClass) && (
            <CostInfo
              type={type}
              transportType={transportType}
              cost={cost && economyTaxiCost && cost - economyTaxiCost}
              hasBonus={true}
              isBonus={true}
            />
          )}
          <WaitingTime>{waitingTimeString}</WaitingTime>
        </FlexItem>
      </Radio>

      <InfoButton disabled={isBonus && !disabled && !getCostBonusesOdds() ? false : disabled} name={name} />
      <div className={styles.additionalButton}>
        {transportType === TransportTypeEnum.PERSONAL ? checkPersonalCars() : limitRequestButton}
        <CardContentContext.Provider value={logger}>
          <NotifyPushButton limitSharing={limitSharing} disabled={disabled} />
        </CardContentContext.Provider>
      </div>
    </div>
  );
});
