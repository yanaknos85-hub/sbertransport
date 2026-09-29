export const MAINTENANCE_MONITOR_URL = '/:type/monitoring';
export const MAINTENANCE_TAKE_TO_WORK_URL = '/:type/monitoring/:requestId/take_to_work';
export const MAINTENANCE_REQUEST_STATUS_UPDATE_URL = '/:type/monitoring/:requestId';
export const MAINTENANCE_REPAIR_REQUEST_STATUS_UPDATE_URL = '/:type/monitoring/request/:requestId';

export enum MaintenanceTabs {
  Repair = 'repair',
  Maintenance = 'maintenance',
  TireService = 'tire',
  Washing = 'washing',
  Evacuation = 'evacuation',
  Registration = 'registration',
}

interface MaintenanceStatusLabel {
  readonly value: string;
  readonly label: string;
}

export enum WashingStatus {
  EXPECTED_WASH = 'EXPECTED_WASH',
  GOING_WASH = 'GOING_WASH',
  FINISHED = 'FINISHED',
  CANCELLED = 'CANCELLED',
}

export enum RepairStatus {
  REGISTRATION_AT_THE_SERVICE_STATION = 'REGISTRATION_AT_THE_SERVICE_STATION',
  EXPECTED_AT_THE_SERVICE_STATION = 'EXPECTED_AT_THE_SERVICE_STATION',
  CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION = 'CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION',
  DIAGNOSTICS = 'DIAGNOSTICS',
  WORK_ORDER_APPROVAL = 'WORK_ORDER_APPROVAL',
  WORK = 'WORK',
  ISSUING_A_CAR = 'ISSUING_A_CAR',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
  SEARCH_FOR_A_TOW_TRUCK = 'SEARCH_FOR_A_TOW_TRUCK',
  TOW_TRUCK_IS_COMING = 'TOW_TRUCK_IS_COMING',
  CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK = 'CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK',
  REGISTRATION_FOR_FIELD_SERVICE = 'REGISTRATION_FOR_FIELD_SERVICE',
  PERFORMER_ON_THE_WAY = 'PERFORMER_ON_THE_WAY',
  DETERMINATION_OF_THE_LIST_OF_WORKS = 'DETERMINATION_OF_THE_LIST_OF_WORKS',
}

