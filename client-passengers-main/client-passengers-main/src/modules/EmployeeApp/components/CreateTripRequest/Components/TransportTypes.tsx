/* eslint-disable @typescript-eslint/no-explicit-any */
import { Form, Radio, RadioChangeEvent } from 'antd';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import React, {
  FC, useCallback, useEffect, useMemo, useState
} from 'react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';

import { TransportTypeEnum, TransportTypeTitlesEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  ITaxi, ITripTariff, TExternalPrices, TaxiClassEnum
} from 'stores/Trip/Trip.interface';
import useTripLimit from '../../TaxiClasses2/hooks';
import TaxiClassCardWrapper from '../../TaxiClasses2/TaxiClassCardWrapper';
import styles from '../../TaxiClasses2/taxiClasses.module.scss';
import {
  ExternalClassesConfig,
  TransportTypesConfig,
  busClasses,
  busConfig,
  busIdStub
} from '../../TaxiClasses2/TaxiClassesConfig';
import { SpinWrapped } from 'shared/components';

import { TransportTypesProps } from '../types/types';
import { useUserLimits } from 'shared/hooks/limit';
import { toRubles } from 'utils';
import { getValidTripDateParams } from '../utils/utils';
import { useYandexTaxiLimitAllowance, useYandexTaxiTariffs } from 'api/yandexTaxi/yandex-taxi.api';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

type TExternalPricesWithMandatoryPrice = TExternalPrices & {
  price: number;
};

