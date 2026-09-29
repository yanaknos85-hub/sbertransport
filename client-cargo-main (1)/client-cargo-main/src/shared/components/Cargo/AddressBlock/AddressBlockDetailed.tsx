import * as React from 'react';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { Button, Space } from 'antd';
import classNames from 'classnames';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import styles from './styles.module.scss';

/**
 * Преобразуем массив {@link WaypointModel[]} в графические блоки адресов с указанием людей и организаций
 * @param waypoints объекты маршрутов {@link WaypointModel[]}
 * @param employees люди, привязанные к точке маршрута
 * @param senderOrganization организация отправителя
 * @param recipientOrganization организация получателя
 * @class
 */
const AddressBlockDetailed: React.FC<any> = ({
  waypoints,
  employees,
  senderOrganization,
  recipientOrganization,
}: {
  waypoints: WaypointModel[];
  employees: EmployeeModel[];
  senderOrganization: string;
  recipientOrganization: string;
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
  const getAddressLetter = (index: number, length: number): string => {
    if (index === 0 || (index === 1 && length === 2)) {
      return getAddressLetterExpanded(index);
    }
    return '';
  };

  const renderFullName = (employeeName: string, externalName: string) => {
    return employeeName?.replaceAll(' ', '') !== '' ? employeeName : externalName;
  };

  /**
   * Отображаем первый по порядку блок
   * @param address адрес
   * @param employee человек, связанный с блоком
   */
  const firstAddressBlock = (address: string, employee: EmployeeModel): any => (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
      <div className={classNames(styles.addressCircleSpacer1)} />
      <div className={styles.addressDiv}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressFullName}>
            {renderFullName(employee?.fullName, externalSender.fullName)}
          </span>
          <span className={styles.addressMobilePhone}>{employee?.mobilePhone || externalSender?.mobilePhone}</span>
          <span className={styles.addressOrganization}>{senderOrganization}</span>
        </div>
      </div>
    </div>
  );

  /**
   * Отображаем второй по порядку блок в свернутом состоянии
   * @param index порядковый номер блока
   * @param length общее количество блоков
   */
  const secondAddressBlockCollapsed = (index: number, length: number): any => (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index, length)}
      </div>
      <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
      <div className={classNames(styles.addressDiv)}>
        <div className={classNames(styles.addressString2, styles.addressStringCollapsed)}>
          {length - 1}
          {' '}
          адреса
          {' '}
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
    employee: EmployeeModel,
    className: any
  ): any => (
    <div className={styles.addressMainDivDetailed}>
      <div className={className}>{getAddressLetterExpanded(index)}</div>
      <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
      <div className={classNames(styles.addressDiv)}>
        <div className={styles.addressString2}>
          <span>{address}</span>
          <span className={styles.addressFullName}>
            {renderFullName(employee?.fullName, externalRecipient?.fullName)}
          </span>
          <span className={styles.addressMobilePhone}>{employee?.mobilePhone || externalRecipient?.mobilePhone}</span>
          <span className={styles.addressOrganization}>{recipientOrganization}</span>
        </div>
      </div>
    </div>
  );

  /**
   * Выбираем необходимый нам блок алреса для отображения
   * @param index порядковый номер блока
   * @param length общее количество блоков
   * @param address адрес
   * @param employee человек, связанный с блоком
   */
  const switchAddressBlock = (index: number, length: number, address: string, employee: EmployeeModel): any => {
    if (index === 0) {
      return firstAddressBlock(address, employee);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(index, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(
          index,
          length,
          address,
          employee,
          classNames(styles.addressCircle, styles.addressCircleLast)
        );
      }
      return otherAddressBlocks(index, length, address, employee, classNames(styles.addressCircle));
    }
    if (length === 2) {
      return otherAddressBlocks(
        index,
        length,
        address,
        employee,
        classNames(styles.addressCircle, styles.addressCircleLast)
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
          {switchAddressBlock(index, array.length, x.addressString, employees[index])}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockDetailed;
