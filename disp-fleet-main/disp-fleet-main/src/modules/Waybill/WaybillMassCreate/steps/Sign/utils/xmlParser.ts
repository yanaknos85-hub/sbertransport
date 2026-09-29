/**
 * Утилита для парсинга XML-документа из Base64
 * @description
 * Предоставляет функции для декодирования Base64 в XML строку,
 * парсинга XML в JavaScript объект и извлечения данных для таблицы.
 *
 * @module xmlParser
 */

import { b64DecodeUnicode } from 'utils/utils';

/**
 * Декодирует Base64 строку в XML
 * @param base64String - Base64 закодированная XML строка
 * @returns Декодированная XML строка
 * @throws Error если декодирование не удалось
 */
export const decodeBase64ToXml = (base64String: string): string => {
  try {
    return b64DecodeUnicode(base64String);
  } catch (error) {
    throw new Error(`Ошибка при декодировании Base64: ${error instanceof Error ? error.message : 'Неизвестная ошибка'}`);
  }
};

/**
 * Парсит XML строку в JavaScript объект
 * @param xmlString - XML строка для парсинга
 * @returns Объект, представляющий структуру XML
 * @throws Error если парсинг не удался
 * @description
 * Использует DOMParser для преобразования XML в Document object.
 * Затем рекурсивно обходит дерево и преобразует его в JavaScript объект.
 */
export const parseXmlToObject = (xmlString: string): Document | null => {
  try {
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(xmlString, 'text/xml');

    // Проверка на ошибки парсинга (в некоторых браузерах ошибки не генерируют исключения)
    const parserError = xmlDoc.getElementsByTagName('parsererror');
    if (parserError.length > 0) {
      throw new Error(`Ошибка при парсинге XML: ${parserError[0].textContent}`);
    }

    return xmlDoc;
  } catch (error) {
    throw new Error(`Ошибка при парсинге XML: ${error instanceof Error ? error.message : 'Неизвестная ошибка'}`);
  }
};

/**
 * Извлекает данные водителя из XML документа
 * @param xmlDoc - Парсированный XML документ
 * @returns Объект с данными водителя
 */
export const extractDriverData = (xmlDoc: Document): {
  fullName?: string;
  personnelNumber?: string;
  licenseSeries?: string;
  licenseNumber?: string;
  licenseIssueDate?: string;
} => {
  const sodInfo = xmlDoc.getElementsByTagName('СодИнфСоб')[0];
  if (!sodInfo) {
    return {};
  }

  const driverElem = sodInfo.getElementsByTagName('СвВодит')[0];
  if (!driverElem) {
    return {};
  }

  // Извлечение ФИО из тега ФИО
  const fioElem = driverElem.getElementsByTagName('ФИО')[0];
  const fullName = fioElem
    ? [
      fioElem.getAttribute('Фамилия'),
      fioElem.getAttribute('Имя'),
      fioElem.getAttribute('Отчество'),
    ]
      .filter(Boolean)
      .join(' ')
    : undefined;

  // Извлечение данных водительского удостоверения
  const licenseElem = driverElem.getElementsByTagName('ВодитУд')[0];

  return {
    fullName,
    personnelNumber: driverElem.getAttribute('ИННФЛ') ?? undefined,
    licenseSeries: licenseElem ? licenseElem.getAttribute('СерВУ') ?? undefined : undefined,
    licenseNumber: licenseElem ? licenseElem.getAttribute('НомВУ') ?? undefined : undefined,
    licenseIssueDate: licenseElem ? licenseElem.getAttribute('ДатаВыдВУ') ?? undefined : undefined,
  };
};

/**
 * Извлекает данные организации из XML документа
 * @param xmlDoc - Парсированный XML документ
 * @returns Объект с данными организации
 */
export const extractOrganizationData = (xmlDoc: Document): {
  name?: string;
  ogrn?: string;
  tin?: string;
} => {
  const sodInfo = xmlDoc.getElementsByTagName('СодИнфСоб')[0];
  if (!sodInfo) {
    return {};
  }

  const orgElem = sodInfo.getElementsByTagName('СвЛицПЛ')[0];
  if (!orgElem) {
    return {};
  }

  const orgInfoElem = orgElem.getElementsByTagName('ИдСв')[0];
  if (!orgInfoElem) {
    return {};
  }

  const orgNameElem = orgInfoElem.getElementsByTagName('СвЮЛУч')[0];
  if (!orgNameElem) {
    return {};
  }

  return {
    name: orgNameElem.getAttribute('НаимОрг') ?? undefined,
    ogrn: orgNameElem.getAttribute('ОГРН') ?? undefined,
    tin: orgNameElem.getAttribute('ИННЮЛ') ?? undefined,
  };
};

/**
 * Извлекает данные транспортного средства из XML документа
 * @param xmlDoc - Парсированный XML документ
 * @returns Объект с данными транспортного средства
 */
