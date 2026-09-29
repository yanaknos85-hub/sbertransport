/* eslint-disable prefer-destructuring */
/* eslint-disable jsx-a11y/click-events-have-key-events */
import React, { FC, useEffect } from 'react';
import { Col, Row } from 'antd';
import moment from 'moment';
import CargoTypeIcon from 'shared/components/Cargo/CargoTypeIcon/CargoTypeIcon';
import Waypoints from 'shared/components/Cargo/Waypoints/Waypoints';
import { emptySign } from 'shared/constants/constants';
import Select from 'shared/form/Select/Select';

import { RequestListItem } from 'stores/CargoMassMultiple/CargoMassMultiple.interface';
import { CargoTypeCategoryNameEnum, TariffCost, TariffType } from 'stores/CargoTariff/CargoTariff.interface';
import { TransportTypeEnum, transportTypeTitles } from 'types/Cargo';
import { DATE_FORMAT } from 'constants/constants.app';
import * as utils from 'utils/Misc';
import { TariffsOption } from 'modules/CargoMassMultiple/types';

import * as S from './CargoMassItemMultiple.style';
import { ReactComponent as DeleteIcon } from './images/delete.svg';
import { ReactComponent as EditIcon } from './images/edit.svg';

interface Props {
  item: RequestListItem;
  onChangeTariff: (requestId: string, tariffType: TariffType) => void;
  onEdit: (data: RequestListItem) => void;
  onDelete: (data: string) => void;
  isRegular: boolean;
}

const CargoItemMultiple: FC<Props> = (props: Props) => {
  const {
    item, onChangeTariff, onEdit, onDelete, isRegular,
  } = props;
  const {
    humanReadableId, expected, tariffs = [], totalSizes, listCargo,
  } = item;

  const desiredDate = moment(item.desiredDate).format(DATE_FORMAT.DATE_WITH_TIME_DOTS);

  const currentTariff = tariffs.find(tariff => tariff.active) || tariffs[0];
  const currentTariffName = currentTariff?.transportType.name || '';

  const tariffsOptions: TariffsOption[] = tariffs.length
    ? item.tariffs.map((tariff: TariffCost) => ({
      label: transportTypeTitles[tariff.transportType.name as TransportTypeEnum],
      value: tariff.transportType.name,
    }))
    : [];

  const isOversized = listCargo.some(cargo => (
    cargo.cargoCategory === CargoTypeCategoryNameEnum.LIQUID || cargo.cargoCategory === CargoTypeCategoryNameEnum.BULK
  ));

  const deliveryTime = currentTariff?.deliveryTime
    ? `${currentTariff.deliveryTime} ${utils.declOfNum(currentTariff.deliveryTime, ['день', 'дня', 'дней'])}` : emptySign;
  const weight = `Вес ${!isOversized ? utils.getWeight(totalSizes.weight) : emptySign}`;
  const volume = `Объём ${utils.getVolume(totalSizes.volume)}`;

  useEffect(() => {
    const currentTariff = tariffs.find(tariff => tariff.active);
    if (!currentTariff) {
      onChangeTariff(item.id, tariffs[0]?.transportType.name);
    }
  }, []);
  /**
  // todo подумать как склеивать после сортировки, пока не отображаю ничего. Надо вынести вне
  const prefTariff = prevItem?.tariffs.find(tariff => tariff.active);

  // Смотрим,
  // если приходят только минуты или время - приводим к 1 дню
  // если приходят приходят и дни и время - приводим к кол-во дней + 1

  // eslint-disable-next-line prefer-const
  let { days: currentDays, hours: currentHours, minutes: currentMinutes } = utils.getDaysTime(currentTariff?.deliveryTime);
  currentDays = !currentDays && (currentHours || currentMinutes) ? 1 : (currentDays && (currentHours || currentMinutes) ? currentDays + 1 : currentDays);

  // eslint-disable-next-line prefer-const
  let { days: prevDays, hours: prevHours, minutes: prevMinutes } = utils.getDaysTime(prefTariff?.deliveryTime);
  prevDays = !prevDays && (prevHours || prevMinutes) ? 1 : (prevDays && (prevHours || prevMinutes) ? prevDays + 1 : prevDays);

  // далле сраваниваем, если кол-во дней одинаковое - склеиваем
  const coundDays = currentDays !== prevDays ? currentDays : null;
   */

  return (
    <>
      {/* {coundDays && (
        <Days first={index === 0}>
          Срок доставки <span>{coundDays} {declension(['день', 'дня', 'дней'], coundDays)}</span>
        </Days>
      )} */}
      <S.Item>
        <S.Inner paddingBottom="16px">
          <S.Header>
            <S.RequestID>
              {humanReadableId}
              {/* {' '}
              {status === RequestStatus.ERROR && (
                <>*Не все поля заполнены</>
              )} */}
            </S.RequestID>
            {!isRegular
              ? (
                <S.CargosParams>
                  Дата отправления
                  {' '}
                  {desiredDate}
                </S.CargosParams>
              )
              : null}
            <S.Icons>
              <EditIcon
                role="button"
                tabIndex={0}
                onClick={() => onEdit(item)}
              />
              <DeleteIcon
                role="button"
                tabIndex={0}
                onClick={() => onDelete(item.id)}
              />
            </S.Icons>
            <S.Cargos>
              <S.CargosList>
                <S.CargosItem>{listCargo.map(({ cargoName }) => cargoName).join(', ')}</S.CargosItem>
              </S.CargosList>
              <S.CargosParams>
                <S.Dot>•</S.Dot>
                {volume}
                <S.Dot>•</S.Dot>
                {weight}
              </S.CargosParams>
            </S.Cargos>
            <S.CargoTypes>
              {[...Array.from(new Set(listCargo.filter(({ cargoType }) => cargoType)))].map(({ cargoType, cargoName }) => (
                <S.CargoType key={cargoType + cargoName}>
                  <CargoTypeIcon name={cargoType} />
                </S.CargoType>
              ))}
            </S.CargoTypes>
          </S.Header>
        </S.Inner>
        <S.Divider />
        <S.Inner
          paddingTop="0"
          paddingLeft="0"
          paddingBottom="0"
          paddingRight="0"
        >
          <Row>
            <Col span={8}>
              <S.Inner paddingTop="16px" borderRight={true}>
                <Waypoints waypoints={expected.waypoints} />
              </S.Inner>
            </Col>
            <Col span={16}>
              <S.Inner flexCenter={true}>
                <S.Params>
                  <S.Param first={true}>
                    <S.Label>Тип доставки</S.Label>
                    <Select
                      value={currentTariffName}
                      options={tariffsOptions}
                      placeholder="Выберите тип доставки"
                      onChange={(value: any) => onChangeTariff(item.id, value)}
                    />
                  </S.Param>
                  <S.Params value={true}>
                    <S.Param>
                      <S.Label>Срок</S.Label>
                      <S.Value dangerouslySetInnerHTML={{ __html: deliveryTime || emptySign }} />
                    </S.Param>
                    <S.Param>
                      <S.Label price={true}>Стоимость</S.Label>
                      <S.Value price={true}>
                        {currentTariff?.cost ? currentTariff.cost / 100 : 0}
                        {' '}
                        &#8381;
                      </S.Value>
                    </S.Param>
                  </S.Params>
                </S.Params>
              </S.Inner>
            </Col>
          </Row>
        </S.Inner>
      </S.Item>
    </>
  );
};

export default CargoItemMultiple;
