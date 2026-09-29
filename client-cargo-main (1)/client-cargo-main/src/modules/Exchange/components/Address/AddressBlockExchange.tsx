import React, { useState } from 'react';
import { Space } from 'antd';
import classNames from 'classnames';

import { Contacts, WaypointExchange } from '../../types';

import styles from './styles.module.scss';

const AddressBlockExchange: React.FC<{ waypoints: WaypointExchange[] }> = ({ waypoints }) => {
  const [additionalAddressesVisible, setAdditionalAddressesVisible] = useState(false);
  /**
   * По клику на кнопку переключаем режим отображения дополнительных адресов
   */
  const showAdditionalAddressesHandle = (): void => {
    setAdditionalAddressesVisible(!additionalAddressesVisible);
  };

  /**
   * Получаем отображаемую буквы в строке адреса для раскрытого блока
   * @param index индекс строки среди адресов
   */
  const getAddressLetterExpanded = (index: number): string => String.fromCharCode(97 + index).toUpperCase();

  /**
   * Получаем отображаемую буквы в строке адреса
   * @param index индекс строки среди адресов
   */
  const getAddressLetter = (index: number): string => getAddressLetterExpanded(index);

  /**
   * Отображаем первый по порядку блок
   * @param address адрес
   * @param contacts
   */
  const firstAddressBlock = (address: string, _contacts: Contacts[]): JSX.Element => (
    <div className={styles.addressMainDivDetailedJournal}>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <div className={classNames(styles.addressCircleSpacer1)} />
      <div className={styles.addressDiv}>
        <div className={styles.addressString2}>
          <span className={styles.title}>Адрес отправки</span>
          <span>{address}</span>
        </div>
      </div>
    </div>
  );

  /**
   * Отображаем второй по порядку блок в свернутом состоянии
   * @param index порядковый номер блока
   * @param length общее количество блоков
   */
  const secondAddressBlockCollapsed = (index: number, length: number): JSX.Element => (
    <div className={styles.addressMainDivDetailedJournal}>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index)}
      </div>
      <div className={classNames(styles.addressCircleSpacerLast)} />
      <div className={classNames(styles.addressDiv)}>
        <div className={classNames(styles.addressString2, styles.addressStringCollapsed)}>
          {length - 1}
          {' '}
          адреса
          {' '}
          <span className={styles.additionalAddressesButton} onClick={showAdditionalAddressesHandle}>
            Показать
          </span>
        </div>
      </div>
    </div>
  );

  /**
   * Отображаем остальные блоки
   * @param index порядковый номер блока
   * @param length общее количество блоков
   * @param address адрес
   * @param lastFlag
   * @param contacts
   */
  const otherAddressBlocks = (
    index: number,
    length: number,
    address: string,
    lastFlag: boolean,
    _contacts: Contacts[]
  ): JSX.Element => {
    return (
      <div className={styles.addressMainDivDetailedJournal}>
        <div className={lastFlag ? classNames(styles.addressCircle, styles.addressCircleLast) : styles.addressCircle}>
          {getAddressLetterExpanded(index)}
        </div>
        <div
          className={lastFlag ? classNames(styles.addressCircleSpacerLast) : classNames(styles.addressCircleSpacer2)}
        />
        <div className={classNames(styles.addressDiv)}>
          <div className={styles.addressString2}>
            <span className={styles.title}>Адрес доставки</span>
            <span>{address}</span>
          </div>
        </div>
      </div>
    );
  };

  /**
   * Выбираем необходимый нам блок алреса для отображения
   * @param index порядковый номер блока
   * @param length общее количество блоков
   * @param address адрес
   * @param contacts
   */
  const switchAddressBlock = (index: number, length: number, address: string, _contacts: Contacts[]): React.ReactNode => {
    if (index === 0) {
      return firstAddressBlock(address, _contacts);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length - 1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(index, length, address, true, _contacts);
      }
      return otherAddressBlocks(index, length, address, false, _contacts);
    }
    if (length === 2) {
      return otherAddressBlocks(index, length, address, true, _contacts);
    }
    return <Space />;
  };

  return (
    <div>
      <div className={styles.addressMainDiv}>
        {additionalAddressesVisible ? (
          <span className={styles.additionalAddressesButton} onClick={showAdditionalAddressesHandle}>
            Скрыть промежуточные
          </span>
        ) : (
          <Space />
        )}
      </div>
      {waypoints?.map((x, index, array) => (
        <div
          className={styles.address}
          key={index}
          title={x.address.addressStringRepresentation}
        >
          {switchAddressBlock(index, array.length, x.address.addressStringRepresentation || '', x?.contacts || [])}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockExchange;
