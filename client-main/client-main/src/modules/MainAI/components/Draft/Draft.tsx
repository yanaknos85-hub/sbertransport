import React, { FC, useMemo } from 'react';
import {
  Map2GIS, Marker2GIS, MOSCOW, Polyline2GIS
} from '@sber-sbertransport/ui-kit/src';
import moment from 'moment/moment';
import cn from 'classnames';

import type { TDraftDto, TPlaceRef } from 'api/draft-pilot/draft-pilot.types';
import { TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';

import taxi from 'shared/images/taxi.png';
import { ReactComponent as Hands } from 'shared/icons/hands.svg';
import { formatRubles } from 'utils';

import styles from './Draft.module.scss';

interface DraftProps {
  draft: TDraftDto;
}

/** Форматирование PlaceRef в строку адреса */
const formatPlaceAddress = (place: TPlaceRef): string => {
  const parts = [place.name, place.country, place.region, place.city, place.street, place.house].filter(
    (p): p is string => !!p
  );
  return parts.join(', ') || 'Адрес не указан';
};

const formatPickupTime = (date?: string): string => {
  if (!date) return '';
  const m = moment.parseZone(date);
  const day = m.date();
  const monthNames = [
    'января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
    'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря',
  ];
  return `${day} ${monthNames[m.month()]} в ${m.format('HH:mm')}`;
};

const addressTitle = {
  first: 'Начало поездки',
  intermidiate: 'Промежуточная точка',
  last: 'Конец поездки',
};

interface AddressProps {
  address?: TPlaceRef;
  index: number;
  total: number;
}

const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

const Address: FC<AddressProps> = ({
  address,
  index,
  total,
}) => {
  const text = useMemo(() => formatPlaceAddress(address ?? {} as TPlaceRef), [address]);
  const title = index === 0 ? addressTitle.first : index === total - 1 ? addressTitle.last : addressTitle.intermidiate;
  const letter = LETTERS[index] ?? '';

  return (
    <div className={styles.address}>
      <div
        className={cn(styles.lineWrapper, {
          [styles.second]: index % 2 !== 0,
        })}
      >
        <div className={styles.circle}>{letter}</div>
        <div className={styles.line} />
      </div>

      <div>
        <div className={styles.markerText}>{title}</div>
        <div className={styles.addressText}>{text}</div>
      </div>
    </div>
  );
};

/** Форматирует миллисекунды в минуты */
const formatMsToMin = (ms?: number): string => {
  if (ms === undefined || ms === null) return '—';
  return `${Math.round(ms / 60000)} мин`;
};

/** Форматирует расстояние в км */
const formatKm = (km?: number): string => {
  if (km === undefined || km === null) return '—';
  return `${Math.round(km)} км`;
};

const Draft: FC<DraftProps> = ({ draft }) => {
  const pickupTime = useMemo(() => formatPickupTime(draft.pickupTime), [draft.pickupTime]);

  const routeData = draft.calculationResults?.route;
  const tariffs = draft.calculationResults?.tariffs;

  const travelTime = useMemo(() => formatMsToMin(routeData?.time), [routeData?.time]);
  const waitingTime = useMemo(() => {
    const totalWaitMs = routeData?.waypoints?.reduce((sum, wp) => sum + (wp.waitTime ?? 0), 0) ?? 0;
    return formatMsToMin(totalWaitMs);
  }, [routeData?.waypoints]);
  const distance = useMemo(() => formatKm(routeData?.distance), [routeData?.distance]);
  const cost = useMemo(() => {
    const taxiClass = draft.taxiClass;
    if (!taxiClass) return '—';
    const matchingTariff = tariffs?.find(t => t.taxiClass === taxiClass);
    if (!matchingTariff || matchingTariff.cost === undefined || matchingTariff.cost === null) return '—';
    return formatRubles(matchingTariff.cost / 100);
  }, [tariffs, draft.taxiClass]);

  const tripPurposeLabel = draft.tripPurpose?.label;
  const taxiClassTitle = draft.taxiClass ? TaxiClassTitlesEnum[draft.taxiClass] : '';

  return (
    <div className={styles.draft}>
      {/* ---- Карточка 1: детали поездки ---- */}
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.headerTitle}>{pickupTime}</span>
        </div>

        <div className={styles.bodyRow}>
          {/* Левая часть */}
          <div className={styles.leftCol}>
            <div className={styles.taxiLabel}>
              <img src={taxi} alt="Такси" />
              {taxiClassTitle ? <span>Такси • {taxiClassTitle}</span> : <span>Такси</span>}
            </div>
            {tripPurposeLabel && (
              <div className={styles.purposeBadge}>
                <Hands />
                <span>{tripPurposeLabel}</span>
              </div>
            )}
          </div>

          {/* Правая часть — плановые данные */}
          <div className={styles.rightCol}>
            <div className={styles.planTitle}>Плановые данные</div>
            <div className={styles.columns}>
              <div className={styles.column}>
                <span className={styles.colLabel}>Время в пути</span>
                <span className={styles.colValue}>{travelTime}</span>
              </div>
              <div className={styles.divider} />
              <div className={styles.column}>
                <span className={styles.colLabel}>Время ожидания</span>
                <span className={styles.colValue}>{waitingTime}</span>
              </div>
              <div className={styles.divider} />
              <div className={styles.column}>
                <span className={styles.colLabel}>Расстояние</span>
                <span className={styles.colValue}>{distance}</span>
              </div>
              <div className={styles.divider} />
              <div className={styles.column}>
                <span className={styles.colLabel}>Стоимость услуг</span>
                <span className={styles.colValue}>{cost}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ---- Карточка 2: маршрут ---- */}
      <div className={styles.card}>
        <div className={styles.routeHeader}>Маршрут</div>

        <div className={styles.routeBody}>
          <div className={styles.addresses}>
            {/* Левая часть — точки маршрута */}
            {[draft.pickupPoint, draft.destinationPoint].map((address, index, arr) => (
              <Address
                key={index}
                address={address}
                index={index}
                total={arr.length}
              />
            ))}
          </div>

          {/* Правая часть — карта */}
          <div className={styles.mapPlaceholder}>
            <Map2GIS
              containerStyle={{ height: '100%' }}
              zoomControl={false}
              disableDragging
              disablePitchByUserInteraction
              disableZoomOnScroll
              enableTrackResize={false}
              bounds={{
                southWest: [
                  draft.pickupPoint?.longitude ?? draft.destinationPoint?.longitude ?? MOSCOW[0],
                  draft.pickupPoint?.latitude ?? draft.destinationPoint?.latitude ?? MOSCOW[1],
                ],
                northEast: [
                  draft.destinationPoint?.longitude ?? draft.pickupPoint?.longitude ?? MOSCOW[0],
                  draft.destinationPoint?.latitude ?? draft.pickupPoint?.latitude ?? MOSCOW[1],
                ],
              }}
            >
              {[draft.pickupPoint, draft.destinationPoint].map((point, index) => !!point?.longitude && !!point?.latitude && (
                <Marker2GIS key={index} coordinates={[point.longitude, point.latitude]} />
              ))}
              {routeData?.segments?.map((segment, segIndex) => (
                <Polyline2GIS
                  key={segIndex}
                  coordinates={segment.coordinates.map(coord => [coord.longitude, coord.latitude])}
                  color="#1677ff"
                  width={4}
                  zIndex={1}
                />
              ))}
            </Map2GIS>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Draft;
