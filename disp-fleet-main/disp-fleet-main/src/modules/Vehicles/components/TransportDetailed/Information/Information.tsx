import React, { FC } from 'react';
import { Col, Row } from 'antd';
import classnames from 'classnames';
import moment from 'moment';

import { useGetAutopark } from 'api/contractors/contractors.api';
import { useAutopark as useAutoparkBranch } from 'api/autopark/autopark.api';
import { Transport, TransportStatus, StatusesNames } from 'api/transport/transport.types';
import { DATE_FORMAT, emptySign } from 'constants/app.constants';
import { numberWithSpaces } from 'utils/utils';
import DetailedItem from '../DetailedItem/DetailedItem';

import ptsIcon from 'assets/images/pts_icon.png';
import stsIcon from 'assets/images/sts_icon.png';
import styles from './Information.module.scss';

interface IProps {
  vehicle: Transport['vehicle'];
  location: Transport['location'];
  documents: Transport['documents'];
  engine: Transport['engine'];
  general: Transport['general'];
  service: Transport['service'];
  contractorId?: Transport['contractorId'];
  autoparkId?: Transport['autoparkId'];
}

const seriesAndNumbersValue = (value: string) => value.length > 4 ? value.slice(0, 4) + ' ' + value.slice(4, value.length) : value;