export const extractTransportData = (xmlDoc: Document): {
  stateNumber?: string;
  vehicleBrand?: string;
  vehicleModel?: string;
  transportType?: string;
} => {
  const sodInfo = xmlDoc.getElementsByTagName('СодИнфСоб')[0];
  if (!sodInfo) {
    return {};
  }

  const vehicleElem = sodInfo.getElementsByTagName('СвТС')[0];
  if (!vehicleElem) {
    return {};
  }

  const tcElem = vehicleElem.getElementsByTagName('ТС')[0];
  if (!tcElem) {
    return {};
  }

  return {
    stateNumber: tcElem.getAttribute('РегНомер') ?? undefined,
    vehicleBrand: tcElem.getAttribute('Марка') ?? undefined,
    vehicleModel: tcElem.getAttribute('Модель') ?? undefined,
    transportType: tcElem.getAttribute('Тип') ?? undefined,
  };
};

/**
 * Извлекает данные путевого листа из XML документа
 * @param xmlDoc - Парсированный XML документ
 * @returns Объект с данными путевого листа
 */
export const extractWaybillData = (xmlDoc: Document): {
  humanReadableId?: string;
  startDate?: string;
  finishDate?: string;
  documentDate?: string;
} => {
  const documentElem = xmlDoc.getElementsByTagName('Документ')[0];
  if (!documentElem) {
    return {};
  }

  const periodElem = documentElem.getElementsByTagName('СрокПЛ')[0];
  const startDate = periodElem ? periodElem.getAttribute('ДатаНачИспПЛ') ?? undefined : undefined;
  const finishDate = periodElem ? periodElem.getAttribute('ДатаКонИспПЛ') ?? undefined : undefined;

  // Fallback: если не получилось взять даты из текущих полей, берем из ДатаПЛ
  const documentDate = documentElem.getAttribute('ДатаПЛ') ?? undefined;

  return {
    humanReadableId: documentElem.getAttribute('НомерПЛ') ?? undefined,
    startDate: startDate || documentDate,
    finishDate: finishDate || documentDate,
    documentDate,
  };
};

/**
 * Извлекает данные для таблицы из Base64 закодированного XML
 * @param base64Xml - Base64 закодированная XML строка
 * @returns Объект с данными для таблицы
 * @description
 * Эта функция объединяет все шаги парсинга:
 * 1. Декодирует Base64 в XML строку
 * 2. Парсит XML в Document объект
 * 3. Извлекает данные из различных элементов XML
 *
 * @example
 * ```typescript
 * const base64Xml = "PD94bWwgdmVyc2lvbj0iMS4wIiA..."; // Base64 encoded XML
 * const data = extractDataFromBase64Xml(base64Xml);
 * console.log(data.organizationName); // "000001Тестовая_организация_01"
 * console.log(data.stateNumber); // "А456АА888"
 * ```
 */
export const extractDataFromBase64Xml = (base64Xml: string): {
  organizationName?: string;
  organizationOgrn?: string;
  organizationTin?: string;
  driverFullName?: string;
  driverPersonnelNumber?: string;
  drivingLicenseSeries?: string;
  drivingLicenseNumber?: string;
  drivingLicenseIssueDate?: string;
  humanReadableId?: string;
  startDate?: string;
  finishDate?: string;
  documentDate?: string;
  stateNumber?: string;
  vehicleBrand?: string;
  vehicleModel?: string;
  transportType?: string;
} => {
  try {
    // Шаг 1: Декодирование Base64 в XML
    const xmlString = decodeBase64ToXml(base64Xml);

    // Шаг 2: Парсинг XML в Document объект
    const xmlDoc = parseXmlToObject(xmlString);
    if (!xmlDoc) {
      throw new Error('Не удалось создать Document объект из XML');
    }

    // Шаг 3: Извлечение данных из XML
    const organizationData = extractOrganizationData(xmlDoc);
    const driverData = extractDriverData(xmlDoc);
    const transportData = extractTransportData(xmlDoc);
    const waybillData = extractWaybillData(xmlDoc);

    return {
      // Данные организации
      organizationName: organizationData.name,
      organizationOgrn: organizationData.ogrn,
      organizationTin: organizationData.tin,

      // Данные водителя
      driverFullName: driverData.fullName,
      driverPersonnelNumber: driverData.personnelNumber,
      drivingLicenseSeries: driverData.licenseSeries,
      drivingLicenseNumber: driverData.licenseNumber,
      drivingLicenseIssueDate: driverData.licenseIssueDate,

      // Данные путевого листа
      humanReadableId: waybillData.humanReadableId,
      startDate: waybillData.startDate,
      finishDate: waybillData.finishDate,
      documentDate: waybillData.documentDate,

      // Данные транспортного средства
      stateNumber: transportData.stateNumber,
      vehicleBrand: transportData.vehicleBrand,
      vehicleModel: transportData.vehicleModel,
      transportType: transportData.transportType,
    };
  } catch {
    return {};
  }
};