export enum TireStatus {
  SEARCH_TRUCK = 'SEARCH_TRUCK',
  TRUCK_ON_WAY = 'TRUCK_ON_WAY',
  CAR_PASSED_TRUCK = 'CAR_PASSED_TRUCK',
  RECORD_FOR_TIRE = 'RECORD_FOR_TIRE',
  WAITING_FOR_TIRE = 'WAITING_FOR_TIRE',
  ACCEPTING = 'ACCEPTING',
  DEFINING_WORKS = 'DEFINING_WORKS',
  APPROVE_WORKS_COST = 'APPROVE_WORKS_COST',
  CARRY_OUT = 'CARRY_OUT',
  ISSUING_A_CAR = 'ISSUING_A_CAR',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export enum EvacuationStatus {
  SEARCH_FOR_A_TOW_TRUCK = 'SEARCH_FOR_A_TOW_TRUCK',
  TOW_TRUCK_IS_COMING = 'TOW_TRUCK_IS_COMING',
  TOW_TRUCK_FILED = 'TOW_TRUCK_FILED',
  CAR_IS_ON_THE_WAY = 'CAR_IS_ON_THE_WAY',
  CAR_DELIVERED_TO_DESTINATION = 'CAR_DELIVERED_TO_DESTINATION',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export enum MaintenanceStatus {
  SEARCH_TRUCK = 'SEARCH_TRUCK',
  TRUCK_ON_WAY = 'TRUCK_ON_WAY',
  CAR_PASSED_TRUCK = 'CAR_PASSED_TRUCK',
  RECORD_FOR_MAINTENANCE = 'RECORD_FOR_MAINTENANCE',
  WAITING_FOR_MAINTENANCE = 'WAITING_FOR_MAINTENANCE',
  ACCEPTING = 'ACCEPTING',
  DIAGNOSTICS = 'DIAGNOSTICS',
  APPROVE_WORKS_COST = 'APPROVE_WORKS_COST',
  CARRY_OUT = 'CARRY_OUT',
  ISSUING_A_CAR = 'ISSUING_A_CAR',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export enum RegistrationStatus {
  DOCUMENT_TRANSFER = 'DOCUMENT_TRANSFER',
  PAYMENT_OF_GOVERNMENT_FEE = 'PAYMENT_OF_GOVERNMENT_FEE',
  TRAFFIC_POLICE_INSPECTION = 'TRAFFIC_POLICE_INSPECTION',
  ISSUING_A_CAR = 'ISSUING_A_CAR',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED',
}

export const MAINTENANCE_STATUSES: Record<MaintenanceTabs, readonly MaintenanceStatusLabel[]> = {
  [MaintenanceTabs.Washing]: [
    { value: WashingStatus.EXPECTED_WASH, label: 'Вас ожидают на мойке' },
    { value: WashingStatus.GOING_WASH, label: 'Идет мойка' },
    { value: WashingStatus.FINISHED, label: 'Завершено' },
    { value: WashingStatus.CANCELLED, label: 'Отменено' },
  ],
  [MaintenanceTabs.Repair]: [
    { value: RepairStatus.REGISTRATION_AT_THE_SERVICE_STATION, label: 'Запись на СТО' },
    { value: RepairStatus.EXPECTED_AT_THE_SERVICE_STATION, label: 'Вас ожидают на СТО' },
    { value: RepairStatus.CAR_WAS_ACCEPTED_AT_THE_SERVICE_STATION, label: 'Машина принята на СТО' },
    { value: RepairStatus.DIAGNOSTICS, label: 'Диагностика автомобиля' },
    { value: RepairStatus.WORK_ORDER_APPROVAL, label: 'Согласование Заказ-наряда' },
    { value: RepairStatus.WORK, label: 'Ремонтные работы' },
    { value: RepairStatus.ISSUING_A_CAR, label: 'Выдача автомобиля' },
    { value: RepairStatus.FINISHED, label: 'Завершено' },
    { value: RepairStatus.CANCELED, label: 'Отменено' },
    { value: RepairStatus.SEARCH_FOR_A_TOW_TRUCK, label: 'Поиск эвакуатора' },
    { value: RepairStatus.TOW_TRUCK_IS_COMING, label: 'Эвакуатор едет к вам' },
    { value: RepairStatus.CAR_WAS_TRANSFERRED_TO_A_TOW_TRUCK, label: 'Машина передана на эвакуатор' },
    { value: RepairStatus.REGISTRATION_FOR_FIELD_SERVICE, label: 'Запись на Выездной сервис' },
    { value: RepairStatus.PERFORMER_ON_THE_WAY, label: 'Исполнитель в пути' },
    { value: RepairStatus.DETERMINATION_OF_THE_LIST_OF_WORKS, label: 'Определение перечня работ' },
  ],
  [MaintenanceTabs.TireService]: [
    { value: TireStatus.SEARCH_TRUCK, label: 'Поиск эвакуатора' },
    { value: TireStatus.TRUCK_ON_WAY, label: 'Эвакуатор едет к вам' },
    { value: TireStatus.CAR_PASSED_TRUCK, label: 'Машина передана на эвакуатор' },
    { value: TireStatus.RECORD_FOR_TIRE, label: 'Запись на шиномонтаж' },
    { value: TireStatus.WAITING_FOR_TIRE, label: 'Вас ожидают на шиномонтаже' },
    { value: TireStatus.ACCEPTING, label: 'Машина принята на шиномонтаж' },
    { value: TireStatus.DEFINING_WORKS, label: 'Определение перечня работ' },
    { value: TireStatus.APPROVE_WORKS_COST, label: 'Согласование стоимости работ' },
    { value: TireStatus.CARRY_OUT, label: 'Проведение работ' },
    { value: TireStatus.ISSUING_A_CAR, label: 'Выдача автомобиля' },
    { value: TireStatus.FINISHED, label: 'Завершено' },
    { value: TireStatus.CANCELED, label: 'Отменено' },
  ],
  [MaintenanceTabs.Evacuation]: [
    { value: EvacuationStatus.SEARCH_FOR_A_TOW_TRUCK, label: 'Поиск эвакуатора' },
    { value: EvacuationStatus.TOW_TRUCK_IS_COMING, label: 'Эвакуатор едет к вам' },
    { value: EvacuationStatus.TOW_TRUCK_FILED, label: 'Эвакуатор подан' },
    { value: EvacuationStatus.CAR_IS_ON_THE_WAY, label: 'Машина в пути' },
    { value: EvacuationStatus.CAR_DELIVERED_TO_DESTINATION, label: 'Машина доставлена' },
    { value: EvacuationStatus.FINISHED, label: 'Завершено' },
    { value: EvacuationStatus.CANCELED, label: 'Отменено' },
  ],
  [MaintenanceTabs.Maintenance]: [
    { value: MaintenanceStatus.SEARCH_TRUCK, label: 'Поиск эвакуатора' },
    { value: MaintenanceStatus.TRUCK_ON_WAY, label: 'Эвакуатор едет к вам' },
    { value: MaintenanceStatus.CAR_PASSED_TRUCK, label: 'Машина передана на эвакуатор' },
    { value: MaintenanceStatus.RECORD_FOR_MAINTENANCE, label: 'Запись на ТО' },
    { value: MaintenanceStatus.WAITING_FOR_MAINTENANCE, label: 'Вас ожидают на СТО' },
    { value: MaintenanceStatus.ACCEPTING, label: 'Машина принята' },
    { value: MaintenanceStatus.DIAGNOSTICS, label: 'Диагностика автомобиля' },
    { value: MaintenanceStatus.APPROVE_WORKS_COST, label: 'Согласование стоимости работ' },
    { value: MaintenanceStatus.CARRY_OUT, label: 'Проведение работ' },
    { value: MaintenanceStatus.ISSUING_A_CAR, label: 'Выдача автомобиля' },
    { value: MaintenanceStatus.FINISHED, label: 'Завершено' },
    { value: MaintenanceStatus.CANCELED, label: 'Отменено' },
  ],
  [MaintenanceTabs.Registration]: [
    { value: RegistrationStatus.DOCUMENT_TRANSFER, label: 'Передача документов' },
    { value: RegistrationStatus.PAYMENT_OF_GOVERNMENT_FEE, label: 'Оплата гос. пошлины' },
    { value: RegistrationStatus.TRAFFIC_POLICE_INSPECTION, label: 'Осмотр в ГИБДД' },
    { value: RegistrationStatus.ISSUING_A_CAR, label: 'Выдача автомобиля' },
    { value: RegistrationStatus.FINISHED, label: 'Завершено' },
    { value: RegistrationStatus.CANCELED, label: 'Отменено' },
  ],
};