const Information: FC<IProps> = ({
  vehicle, location, documents, engine, general, service, contractorId, autoparkId,
}) => {
  const autopark = useGetAutopark(contractorId!, { enabled: contractorId }).data;

  const autoparkBranch = useAutoparkBranch(
    { contractorId: contractorId!, autoparkId: autoparkId! },
    {
      enabled: contractorId && autoparkId,
    }
  ).data;

  return (
    <div className={styles.wrap}>
      <div className={styles.section}>
        <div className={styles.sectionHeader}>Автомобиль</div>

        <div className={styles.panel}>
          <Row gutter={[0, 16]}>
            <Col span={12}>
              <DetailedItem title="Страна изготовитель">{vehicle.manufacturer}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Статус">
                <div
                  className={classnames(
                    styles.status,
                    vehicle.status === TransportStatus.IN_USE ? styles.status_use : styles.status_notUse
                  )}
                >
                  <span>{StatusesNames[vehicle.status]}</span>
                </div>
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="VIN">{vehicle.vinCode}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Инвентарный номер">{vehicle.inventoryNumber ?? emptySign}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Номер кузова">{vehicle.bodyNumber ?? emptySign}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Номер основного средства">{vehicle.assetNumber}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Номер шасси">{vehicle.chassisNumber ?? emptySign}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Вид">{vehicle.type}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Привод">{vehicle.drive}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Подвид">{vehicle.subtype}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Экологический класс">{vehicle.ecologicalClass}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Кузов">{vehicle.bodyType}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Трансмиссия">{vehicle.transmissionType}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Период выпуска">{vehicle.manufacturePeriod}</DetailedItem>
            </Col>
          </Row>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>Расположение</div>

        <div className={styles.panel}>
          <div className={styles.placement}>
            <Row>
              <Col span={12}>
                <DetailedItem title="Автопарк">{autopark?.name ?? emptySign}</DetailedItem>
              </Col>

              <Col span={12}>
                <DetailedItem title="Филиал">{autoparkBranch?.name ?? emptySign}</DetailedItem>
              </Col>
            </Row>

            <Row>
              <Col span={12}>
                <DetailedItem title="Дата начала эксплуатации">
                  {moment(location.exploitationStart).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
                </DetailedItem>
              </Col>

              <Col span={12}>
                <DetailedItem title="Дата окончания эксплуатации">
                  {location.exploitationEnd
                    ? moment(location.exploitationEnd).format(DATE_FORMAT.BASE_REVERTED_DOTS)
                    : emptySign}
                </DetailedItem>
              </Col>
            </Row>
          </div>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>Документы</div>

        <div className={styles.panel}>
          <div className={styles.documents}>
            <div className={styles.iconCard}>
              <img src={ptsIcon} alt="icon" />

              <DetailedItem title="ПТС">
                <div className={styles.docTextWrap}>
                  <span className={styles.docNum}>{seriesAndNumbersValue(documents.passportNumber)}</span>

                  <span className={styles.docWhen}>
                    выдан
                    {' '}
                    {moment(documents.passportIssuedDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
                  </span>
                </div>
              </DetailedItem>
            </div>

            <DetailedItem title="Марка по ПТС">{documents.brandByPassport}</DetailedItem>

            <DetailedItem title="Модель по ПТС">{documents.modelByPassport}</DetailedItem>
          </div>
        </div>

        <div className={styles.panel}>
          <div className={styles.documents}>
            <div className={styles.iconCard}>
              <img src={stsIcon} alt="icon" />

              <DetailedItem title="СТС">
                <div className={styles.docTextWrap}>
                  <span className={styles.docNum}>{seriesAndNumbersValue(documents.certificateNumber)}</span>

                  <span className={styles.docWhen}>
                    выдан
                    {' '}
                    {moment(documents.certificateIssuedDate).format(DATE_FORMAT.BASE_REVERTED_DOTS)}
                  </span>
                </div>
              </DetailedItem>

              <DetailedItem title="Тип">{vehicle.subtype}</DetailedItem>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>Двигатель</div>

        <div className={styles.panel}>
          <Row gutter={[0, 16]}>
            <Col span={12}>
              <DetailedItem title="Двигатель">{engine.engineType}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Мощность, ЛС">{numberWithSpaces(engine.enginePower)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title={`Объем, см\u00B3`}>{numberWithSpaces(engine.engineCapacity)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Топливо">{engine.fuelType}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Городской расход л/100км">{engine.cityConsumptionRate}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Загородный расход л/100км">{engine.countryConsumptionRate}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Смешанный расход л/100км">{engine.hybridConsumptionRate}</DetailedItem>
            </Col>
          </Row>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>Общие характеристики</div>

        <div className={styles.panel}>
          <Row gutter={[0, 16]}>
            <Col span={12}>
              <DetailedItem title="Ширина, мм">{numberWithSpaces(general.width)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Категория ТС">{general.category}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Длина, мм">{numberWithSpaces(general.length)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Наименование ТС">{general.categoryName}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Высота, мм">{numberWithSpaces(general.height)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Объем топливного бака">{numberWithSpaces(general.fuelTankVolume)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Масса без нагрузки, кг">{numberWithSpaces(general.weight)}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Держатель запасного колеса">
                {general.spareWheelHolderInstalled ? 'Да' : 'Нет'}
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Разрешенная максимальная масса, кг">
                {numberWithSpaces(general.maxWeight)}
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Наличие брызговиков">{general.mudguardInstalled ? 'Да' : 'Нет'}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Цвет кузова">{general.bodyColor}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Телематика">{general.telematics ?? emptySign}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Размер передних колес">{general.frontWheelSize ?? emptySign}</DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Размер задних колес">{general.rearWheelSize ?? emptySign}</DetailedItem>
            </Col>
          </Row>
        </div>
      </div>

      <div className={styles.section}>
        <div className={styles.sectionHeader}>Обслуживание</div>

        <div className={styles.panel}>
          <Row gutter={[0, 16]}>
            <Col span={12}>
              <DetailedItem title="Межсервисный интервал по пробегу, км">
                {numberWithSpaces(service.serviceIntervalMileage)}
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Допуск по пробегу, км">
                {numberWithSpaces(service.serviceAuthorizationMileage)}
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Межсервисный интервал по времени, дни">
                {numberWithSpaces(service.serviceIntervalDays)}
              </DetailedItem>
            </Col>

            <Col span={12}>
              <DetailedItem title="Допуск по времени, дни">
                {numberWithSpaces(service.serviceAuthorizationDays)}
              </DetailedItem>
            </Col>
          </Row>
        </div>
      </div>
    </div>
  );
};

export default Information;
