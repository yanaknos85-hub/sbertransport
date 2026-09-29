import { ILogger } from '@sber-sbertransport/mf-core';
import { StoreNames } from 'stores';
import { Radio } from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import React from 'react';

import { letterEndingMinutes } from 'shared/hooks/letterEndingFactory';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { isPrivilegedTaxiClass } from 'utils/trips';

import Process from '../../Evaluation/Constants/Process';
import { ICardProps } from './Card';
import styles from './card.module.scss';
import { CostInfo, InfoButton, TransportPicture } from './components';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { ClassHeader, FlexItem, WaitingTime } from './styled';
import './override.scss';

export const tariffSeparator = '__';
const CardContentContext = React.createContext<ILogger | null>(null);

export function useCardContentContext(): ILogger {
  const logger = React.useContext<ILogger | null>(CardContentContext);
  if (!logger) {
    throw new Error('There was an attention of ILogger usage before initiation.');
  }
  return logger;
}

export const CardContent: React.FC<{ props: ICardProps }> = observer(({ props }) => {
  // const { logger } = useAppStoreContext();

  const {
    config,
    value,
    type = LIMIT_TYPE.DEPARTMENT,
    // isDisabledByPosition,
    externalPrices,
    externalTaxiClass,
    hasBonus,
    isBonus,
    economyTaxiCost,
    setShowCarToolTrip,
    isAvailableWithoutBonus,
    bonuses,
    setProcess,
  } = props;
  const {
    name, taxiClass, disabled, waitingTime, transportType,
  } = config;

  const { [StoreNames.tripStore]: tripStore } = useAppStoreContext();

  const { cost, bonusCost } = value || {};

  const { personalCars } = tripStore;

  /* Вынужденная мера. К рефакторингу!!! */
  // // getCostBonusesOdds - проверка на достаточность бонусов на счете
  const getCostBonusesOdds = () => isAvailableWithoutBonus || !!(cost && bonuses?.balance && bonuses?.balance - (bonusCost || 0) >= 0);
  // // getActiveBonuses - проверка на включенные или отключенные бонусы
  const getActiveBonuses = () => (isBonus && !disabled ? !getCostBonusesOdds() : disabled);
  // // getNotActiveBonuses - делает неактивнымы тарифы с бонусами и накидывает стили серые
  const getNotActiveBonuses = () => isBonus && !disabled && !getCostBonusesOdds();

  const radioClass = classNames(styles.card, disabled && styles.card__disabled, styles.card__label, 'no-input-radio');

  // Закоментировал в рамках TRANSPORT-10116
  // const limitRequestButton = (
  //   <LimitRequestButton
  //     type={type}
  //     disabled={disabled}
  //     transportType={transportType}
  //     limitSharing={limitSharing}
  //     cost={cost}
  //     isDisabledByPosition={isDisabledByPosition}
  //     limitAvailable={value?.limitAvailable}
  //   />
  // );

  // const { data: limitColorsPercentValues } = useGetLimitColorsPercentInfo();
  // const currentTypeColorsPercentValues = filterColorsValues(type, limitColorsPercentValues);

  // const colorsPercent = R.mergeAll(
  //   currentTypeColorsPercentValues.map(element => ({
  //     [element.name as string]: element.value,
  //   })),
  // );

  // const colorsObjectSpecified: any = {};
  // const shortObjectsKeys = (localValue: string, key: string): void => {
  //   // eslint-disable-next-line no-param-reassign
  //   key = key.substring('EMP_LIMIT_'.length, key.length);
  //   // FIXME no-param-reassign
  //   colorsObjectSpecified[key] = localValue;
  // };

  // R.forEachObjIndexed(shortObjectsKeys as any, colorsPercent);

  // const limitPercent = getAvailablePercentage(limitSharing);
  // const color = chooseColorByPercent(limitPercent, colorsObjectSpecified);
  // const limitValue = type === LIMIT_TYPE.EMPLOYEE ? getAvailableBalance(limitSharing) : limitPercent;
  // const limitUnit = type === LIMIT_TYPE.EMPLOYEE ? '₽' : '%';

  const checkPersonalCars = () => personalCars && personalCars.length && null;
  const waitingTimeString = waitingTime > 0 && `≈${waitingTime} ${letterEndingMinutes(waitingTime)}`;

  const externalPrice = externalPrices?.find(
    p => p.provider === config.taxiClass && p.taxiClass === externalTaxiClass
  )?.price;

  const getCostRegular = () => {
    const withoutBonuses = cost || (externalPrice ? externalPrice * 100 : undefined);

    return isAvailableWithoutBonus
      || (isBonus
      && hasBonus
      && cost
      && bonuses?.balance
      && bonuses?.balance - cost > 0
      && economyTaxiCost
      && cost > economyTaxiCost)
      ? withoutBonuses
      : economyTaxiCost;
  };

  const getCostBonuses = () => isBonus && hasBonus && cost && getCostBonusesOdds() && economyTaxiCost && cost - economyTaxiCost;
  const toolTipShow: (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => void = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>
  ): void => setShowCarToolTrip && setShowCarToolTrip(e.currentTarget.getBoundingClientRect().top);

  const toolTipClose: () => void = (): void => setShowCarToolTrip && setShowCarToolTrip(null);

  // const carSharingCompName = priceDetails?.carSharingCompName;
  // const transportTitle = transportType === TransportTypeEnum.CARSHARING && carSharingCompName ? carSharingCompName : name;
  const transportTitle = name;

  return (
    <div
      className={`${getNotActiveBonuses() && styles.wrapperOpacity} ${styles.wrapper}`}
      onMouseEnter={getNotActiveBonuses() ? toolTipShow : undefined}
      onMouseLeave={getNotActiveBonuses() ? toolTipClose : undefined}
      onClick={() => setProcess && transportType === TransportTypeEnum.CARSHARING && setProcess(Process.START)}
    >
      <Radio
        className={radioClass}
        value={`${taxiClass}-${externalTaxiClass}${tariffSeparator}${value?.id}`}
        disabled={getActiveBonuses()} // С какого то момента бонусы стали решать можно ли вырбрать тариф или нет. Никто не знает как должно быть
        style={{ display: 'flex' }}
      >
        <FlexItem>
          <TransportPicture transportType={transportType} taxiClass={taxiClass} />
        </FlexItem>

        <FlexItem style={{ flex: '1.6 1' }}>
          <ClassHeader>{transportTitle}</ClassHeader>
          {/* {limitSharing ? (
            <LimitStatus $value={limitValue} $unit={limitUnit} $color={color} />
          ) : ( */}
          <div className={styles.limit_bar} />
          {/* )} */}
        </FlexItem>

        <FlexItem style={{ flex: '1.8 1' }}>
          <CostInfo
            type={type}
            tariffId={value?.id}
            transportType={transportType}
            cost={getCostRegular()}
            hasBonus={getCostBonuses() ? hasBonus && !isAvailableWithoutBonus : false}
            isBonus={false}
          />

          {!isAvailableWithoutBonus && taxiClass && isPrivilegedTaxiClass(taxiClass) && (
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
        {transportType === TransportTypeEnum.PERSONAL ? checkPersonalCars() : null}
        {/* tentContext.Provider value={logger}> */}
        {/*   <NotifyPushButton limitSharing={limitSharing} disabled={disabled} /> */}
        {/* </CardContentContext.Provider> */}
      </div>
    </div>
  );
});
