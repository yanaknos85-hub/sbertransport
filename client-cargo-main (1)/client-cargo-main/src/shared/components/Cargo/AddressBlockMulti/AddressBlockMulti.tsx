import React, { FC, ReactNode } from 'react';
import { useState } from 'react';
import { Space, Tooltip } from 'antd';
import classNames from 'classnames';
import { Contact, WaypointModel } from 'shared/models/geo/Waypoint.model';

import ContactItem from './ContactItem';

import styles from './styles.module.scss';

/**
 * Преобразуем массив {@link WaypointModel[]} в графические блоки адресов с указанием людей и организаций
 * @param waypoints объекты маршрутов {@link WaypointModel[]}
 * @class
 */
const AddressBlockMulti: FC<{ waypoints: WaypointModel[] }> = ({ waypoints }) => {
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
   * @param length общая длина массива адресов
   */
  const getAddressLetter = (index: number): string => getAddressLetterExpanded(index);

  const showFirstContactWithTooltip = (contacts: Contact[]): ReactNode => {
    if (!contacts || contacts.length === 0) return null;

    const firstContact = contacts[0];
    const remainingContacts = contacts.slice(1);

    if (remainingContacts.length > 0) {
      return (
        <Tooltip
          placement="topLeft"
          title={(
            <div>
              {contacts.map((c, i) => (
                <div key={c.id} className={styles.tooltipContact}>
                  {i + 1}
                  .
                  {c.fullName}
                  {c.mobilePhone && <span className={styles.contact}>{c.mobilePhone}</span>}
                </div>
              ))}
            </div>
          )}
        >
          <div>
            <ContactItem key={firstContact.id} contact={firstContact} />
            <span className={styles.contact}>...</span>
          </div>
        </Tooltip>
      );
    }

    return <ContactItem key={firstContact.id} contact={firstContact} />;
  };

  /**
   * Отображаем первый по порядку блок
   * @param address адрес
   */
  const firstAddressBlock = (address: string, contacts: Contact[]): ReactNode => (
    <div className={styles.addressMainDivDetailedJournal}>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <div className={classNames(styles.addressCircleSpacer1)} />
      <div className={styles.addressDiv}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <div className={styles.addressContactName}>
            {showFirstContactWithTooltip(contacts.filter(Boolean))}
          </div>
        </div>
      </div>
    </div>
  );

  /**
   * Отображаем второй по порядку блок в свернутом состоянии
   * @param index порядковый номер блока
   * @param length общее количество блоков
   */
  const secondAddressBlockCollapsed = (index: number, length: number): ReactNode => (
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
   * @param className класс для стилизации иконки. если она последняя в списке, меняем ей цвет
   */
  const otherAddressBlocks = (
    index: number,
    length: number,
    address: string,
    lastFlag: boolean,
    contacts: Contact[]
  ): JSX.Element => (
    <div className={styles.addressMainDivDetailedJournal}>
      <div className={lastFlag ? classNames(styles.addressCircle, styles.addressCircleLast) : styles.addressCircle}>
        {getAddressLetterExpanded(index)}
      </div>
      <div
        className={lastFlag ? classNames(styles.addressCircleSpacerLast) : classNames(styles.addressCircleSpacer2)}
      />
      <div className={classNames(styles.addressDiv)}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressContactName}>
            {showFirstContactWithTooltip(contacts.filter(Boolean))}
          </span>
        </div>
      </div>
    </div>
  );

  /**
   * Выбираем необходимый нам блок алреса для отображения
   * @param index порядковый номер блока
   * @param length общее количество блоков
   * @param address адрес
   */
  const switchAddressBlock = (index: number, length: number, address: string, contacts: Contact[]): ReactNode => {
    if (index === 0) {
      return firstAddressBlock(address, contacts);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length - 1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(index, length, address, true, contacts);
      }
      return otherAddressBlocks(index, length, address, false, contacts);
    }
    if (length === 2) {
      return otherAddressBlocks(index, length, address, true, contacts);
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
          title={x.addressStringRepresentation || x.addressString}
        >
          {switchAddressBlock(index, array.length, x.addressStringRepresentation || x.addressString || '', x?.contacts || [])}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockMulti;
