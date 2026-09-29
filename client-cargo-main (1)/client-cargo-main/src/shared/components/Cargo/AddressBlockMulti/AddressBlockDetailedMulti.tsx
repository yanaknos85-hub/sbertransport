import React from 'react';
import { Button, Space } from 'antd';
import classNames from 'classnames';
import { StoreNames } from 'ioc/ioc.storeNames';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { ContactMulti, WaypointMulti } from 'stores/Cargos/typesMulti';
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
   * Функция для разделения адреса на основную часть и детали
   * @param waypoint объекты маршрутов {@link WaypointModel[]}
   * @param mainAddress основной адрес
   * @param details дополнительные параметры адреса(подъезд, этаж, квартира)
   */
  const getAddressParts = (waypoint: WaypointModel): { mainAddress: string; details: string } => {
    let fullAddress = waypoint.addressString;
    let details = '';

    // Если доступен метод с деталями, используем его
    if (waypoint.getFormattedAddressWithDetails) {
      fullAddress = waypoint.getFormattedAddressWithDetails();
    }

    // Извлекаем детали (подъезд, этаж, кв/офис)
    const detailsRegex = /(, подъезд \d+|, этаж \d+|, кв\/офис \d+)/g;
    const matches = fullAddress.match(detailsRegex);

    if (matches && matches.length > 0) {
      // Основной адрес без деталей
      const mainAddress = fullAddress.replace(detailsRegex, '');
      // Детали (убираем первую запятую)
      details = matches.join('').substring(2);
      return { mainAddress, details };
    }

    return { mainAddress: fullAddress, details: '' };
  };

  const firstAddressBlock = (
    mainAddress: string,
    details: string,
    employee: ContactMulti,
    employees: WaypointMulti[]
  ): React.ReactElement => {
    const organization = employees[0].organization;
    return (
      <div className={styles.addressMainDivDetailed}>
        <div className={classNames(styles.addressCircle, styles.addressCircleFirst)}>A</div>
        <div className={classNames(styles.addressCircleSpacer1)} />
        <div className={styles.addressDiv}>
          <div className={styles.addressString2}>
            <span>{mainAddress}</span>
            {details && (
              <div className={styles.addressDetails}>
                {details}
              </div>
            )}
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

  const secondAddressBlockCollapsed = (index: number, length: number): React.ReactElement => (
    <div className={styles.addressMainDivDetailed}>
      <div className={classNames(styles.addressCircle, styles.addressCircleLast)}>
        {getAddressLetter(index)}
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
   * @param mainAddress адрес
   * @param employee человек, связанный с блоком
   * @param className класс для стилизации иконки. если она последняя в списке, меняем ей цвет
   */
  const otherAddressBlocks = (
    index: number,
    length: number,
    mainAddress: string,
    details: string,
    employee: ContactMulti,
    className: string,
    employees: WaypointMulti[]
  ): React.ReactElement => {
    const organization = employees[index]?.organization;
    return (
      <div className={styles.addressMainDivDetailed}>
        <div className={className}>{getAddressLetterExpanded(index)}</div>
        <div className={classNames(styles.addressCircleSpacer2, styles.addressCircleSpacerDetailed)} />
        <div className={classNames(styles.addressDiv)}>
          <div className={styles.addressString2}>
            <span>{mainAddress}</span>
            {details && (
              <div className={styles.addressDetails}>
                {details}
              </div>
            )}
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
  const switchAddressBlock = (
    index: number,
    length: number,
    waypoint: WaypointModel,
    employee: ContactMulti,
    employees: WaypointMulti[]
  ): JSX.Element => {
    const { mainAddress, details } = getAddressParts(waypoint);

    if (index === 0) {
      return firstAddressBlock(mainAddress, details, employee, employees);
    }
    if (index === 1 && !additionalAddressesVisible && length > 2) {
      return secondAddressBlockCollapsed(length - 1, length);
    }
    if (length > 2 && additionalAddressesVisible) {
      if (length - 1 === index) {
        return otherAddressBlocks(
          index,
          length,
          mainAddress,
          details,
          employee,
          classNames(styles.addressCircle, styles.addressCircleLast),
          employees
        );
      }
      return otherAddressBlocks(
        index,
        length,
        mainAddress,
        details,
        employee,
        classNames(styles.addressCircle),
        employees
      );
    }
    if (length === 2) {
      return otherAddressBlocks(
        index,
        length,
        mainAddress,
        details,
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
          title={getAddressParts(x).mainAddress + (getAddressParts(x).details ? ', ' + getAddressParts(x).details : '')}
        >
          {switchAddressBlock(index, array.length, x, employees[index]?.contacts[0], employees)}
        </div>
      ))}
    </div>
  );
};

export default AddressBlockDetailedMulti;
