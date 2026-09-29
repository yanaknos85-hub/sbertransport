import { useMemo } from 'react';
import { TMassCreateFirstTitleItem } from 'api/shifts/shifts.types';
import { Shift } from 'api/shifts/shifts.types';
import {
  Communication, CommunicationInfo, Transportation, TransportationInfo
} from 'modules/Waybill/Waybill.constants';
import { extractDataFromBase64Xml } from '../utils/xmlParser';

interface UseDataSourceParams {
  firstTitleResult: TMassCreateFirstTitleItem[] | undefined;
  selectedShifts: Shift[];
}

interface SignDataSourceItem {
  key: string;
  humanReadableId: string;
  startDate?: string;
  driverFullName?: string;
  driverPersonnelNumber?: string;
  drivingLicenseSeries?: string;
  drivingLicenseNumber?: string;
  drivingLicenseIssueDate?: string;
  finishDate?: string;
  transportationType?: string;
  communicationType?: string;
  organizationName?: string;
  organizationOgrn?: string;
  organizationTin?: string;
  stateNumber?: string;
  vehicleBrand?: string;
  vehicleModel?: string;
  transportType?: string;
  documentDate?: string;
}

/**
 * Хук для формирования данных источника для таблицы путевых листов
 * @description
 * Обрабатывает результат создания первого титула, извлекает данные из Base64 закодированного XML
 * и объединяет их с данными смен для формирования итогового массива данных таблицы.
 *
 * @param params - Параметры хука
 * @param params.firstTitleResult - Результат создания первого титула (массив объектов с content в Base64)
 * @param params.selectedShifts - Выбранные смены для отображения
 * @returns Объект с массивом данных для таблицы
 *
 * @example
 * ```typescript
 * const { dataSource } = useDataSource({
 *   firstTitleResult,
 *   selectedShifts
 * });
 * ```
 */
export const useDataSource = ({ firstTitleResult, selectedShifts }: UseDataSourceParams) => {
  const dataSource = useMemo(() => {
    if (!firstTitleResult) return [];

    return firstTitleResult.map(item => {
      const shift = selectedShifts.find(s => s.id === item.shiftId);

      // Извлечение данных из XML, если есть content
      let xmlData = {};
      if (item.content) {
        try {
          xmlData = extractDataFromBase64Xml(item.content);
        } catch {
          // Если произошла ошибка при парсинге XML, возвращаем пустой объект
        }
      }

      return {
        key: item.shiftId,
        humanReadableId: item.humanReadableId,
        startDate: (xmlData as { startDate?: string }).startDate || shift?.startDate,
        driverFullName: (xmlData as { driverFullName?: string }).driverFullName || shift?.driverName,
        driverPersonnelNumber: (xmlData as { driverPersonnelNumber?: string }).driverPersonnelNumber,
        drivingLicenseSeries: (xmlData as { drivingLicenseSeries?: string }).drivingLicenseSeries,
        drivingLicenseNumber: (xmlData as { drivingLicenseNumber?: string }).drivingLicenseNumber,
        drivingLicenseIssueDate: (xmlData as { drivingLicenseIssueDate?: string }).drivingLicenseIssueDate,
        finishDate: (xmlData as { finishDate?: string }).finishDate || shift?.endDate,
        documentDate: (xmlData as { documentDate?: string }).documentDate,
        transportationType: TransportationInfo[Transportation.OwnNeeds],
        communicationType: CommunicationInfo[Communication.Urban],
        organizationName: (xmlData as { organizationName?: string }).organizationName,
        organizationOgrn: (xmlData as { organizationOgrn?: string }).organizationOgrn,
        organizationTin: (xmlData as { organizationTin?: string }).organizationTin,
        stateNumber: (xmlData as { stateNumber?: string }).stateNumber || shift?.vehicleStateNumber,
        vehicleBrand: (xmlData as { vehicleBrand?: string }).vehicleBrand || shift?.vehicleBrand,
        vehicleModel: (xmlData as { vehicleModel?: string }).vehicleModel || shift?.vehicleModel,
        transportType: (xmlData as { transportType?: string }).transportType,
      };
    });
  }, [firstTitleResult, selectedShifts]);

  return { dataSource: dataSource as SignDataSourceItem[] };
};
