/* eslint-disable @stylistic/implicit-arrow-linebreak */

/* eslint-disable no-nested-ternary */
/* eslint-disable no-unused-expressions */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-empty-function */
import './override.scss';

import { ILogger, useHistory } from '@sber-sbertransport/mf-core';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import * as R from 'ramda';
import React, { useEffect, useState } from 'react';
import cn from 'classnames';
import { Button } from 'antd';

import { useGetLimitColorsPercentInfo } from 'api/limit-settings';

import { filterColorsValues } from 'shared/components/LimitBar/LimitBar';
import { letterEndingMinutes } from 'shared/hooks/letterEndingFactory';
import TTab from 'shared/ui/Tab/Tab';

import { StoreNames, useAppStore } from 'stores';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ExternalProviderEnum, SubClass, TaxiEnum } from 'stores/Trip/Trip.interface';
import { isPrivilegedTaxiClass } from 'utils/trips';
import { EmployeeAppLinks } from 'modules/EmployeeApp/EmployeeApp.constants';

import Process from '../../Evaluation/Constants/Process';
import { ICardProps } from '../TaxiClassCardWrapper';
import { CostInfo, TransportPicture } from './components';
import { ClassHeader, WaitingTime } from './styled';
import { LimitIndicator } from '../LimitIndicator/LimitIndicator';

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
  const {
    config,
    form,
    value,
    type = LIMIT_TYPE.DEPARTMENT,
    externalPrices,
    economyTaxiCost,
    setShowCarToolTrip,
    product,
    setProduct = () => {},
    setSubClass = () => {},
    setExternalProvider = () => {},
    setProcess,
    loadExternalPrices,
    handleNextStep,
    limitPercentage,
    limitSharing,
    limitAuthor,
    isDisabledByPosition,

    // hasBonus,
    // isBonus,
    // bonuses,
    // isAvailableWithoutBonus,
  } = props;

  /* Бонусный счет, который пока что отключен */
  const [externalPricesClass, setExternalPricesClass] = useState('');
  const hasBonus = false;
  const isBonus = false;
  const bonuses = [] as any;
  const isAvailableWithoutBonus = true;

  const { [StoreNames.selfStore]: selfStore, [StoreNames.tripStore]: tripStore } = useAppStore();

  const {
    transportType: taxiClassByProduct, step, subClass, isExternal, externalProvider,
  } = product || {};

  const {
    name, taxiClass, disabled, waitingTime, transportType,
  } = config;

  const {
    cost, bonusCost, priceDetails,
  } = value || {};
  const { personalCars } = tripStore;

  const history = useHistory();

  const isSecondStep = step === 2;
  const isThirdStep = step === 3;

  const radioClass = classNames(styles.card, disabled && styles.card__disabled, styles.card__label, 'no-input-radio');

  // getCostBonusesOdds - проверка на достаточность бонусов на счете
  const getCostBonusesOdds = () =>
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    isAvailableWithoutBonus || !!(cost && bonuses?.balance && bonuses?.balance - (bonusCost || 0) >= 0);
  // getActiveBonuses - проверка на включенные или отключенные бонусы
  // eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-use-before-define
  const getActiveBonuses = () => (isBonus && !disabled ? !getCostBonusesOdds() : disabled);
  // getNotActiveBonuses - делает неактивнымы тарифы с бонусами и накидывает стили серые
  // eslint-disable-next-line @typescript-eslint/no-use-before-define
  const getNotActiveBonuses = () => isBonus && !disabled && !getCostBonusesOdds();

  const getCostBonuses = () => isBonus && hasBonus && cost && getCostBonusesOdds() && economyTaxiCost && cost - economyTaxiCost;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const toolTipShow: (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => void = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>
  ): void => setShowCarToolTrip && setShowCarToolTrip(e.currentTarget.getBoundingClientRect().top);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const toolTipClose: () => void = (): void => setShowCarToolTrip && setShowCarToolTrip(null);

  // const limitRequestButton = (
  //   <LimitRequestButton
  //     type={type}
  //     disabled={disabled}
  //     transportType={transportType}
  //     limitSharing={limitSharing}
  //     cost={cost}
  //     isDisabledByPosition={isDisabledByPosition}
  //     limitAvailable={value?.limitAvailable}
  //     isExternal={isExternal}
  //   />
  // );

  const { data: limitColorsPercentValues } = useGetLimitColorsPercentInfo();
  const currentTypeColorsPercentValues = filterColorsValues(type, limitColorsPercentValues);

  const colorsPercent = R.mergeAll(
    currentTypeColorsPercentValues.map(element => ({
      [element.name as string]: element.value,
    }))
  );

  const hasLimitsPercentage = typeof limitPercentage === 'number' && !!limitPercentage === !!value?.limitAvailable;
  const isDisabledByLimit = !isExternal && !value?.limitAvailable;

  const shouldRenderLimitButton = ((disabled && !isDisabledByPosition && limitSharing && cost) || isDisabledByLimit) && !isExternal;

  const handleMoveToLimitPage = () => {
    const activeLimitQuery = (selfStore.empId === limitAuthor || selfStore.selfEmployee.isDepartmentHead) ? 'department' : 'personal';
    history.push(`/client/${EmployeeAppLinks.limits}/limitsInfo?activeKey=${activeLimitQuery}`);
  };

  const requestLimitButton = (
    <div className={styles.requestLimit}>
      <Button
        className={styles.requestLimitButton}
        onClick={handleMoveToLimitPage}
      >
        Запросить лимит
      </Button>
    </div>
  );

  const buttonWithCondition = shouldRenderLimitButton && requestLimitButton;

  const checkPersonalCars = () => personalCars && personalCars.length && buttonWithCondition;
  const waitingTimeString = waitingTime > 0 && `≈${waitingTime} ${letterEndingMinutes(waitingTime)}`;

  const colorsObjectSpecified: any = {};
  const shortObjectsKeys = (localValue: string, key: string): void => {
    // eslint-disable-next-line no-param-reassign
    key = key.substring('EMP_LIMIT_'.length, key.length);
    // FIXME no-param-reassign
    colorsObjectSpecified[key] = localValue;
  };

  R.forEachObjIndexed(shortObjectsKeys as any, colorsPercent);

  // const limitPercent = getAvailablePercentage(limitSharing);
  // const color = chooseColorByPercent(limitPercent, colorsObjectSpecified);
  // const limitValue = type === LIMIT_TYPE.EMPLOYEE ? getAvailableBalance(limitSharing) : limitPercent;
  // const limitUnit = type === LIMIT_TYPE.EMPLOYEE ? '₽' : '%';

  useEffect(() => {
    setExternalPricesClass(form?.getFieldsValue().externalPrices);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [form?.getFieldsValue().externalPrices]);

  const externalTariff = externalPrices?.find(
    p => p.provider === config.taxiClass && p.taxiClass === externalPricesClass
  );
  const externalPrice = externalTariff?.price;

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

  const carSharingCompName = priceDetails?.carSharingCompName;

  let transportTitle;
  let isSecondActive = false;
  let isThirdActive = false;

  if (isSecondStep && !isExternal) {
    transportTitle = transportType === TransportTypeEnum.TAXI || transportType === TransportTypeEnum.GROUP_TRANSFER || transportType === TransportTypeEnum.YANDEX ? TransportTypeTitlesEnum[transportType] : name;
    isSecondActive = taxiClassByProduct === value?.transportType.name;
  } else {
    transportTitle = transportType === TransportTypeEnum.CARSHARING && carSharingCompName ? carSharingCompName : name;
  }

  if (isSecondStep && isExternal) {
    isSecondActive = config.taxiClass === externalProvider;
  }

  if (isThirdStep && subClass) {
    isThirdActive
      = subClass === (value?.taxiClass as unknown as SubClass)
      || subClass === (value?.transportType.name as unknown as SubClass);
  }

  const isActive = (isSecondActive || isThirdActive) && !isDisabledByLimit;

  const isTypeWithoutSubClass = [TransportTypeEnum.CARSHARING, TransportTypeEnum.PERSONAL, TransportTypeEnum.GROUP_TRANSFER].includes(transportType);

  const handleCard = () => {
    if (isExternal) {
      setExternalProvider(config.taxiClass as ExternalProviderEnum);
      setSubClass();
    } else {
      setProduct(transportType);

      if (isTypeWithoutSubClass) {
        setSubClass(taxiClass as unknown as TaxiEnum);
        transportType === TransportTypeEnum.GROUP_TRANSFER && handleNextStep && handleNextStep(taxiClass);
      } else {
        setSubClass(value?.taxiClass ? (value.taxiClass as SubClass) : undefined);
      }
    }
  };

  const onClickNextStep = () => {
    if (isDisabledByLimit) return;

    if (step === 2) {
      isExternal
        ? loadExternalPrices
        : transportType !== 'CARSHARING'
          ? handleNextStep && handleNextStep()
          : setProcess && step === 2 && transportType === TransportTypeEnum.CARSHARING && setProcess(Process.START);
    }
  };

  return (
    <div
      className={`${getNotActiveBonuses() && styles.wrapperOpacity} ${styles.wrapper}`}
      // onMouseEnter={getNotActiveBonuses() ? toolTipShow : undefined}
      // onMouseLeave={getNotActiveBonuses() ? toolTipClose : undefined}
      onClick={onClickNextStep}
    >
      <TTab
        key={value?.id}
        className={radioClass}
        column={false}
        style={{
          padding: '2px, 16px, 2px, 8px',
          height: 72,
          justifyContent: 'space-around',
          margin: 1,
        }}
        isActive={isActive}
        onClick={!isDisabledByLimit ? handleCard : undefined}
      >
        <div className={cn(styles.transportPicture, { [styles.disabledTransportImage]: isDisabledByLimit })}>
          <TransportPicture transportType={transportType} taxiClass={taxiClass} />
        </div>

        <div className={styles.transportTitle}>
          <div>
            <ClassHeader>{transportTitle}</ClassHeader>
            {hasLimitsPercentage && (
              <LimitIndicator percentage={limitPercentage!} />
            )}
            {/* Временно оставить для сверки с предыдущей логикой отображения, в случае ошибок */}
            {/* {limitSharing ? (
              <LimitStatus
                $value={limitValue}
                $unit={limitUnit}
                $color={color}
              />
            ) : (
              <div className={styles.limit_bar} />
            )} */}
          </div>
        </div>

        <div className={cn(styles.costInfo, { [styles.disabledCostInfo]: isDisabledByLimit })}>
          <CostInfo
            type={type}
            tariffId={value?.id}
            transportType={transportType}
            cost={getCostRegular()}
            hasBonus={getCostBonuses() ? hasBonus && !isAvailableWithoutBonus : false}
            isBonus={false}
            step={step}
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
        </div>
      </TTab>

      <div className={styles.additionalButton}>
        {transportType === TransportTypeEnum.PERSONAL || isExternal ? checkPersonalCars() : buttonWithCondition}
      </div>
    </div>
  );
});