export const TransportTypes: FC<TransportTypesProps> = observer(
  ({
    tariffsInfo,
    request,
    form,
    purpose,
    setCurrentTransportType,
    isExternal,
    externalPrices,
    availableTaxiClasses,
    product,
    setProduct,
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    setStep = () => { },
    setSubClass,
    setExternalProvider,
    setProcess,
    loadExternalPrices,
    handleNextStep,
    tariffExternal,
  }) => {
    const {
      // [StoreNames.limitsStore]: limitsStore,
      [StoreNames.selfStore]: selfStore,
      [StoreNames.transportTypesStore]: transportTypesStore,
      [StoreNames.tripStore]: tripStore,
      [StoreNames.geoStore]: geo,
      [StoreNames.configStore]: configStore,
      logger,
    } = useAppStoreContext();

    // useEffect(() => {
    //   limitsStore.getAccountBonuses();
    // }, [limitsStore]);

    const { isMobile } = usePlatformDetect();

    const isOrderYandexAvailable = configStore.env.IS_SDO && isMobile;

    const passengerValue = Form.useWatch('passenger', form);

    const isTripDateValidForYandex = useMemo(() => {
      const when = form?.getFieldValue('when');
      const date = form?.getFieldValue('date');

      return getValidTripDateParams(when, date);
    }, [form?.getFieldValue('when'), form?.getFieldValue('date')]);

    const isTripSuitsYandexConditions = useMemo(() => geo.waypoints.length <= 2 && passengerValue === 'me', [geo.waypoints, passengerValue]);

    const formattedCoordinates = useMemo(() => {
      if (!isOrderYandexAvailable) return '';

      const firstWaypoint = geo.waypoints.at(0);
      const secondWaypoint = geo.waypoints.at(-1);

      if (!firstWaypoint?.isValid || !secondWaypoint?.isValid || !firstWaypoint || !secondWaypoint) {
        return '';
      }

      return `${firstWaypoint.latitude},${firstWaypoint.longitude};${secondWaypoint.latitude},${secondWaypoint.longitude}`;
    }, [geo.waypoints]);

    // !!! Добавил suspense: false, чтобы запрос выполнялся в фоне и не мешал отображению других видов транспорта во время загрузки.
    // После раскомментирования проверить, как оно будет работать
    const { data } = useYandexTaxiLimitAllowance({ suspense: false, enabled: isOrderYandexAvailable });
    const order = data?.order ?? false;

    const { data: externalYandexTariffs } = useYandexTaxiTariffs(formattedCoordinates, { enabled: !!formattedCoordinates && order && isOrderYandexAvailable });

    useEffect(() => {
      tripStore.setAvailableChoosingBookingTransport(null);
      tripStore.setAvailableDispatcherTransport(null);
      tripStore.setRangeDispatcherTransportDate(null);
    }, []);

    // const { bonuses } = limitsStore;

    const formattedYandexTariffs = useMemo(() => {
      if (!externalYandexTariffs || !externalYandexTariffs?.length || !tariffsInfo?.length || !order || !isOrderYandexAvailable) return [];

      const taxiTariff = tariffsInfo?.find(item => item.transportType.name === TransportTypeEnum.TAXI && (item?.priceDetails?.taxiClass && !item.priceDetails.taxiClass.includes('BUS')));

      if (!taxiTariff) return [];

      return externalYandexTariffs.reduce((acc, item) => {
        if (item.type === TaxiClassEnum.COMFORT || item.type === TaxiClassEnum.ECONOMY) {
          const newTariff: ITripTariff = {
            ...structuredClone(taxiTariff),
            cost: item.price * 100,
            transportType: { id: TransportTypeEnum.YANDEX, name: TransportTypeEnum.YANDEX },
            taxiClass: `YANDEX_${item.type}` as keyof typeof TaxiClassEnum,
            id: `YANDEX${item.type ?? ''}`,
          };

          if (newTariff?.priceDetails) {
            newTariff.priceDetails.taxiClass = `YANDEX_${item.type}`;
          }

          return acc.concat(newTariff);
        }

        return acc;
      }, [] as ITripTariff[]);
    }, [tariffsInfo, externalYandexTariffs, order]);

    // Делает вспышку фронта при инициализации компонента
    const tripLimit = useTripLimit({ request });

    const isAuthor = selfStore.selfEmployee.id === request?.author.id;
    const requestExist = !!request;
    const isEditable = !requestExist || (requestExist && isAuthor);

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [showCarToolTrip, setShowCarToolTrip] = useState<number | null>(null);

    const { step, transportType } = product || {};
    const isSecondStep = step === 2;
    const isThirdStep = step === 3;

    const { currentEmployeeSharing, currentDepartmentSharing } = useUserLimits(selfStore.selfEmployee);
    const formattedDepsSharing = currentDepartmentSharing.flat(2).reduce((acc, entry) => {
      const sharing = entry.limitSharingPerPeriodDTO ?? entry;

      return ({
        ...acc,
        [entry.transportType]: {
          balance: sharing.balance,
          sum: sharing.sum,
          author: sharing.author,
        },
      });
    }, {});

    const formattedEmpSharing = currentEmployeeSharing.flat(2).reduce((acc, entry) => {
      const sharing = entry.limitSharingPerPeriodDTO ?? entry;

      return ({
        ...acc,
        [entry.transportType]: {
          balance: sharing.balance,
          sum: sharing.sum,
          author: sharing.author,
        },
      });
    }, {});

    const getLimitPercentage = (value: number, rest: number) => {
      return Math.round(value * 100 / rest);
    };

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const getLimitBalance = useCallback((cost: number, transportType: TransportTypeEnum) => {
      const transportTypeName = transportType === TransportTypeEnum.YANDEX ? TransportTypeEnum.TAXI : transportType;

      const depSharing = formattedDepsSharing?.[transportTypeName];
      const empSharing = formattedEmpSharing?.[transportTypeName];

      const depBalance = depSharing?.balance ?? 0;
      const empBalance = empSharing?.balance ?? 0;

      const potentialApproxCost = cost * 110 / 100;
      const author = formattedDepsSharing?.[transportTypeName]?.author;

      const defaultValue = {
        limitPercentage: 0, author,
      };

      if (!depBalance && !empBalance) {
        return defaultValue;
      }

      if (depBalance - potentialApproxCost > 0) {
        return { limitPercentage: getLimitPercentage(toRubles(depBalance), toRubles(depSharing.sum)) ?? 0, author };
      }

      if (empBalance - potentialApproxCost > 0) {
        return { limitPercentage: getLimitPercentage(toRubles(empBalance), toRubles(empSharing.sum)) ?? 0, author };
      }

      return defaultValue;
    }, [formattedDepsSharing, formattedEmpSharing]);

    const shouldInsertRestLimit = (transportType: TransportTypeEnum) => {
      return (isSecondStep || isThirdStep)
        && transportType !== TransportTypeEnum.BUS
        && transportType !== TransportTypeEnum.GROUP_TRANSFER;
    };

    const findTariffs = useCallback(
      (option: ITaxi): (undefined | ITripTariff)[] => {
        if (isExternal) {
          return [undefined]; // show tariff in list for external provider
        }

        if (!tariffsInfo.length) {
          return [undefined]; // no tariffs have been received, show tariff in list with no data
        }

        // eslint-disable-next-line @typescript-eslint/no-shadow
        const { taxiClass, transportType } = option;
        if (taxiClass === TaxiClassEnum.BUS) {
          const busTariffs = tariffsInfo.filter(item => busClasses.includes(item.taxiClass as TaxiClassEnum));
          return busTariffs.length
            ? [
              busTariffs.reduce((prev, curr) => {
                return {
                  ...prev,
                  id: busIdStub,
                  cost: Math.min(prev.cost, curr.cost),
                  limitPercentage: Math.min(prev.limitPercentage as number, curr.limitPercentage as number),
                };
              }),
            ]
            : [];
        }

        return tariffsInfo.concat(formattedYandexTariffs).filter(item => {
          if (shouldInsertRestLimit(item.transportType.name)) {
            const { limitPercentage, author } = getLimitBalance(item.cost, item.transportType.name);
            item.limitPercentage = limitPercentage;
            item.limitAuthor = author;
          }

          if (item.transportType.name === TransportTypeEnum.YANDEX) {
            return item.taxiClass === taxiClass && item.transportType.name === TransportTypeEnum.YANDEX;
          }

          if (item.transportType.name !== TransportTypeEnum.TAXI) {
            if (item.transportType.name === TransportTypeEnum.GROUP_TRANSFER) {
              return isThirdStep
                ? item.transportType.name === transportType && item.groupTransferClass === taxiClass && transportType === TransportTypeEnum.GROUP_TRANSFER
                : item.groupTransferClass === taxiClass;
            }
            return item.transportType.name === transportType;
          }

          return item.taxiClass === taxiClass && transportType === TransportTypeEnum.TAXI;
        });
      },
      [tariffsInfo, isExternal, step, getLimitBalance]
    );

    const formattedAvailableTransportTypes = useMemo(() => {
      const _transportTypes = [...transportTypesStore.availableTransportTypes];
      const hasYandex = _transportTypes.find(item => item.name === TransportTypeEnum.YANDEX);

      if (!hasYandex) {
        _transportTypes.push({
          id: TransportTypeEnum.YANDEX, rusName: TransportTypeTitlesEnum.YANDEX, name: TransportTypeEnum.YANDEX,
        });
      }

      return _transportTypes;
    }, [transportTypesStore.availableTransportTypes]);

    const itemsWhichMayBeShown = useMemo(() => {
      const items: ITaxi[] = [];

      let hasBus = false;

      const availableTransportTypes = [...formattedAvailableTransportTypes];

      Object.values(TransportTypesConfig).forEach(item => {
        const isBus = busClasses.includes(item.taxiClass as TaxiClassEnum);
        const isAvailable = availableTransportTypes
          .filter(i => (isSecondStep || (isThirdStep && i.name === transportType)))
          // eslint-disable-next-line @typescript-eslint/no-shadow
          .some(transportType => transportType.name === item.transportType);
        const hasTransportType = isSecondStep ? items.find(i => i.transportType === item.transportType) : false;

        const isYandexAvailableToPaste = (transportType === TransportTypeEnum.YANDEX || item.transportType === TransportTypeEnum.YANDEX) && isOrderYandexAvailable
          ? order && isTripDateValidForYandex && isTripSuitsYandexConditions
          : true;

        if (isBus) {
          hasBus = true;
        } else if (isAvailable && !hasTransportType && findTariffs(item).some(Boolean) && isYandexAvailableToPaste) {
          items.push(item);
        }
      });

      if ((isSecondStep || (isThirdStep && transportType === TransportTypeEnum.BUS)) && hasBus) {
        items.push(busConfig);
      }

      return items;
    }, [formattedAvailableTransportTypes, isSecondStep, isThirdStep, transportType, findTariffs, isTripDateValidForYandex, isTripSuitsYandexConditions]);

    const externalClasses = Object.values(ExternalClassesConfig);

    const onTransportChange = (event: RadioChangeEvent): void => {
      if (setCurrentTransportType) {
        setCurrentTransportType(event.target.value);
      }
    };

    const sortingPriceExternal = (_externalClasses: ITaxi[]) => {
      const editExternalPrices: TExternalPricesWithMandatoryPrice[] | undefined = externalPrices?.map(item => ({
        ...item,
        price: item.price ? item.price : 0,
      }));
      const filterExternalPrices: TExternalPricesWithMandatoryPrice[] | undefined = editExternalPrices?.filter(
        el => el.taxiClass === tariffExternal?.type
      );
      const order = filterExternalPrices?.map(item => item.provider);
      const sortedExternalClasses
        = order && _externalClasses.sort((a, b) => order.indexOf(a.taxiClass) - order.indexOf(b.taxiClass));

      return sortedExternalClasses;
    };

    const sortingPriceItems = (_itemsWhichMayBeShown: ITaxi[]) => {
      const filterItemsWhichMayBeShown = _itemsWhichMayBeShown.map(el => findTariffs(el));
      const editPriceItems = filterItemsWhichMayBeShown?.map(item => {
        const costTransports = () => {
          if (item[0]?.transportType.name === TransportTypeEnum.PERSONAL) {
            return 10000;
          }

          if (item[0]?.transportType.name === TransportTypeEnum.PUBLIC) {
            return 4200;
          }

          return item[0]?.cost;
        };
        return ({
          ...item,
          transportType: item[0] && busClasses.includes(item[0].taxiClass as TaxiClassEnum) ? 'BUS' : item[0]?.transportType.name,
          cost: costTransports(),
        });
      });
      const order = editPriceItems?.sort((a, b) => (a.cost ?? 0) - (b.cost ?? 0)).map(item => item.transportType);
      const sortedPriceItems
        = order && _itemsWhichMayBeShown.sort((a, b) => order.indexOf(a.transportType) - order.indexOf(b.transportType));

      return sortedPriceItems;
    };

    const classes = isExternal ? sortingPriceExternal(externalClasses) : sortingPriceItems(itemsWhichMayBeShown);

    useEffect(() => {
      if (tripStore.groupTransferLoadWaypoint) {
        if (!tariffsInfo.length) {
          setStep(1);
          logger.toMessage('error', 'Постройте стандартный клиентский путь');
          tripStore.setGroupTransferLoadWaypoint(false);
        }
      }
    }, [tripStore.groupTransferLoadWaypoint]);

    return (
      <Form.Item name="taxiClass">
        {!tariffsInfo.length && step === 2 ? (
          <SpinWrapped />
        )
          : (
            <Radio.Group className={styles.wrapper__ant_radio_button_wrapper} onChange={onTransportChange}>
              <div className={classNames('with-hor-scroll-bar')}>
                {classes?.map(option => {
                  return findTariffs(option).map(tariff => {
                    const busTariffs = busClasses.includes(tariff?.taxiClass as TaxiClassEnum);

                    return (
                      <TaxiClassCardWrapper
                        key={tariff?.id}
                        option={option}
                        tariff={
                          busTariffs
                            ? ({ ...tariff, transportType: { id: tariff?.transportType.id, name: 'BUS' } } as any)
                            : tariff
                        }
                        tariffsInfo={tariffsInfo}
                        externalPrices={externalPrices}
                        isEditable={isEditable}
                        form={form}
                        purpose={purpose ?? ''}
                        tripLimit={tripLimit}
                        isExternal={isExternal}
                        economyTaxiCost={tariffsInfo.find(t => t.taxiClass === 'ECONOMY')?.cost}
                        setShowCarToolTrip={setShowCarToolTrip}
                        availableTaxiClasses={availableTaxiClasses}
                        // bonuses={bonuses}
                        product={product}
                        setProduct={setProduct}
                        setStep={setStep}
                        setSubClass={setSubClass}
                        setExternalProvider={setExternalProvider}
                        setProcess={setProcess}
                        loadExternalPrices={loadExternalPrices}
                        handleNextStep={handleNextStep}
                        limitPercentage={tariff?.limitPercentage}
                        limitAuthor={tariff?.limitAuthor}
                      />
                    );
                  });
                })}
              </div>
            </Radio.Group>
          )}
      </Form.Item>
    );
  }
);
