import React from 'react';
import { Button, Space } from 'antd';
import classNames from 'classnames';
import { StoreNames } from 'ioc/ioc.storeNames';
import { ADDRESSES_DECLENSION_DESCRIPTION } from 'shared/constants/constants';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { ContactMulti, WaypointMulti } from 'stores/Cargos/typesMulti';
import { declension } from 'utils/Misc';
import { formatterPhoneNumber } from 'modules/CargoMultiple/utils';

import styles from './styles.module.scss';

/**
 * Преобразуем массив {@link WaypointModel[]} в графические блоки адресов с указанием людей и организаций
 * @param waypoints объекты маршрутов {@link WaypointModel[]}
 * @param employees люди, привязанные к точке маршрута
 * @param senderOrganization организация отправителя
 * @param recipientOrganization организация получателя
 * @class
 */

interface Props {
  waypoints: WaypointModel[];
  employees: WaypointMulti[];
  senderOrganization: string;
  recipientOrganization: string;
}

const AddressBlockDetailedMulti: React.FC<Props> = ({
  waypoints,
  employees,
}) => {
  const { [StoreNames.cargosStore]: cargosStore } = useAppStoreContext();
  const { externalSender, externalRecipient } = cargosStore;

  const [additionalAddressesVisible, setAdditionalAddressesVisible] = React.useState(false);

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

  /**
   * Отображаем первый по порядку блок
   * @param address адрес
   * @param employee человек, связанный с блоком
   */
  const firstAddressBlock = (address: string, employee: ContactMulti, employees: WaypointMulti[]): React.ReactElement => {
    const organization = employees[0].organization;
    return (
      <div className={styles.addressMainDivDetailed}>
        <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
        <div className={classNames(styles.addressCircleSpacer1)} />
        <div className={styles.addressDiv}>
          <div className={styles.addressString2}>
            <span>{address}</span>
            <span className={styles.addressFullName}>
              {employee?.fullName}
            </span>
            <span className={styles.addressMobilePhone}>
              {formatterPhoneNumber(employee?.mobilePhone) || formatterPhoneNumber(externalSender?.mobilePhone)}
            </span>
            <span className={styles.addressOrganization}>{organization}</span>
          </div>
        </div>
      </div>
    );
  };

  /**
   * Отображаем второй по порядку блок в свернутом состоянии
   * @param index порядковый номер блока
   * @param length общее количество блоков
   */
  const secondAddressBlockCollapsed = (index: number, length: number): React.ReactElement => (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index)}
      </div>
      <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
      <div className={classNames(styles.addressDiv)}>
        <div className={classNames(styles.addressString2, styles.addressStringCollapsed)}>
          `$
          {length - 1}
          {' '}
          $
          {declension(length - 1, ADDRESSES_DECLENSION_DESCRIPTION)}
          `
          <Button
            className={styles.showAdditionalAddressesButton}
            type="link"
            onClick={showAdditionalAddressesHandle}
          >
            Показать
          </Button>
        </div>
      </div>
    </div>
  );

  /**
   * Отображаем остальные блоки
   * @param index порядковый номер блока
   * @param length общее количество блоков
   * @param address адрес
   * @param employee человек, связанный с блоком
   * @param className класс для стилизации иконки. если она последняя в списке, меняем ей цвет
   */
  const otherAddressBlocks = (
    index: number,
    length: number,
    address: string,
    employee: ContactMulti,
    className: string,
    employees: WaypointMulti[]
  ): React.ReactElement => {
    const organization = employees[index].organization;
    return (
      <div className={styles.addressMainDivDetailed}>
        <div className={className}>{getAddressLetterExpanded(index)}</div>
        <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
        <div className={classNames(styles.addressDiv)}>
          <div className={styles.addressString2}>
            <span>{address}</span>
            <span className={styles.addressFullName}>
              {employee?.fullName}
            </span>
            <span
              className={styles.addressMobilePhone}
            >
              {formatterPhoneNumber(employee?.mobilePhone) || formatterPhoneNumber(externalRecipient?.mobilePhone)}
            </span>
            <span className={styles.addressOrganization}>{organization}</span>
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
   * @param employee человек, связанный с блоком
   */
  const switchAddressBlock = (index: number, length: number, address: string, employee: ContactMulti, employees: WaypointMulti[]): JSX.Element => {
    if (index === 0) {
      return firstAddressBlock(address, employee, employees);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length - 1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(
          index,
          length,
          address,
          employee,
          classNames(styles.addressCircle, styles.addressCircleLast),
          employees
        );
      }
      return otherAddressBlocks(index, length, address, employee, classNames(styles.addressCircle), employees);
    }
    if (length === 2) {
      return otherAddressBlocks(
        index,
        length,
        address,
        employee,
        classNames(styles.addressCircle, styles.addressCircleLast),
        employees
      );
    }
    return <Space />;
  };

  return (
    <div>
      <div className={styles.addressMainDiv}>
        <div className={styles.addressTitle}>Адреса</div>
        {additionalAddressesVisible ? (
          <Button
            className={styles.hideAdditionalAddressesButton}
            type="link"
            onClick={showAdditionalAddressesHandle}
          >
            Скрыть промежуточные
          </Button>
        ) : (
          <Space />
        )}
      </div>
      {waypoints.map((x, index, array) => (
        <div
          className={styles.address}
          key={x.latitude + x.longitude}
          title={x.addressString}
        >
          {switchAddressBlock(index, array.length, x.addressString, employees[index].contacts[0], employees)}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockDetailedMulti;
