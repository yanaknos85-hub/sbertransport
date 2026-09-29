/* eslint-disable react/no-danger */
import React, { FC, ReactNode } from 'react';
import { Divider } from 'antd';
import { observer } from 'mobx-react';
import { emptySign } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import useTotal from 'shared/hooks/useTotal';
import { ReactComponent as Distance } from 'shared/images/cargo/total_distance.svg';
import { ReactComponent as Time } from 'shared/images/cargo/total_time.svg';
import { ReactComponent as Volume } from 'shared/images/cargo/total_volume.svg';
import { ReactComponent as Weight } from 'shared/images/cargo/total_weight.svg';
import {
  declOfNum, getVolume, getWeight, toRubles
} from 'utils';

import { PackageItem, StepFinalValues } from 'stores/Cargo/Cargo.interface';
import { TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import {
  CalculatedTariff, CargoListItem, Expected, transportTypeTitles
} from 'types/Cargo';
import * as utils from 'utils/Misc';

import { Bottom, Container, Total } from './CargoTotal.styleMulitple';
import CargoTotalItemMulitple from './CargoTotalItemMulitple';
import { DetailingTextMulitple } from './DetailingTextMulitple';

import styles from 'modules/CargosMultiple/static/Cargos.module.scss';

interface Props {
  calculatedTariff?: CalculatedTariff;
  cargoList: CargoListItem[];
  packageList?: PackageItem[];
  tariffCost: TariffCost | null;
  expected: Expected;
  button?: (total: StepFinalValues) => void;
  bottomElement?: ReactNode;
  topElement?: ReactNode;
  fileElement?: ReactNode;
  isRegular?: boolean;
  allCost?: number;
  sourceLoadersCount: number;
  destinationLoadersCount: number;
  totalCost?: number;
}

const CargoTotalMulitple: FC<Props> = observer(
  ({
    calculatedTariff,
    cargoList,
    packageList,
    tariffCost,
    button,
    bottomElement,
    topElement,
    fileElement,
    isRegular,
    totalCost,
    allCost,
  }) => {
    const {
      cost,
      expressCost,
      loaderCost,
      deliveryTime,
      length,
      height,
      weight,
      width,
      volume,
      occupiedPlacesCount,
      loaders,
    } = useTotal({ cargoList, tariffCost });

    const { cargoStore } = useAppStoreContext();

    const filteredCargoList = cargoList.filter(item => item.occupiedPlacesCount > 0);

    const distance = tariffCost?.distance || 0;

    const rub = (value: number) => `${toRubles(value, true).toFixed(2)}&#8381`;

    const total = {
      cost: cost ? rub(cost) : undefined,
      expressCost: expressCost ? rub(expressCost) : emptySign,
      loaderCost: loaderCost ? rub(loaderCost) : emptySign,
      calculatedTariffCost: calculatedTariff ? rub(calculatedTariff.cost) : emptySign,
      calculatedTariffBaseCost: calculatedTariff ? rub(calculatedTariff.priceDetails.baseCost) : emptySign,
      calculatedTariffExpressCost: calculatedTariff ? rub(calculatedTariff.priceDetails.expressCost) : emptySign,
      calculatedTariffLoaderCost: calculatedTariff ? rub(calculatedTariff.priceDetails.loaderCost) : emptySign,
      deliveryTime: deliveryTime ? `${deliveryTime} ${declOfNum(deliveryTime, ['день', 'дня', 'дней'])}` : emptySign,
      distance: distance ? `${distance.toFixed(0)} км` : emptySign,
      weight: weight ? utils.getWeight(weight) : emptySign,
      volume: volume ? utils.getVolume(volume) : emptySign,
      loaders: loaders ? rub(loaders) : emptySign,
    };

    const getTotalCost = (regularCost: number, oneTimeCost?: string) => {
      if (isRegular) {
        return regularCost > 0 ? rub(regularCost) : `0 &#8381`;
      }
      if (oneTimeCost === undefined) {
        return emptySign;
      }
      return oneTimeCost;
    };

    const transportTitle = () => {
      if (tariffCost) {
        return transportTypeTitles[tariffCost.transportType.name];
      }

      if (calculatedTariff) {
        return transportTypeTitles[calculatedTariff.transportType.name];
      }

      return undefined;
    };

    function getValueForOutput(total: { calculatedTariffBaseCost: string; calculatedTariffCost: string; cost: string | undefined }) {
      if (total.calculatedTariffBaseCost === emptySign && total.calculatedTariffCost) {
        return total.cost;
      } else if (typeof total.calculatedTariffBaseCost === 'string') {
        return total.calculatedTariffBaseCost;
      } else if (typeof total.calculatedTariffCost === 'string') {
        return total.calculatedTariffCost;
      } else {
        return emptySign;
      }
    }

    const totalDeliveryCost = getTotalCost(totalCost || allCost || 0, total.cost || total.calculatedTariffCost);
    const totalWeight = getWeight(cargoList.reduce((acc: number, curr: CargoListItem) => acc + (curr.weight * curr.occupiedPlacesCount || 0), 0));
    const totalVolume = getVolume(cargoList.reduce((acc: number, curr: CargoListItem) => acc + (curr.volume * curr.occupiedPlacesCount || 0), 0));
    const baseCost = (tariffCost ? rub(tariffCost?.priceDetails.baseCost) : undefined);

    return (
      <Container>
        <DetailingTextMulitple
          title="Итого"
          total={`${cargoList.length} ${declOfNum(cargoList.length, ['посылка', 'посылки', 'посылок'])}`}
          titleSize="20px"
          titleFontWeight={600}
          titleFontFamily="SB Sans Interface"
          textSize="14px"
          textColor="#adadad"
        />
        <Total>
          <CargoTotalItemMulitple
            img={<Weight />}
            data={totalWeight || emptySign}
            description="масса"
          />
          <CargoTotalItemMulitple
            img={<Volume />}
            data={totalVolume || emptySign}
            description="объём"
          />
          <CargoTotalItemMulitple
            img={<Time />}
            data={calculatedTariff?.deliveryTime || total.deliveryTime || emptySign}
            description="срок"
          />
          <CargoTotalItemMulitple
            img={<Distance />}
            data={total.distance || emptySign}
            description="расстояние"
          />
        </Total>
        <Divider style={{ marginTop: '8px' }} />
        {topElement}
        <div className={styles.detailing}>
          {!!transportTitle() && (
            <>
              <DetailingTextMulitple
                title={transportTitle()}
                total={baseCost || getValueForOutput(total)}
                mb="16px"
              />
              {(!!expressCost || !!calculatedTariff?.priceDetails.expressCost) && (
                <DetailingTextMulitple
                  title="Доплата за срочность (Экспресс)"
                  total={total.expressCost || total.calculatedTariffExpressCost}
                  mb="16px"
                />
              )}
              {!!loaders && (
                <DetailingTextMulitple
                  title={`Грузчик х ${loaders}`}
                  total={total.loaderCost}
                  mb="16px"
                />
              )}
              {calculatedTariff?.priceDetails.packageCost && (
                <DetailingTextMulitple
                  title="Упаковка"
                  total={rub(calculatedTariff?.priceDetails.packageCost)}
                  mb="16px"
                />
              )}
              {/* Список вещей для переезда */}
              {cargoStore.isRelocation && filteredCargoList && filteredCargoList?.length > 0 && (
                <>
                  <div className={styles.detailingTexTitle}>Вещи, мебель и техника</div>
                  {filteredCargoList?.map(item => (
                    <DetailingTextMulitple
                      title={`${item.cargoName} x ${item.occupiedPlacesCount}`}
                      mb="4"
                      ml="16px"
                    />
                  ))}
                </>
              )}
              {/* Список упаковки для переезда */}
              {cargoStore.isRelocation && packageList && packageList?.length > 0 && (
                <>
                  <div className={styles.detailingTexTitle}>Упаковка</div>
                  {packageList?.map(item => (
                    <DetailingTextMulitple
                      title={`${item.packageTitle} x ${item.packageCount}`}
                      mb="4px"
                      ml="16px"
                    />
                  ))}
                </>
              )}
            </>
          )}
        </div>
        <DetailingTextMulitple
          title="Общая стоимость"
          total={totalDeliveryCost}
          titleSize="16px"
          titleFontFamily="SB Sans Interface"
          titleFontWeight={600}
          textFontWeight={600}
          textSize="24px"
        />
        <Bottom>
          {button
          && button({
            cost,
            length,
            height,
            weight,
            volume,
            width,
            occupiedPlacesCount,
            deliveryTime,
          })}
        </Bottom>
        <Bottom>{bottomElement}</Bottom>
        <Bottom>{fileElement}</Bottom>
      </Container>
    );
  }
);

export default CargoTotalMulitple;
