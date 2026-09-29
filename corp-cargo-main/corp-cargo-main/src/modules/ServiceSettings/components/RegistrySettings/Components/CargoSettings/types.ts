export enum CargoSettingsEnum {
  CARGO_PACKAGE = 'cargoPackage',
  CARGO_TYPES = 'cargoTypes',
  CARGO_DELIVERY_TIME = 'cargoDeliveryTime',
  CARGO_AUTO_SETTINGS = 'cargoAutoSettings',
  CARGO_EXECUTORS_GROUPS_SETTINGS = 'cargoExecutorsGroupsSettings',
  CARGO_VSP_SETTINGS = 'cargoVSPSettings',
}

export const CargoSettingsTitles = {
  [CargoSettingsEnum.CARGO_PACKAGE]: 'Упаковочные материалы',
  [CargoSettingsEnum.CARGO_TYPES]: 'Виды грузов',
  [CargoSettingsEnum.CARGO_DELIVERY_TIME]: 'Сроки доставки',
  [CargoSettingsEnum.CARGO_AUTO_SETTINGS]: 'Автомобили',
  [CargoSettingsEnum.CARGO_EXECUTORS_GROUPS_SETTINGS]: 'Группы исполнителей',
  [CargoSettingsEnum.CARGO_VSP_SETTINGS]: 'Объекты организации (cправочник ВСП)',
}
