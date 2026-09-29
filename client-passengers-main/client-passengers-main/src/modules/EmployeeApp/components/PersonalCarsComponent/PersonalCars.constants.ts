/* eslint-disable @typescript-eslint/no-explicit-any */
export enum PersonalCars {
  id = 'id',
  transportType = 'transportType',
  brandName = 'brandName',
  model = 'model',
  color = 'color',
  engineVolume = 'engineVolume',
  insuranceNumber = 'insuranceNumber',
  registrationCertificate = 'registrationCertificate',
  registrationNumber = 'registrationNumber',
  selectTransportType = 'selectTransportType',
  passengerSeatsCount = 'passengerSeatsCount',
  hint = 'hint',
  ownerInfo = 'ownerInfo',
  persDataAccept = 'persDataAccept',
  thirdPartyHint = 'thirdPartyHint',
}

export const PersonalCarsCyrillic = {
  [PersonalCars.brandName]: 'Марка',
  [PersonalCars.transportType]: 'Вид транспорта',
  [PersonalCars.model]: 'Модель',
  [PersonalCars.color]: 'Цвет',
  [PersonalCars.engineVolume]: 'Объем двигателя в см.куб',
  [PersonalCars.insuranceNumber]: 'Серия и номер страхового полиса',
  [PersonalCars.registrationCertificate]: 'Свидетельство о регистрации',
  [PersonalCars.registrationNumber]: 'Регистрационный номер',
  [PersonalCars.passengerSeatsCount]: 'Количество пассажирских мест',
  [PersonalCars.selectTransportType]: 'Выберите вид транспорта',
  [PersonalCars.hint]:
    'В случае не предоставления недостовернных данных из компесанционной выплаты удерживается НДФЛ страховые взносы а также возникает ответственность в соответствии со ст. 159.2 УК РФ',
  [PersonalCars.ownerInfo]: 'Сведения о собственнике',
  [PersonalCars.thirdPartyHint]: '',
};

export const PersonalCarsCyrillicShort = {
  [PersonalCars.brandName]: 'Марка',
  [PersonalCars.transportType]: 'Вид транспорта',
  [PersonalCars.model]: 'Модель',
  [PersonalCars.color]: 'Цвет',
  [PersonalCars.engineVolume]: 'Об. двигателя, см.куб',
  [PersonalCars.insuranceNumber]: '№ страховки',
  [PersonalCars.registrationCertificate]: 'Рег. сертификат',
  [PersonalCars.registrationNumber]: 'Рег. номер',
  [PersonalCars.passengerSeatsCount]: 'Количество пассажирских мест',
  [PersonalCars.ownerInfo]: 'О собственнике',
};

export enum PersonalCarsTexts {
  pageHeader = 'pageHeader',
  deleteConfirm = 'deleteConfirm',
  addNew = 'addNew',
  save = 'save',
  cancel = 'cancel',
  remove = 'remove',
}

export const PersonalCarsTextsCyrillic = {
  [PersonalCarsTexts.pageHeader]: 'Карточка транcпорта',
  [PersonalCarsTexts.deleteConfirm]: 'Вы уверены что хотите удалить эту запись?',
  [PersonalCarsTexts.addNew]: 'Добавить новый',
  [PersonalCarsTexts.save]: 'Сохранить',
  [PersonalCarsTexts.remove]: 'Удалить',
  [PersonalCarsTexts.cancel]: 'Отменить',
};

export enum PersonalOwnerInformation {
  USER = 'USER',
  SPOUSE = 'SPOUSE',
  THIRD_PARTY = 'THIRD_PARTY',
}

export const PersonalOwnerInformationCyrillic = {
  [PersonalOwnerInformation.USER]: 'в собственности пользователя',
  [PersonalOwnerInformation.SPOUSE]: 'в собственности супруга/супруги пользователя',
  [PersonalOwnerInformation.THIRD_PARTY]: 'в собственности третьих лиц',
};

export enum OsagoUploadResponseParams {
  vehicleMark = 'vehicleInfo.vehicleMark' as any,
  vehicleModel = 'vehicleInfo.vehicleModel' as any,
  ownerLastName = 'owner.personalData.lastName' as any,
  ownerFirstName = 'owner.personalData.firstName' as any,
  ownerMiddleName = 'owner.personalData.middleName' as any,
  endDate = 'contractConditions.endDate' as any,
  insurerLastName = 'insurer.personalData.lastName' as any,
  insurerFirstName = 'insurer.personalData.firstName' as any,
  vehicleInfoNumber = 'vehicleInfo.vehicleDocuments.number' as any,
  contractSeries = 'contractSeries' as any,
  contractNumber = 'contractNumber' as any,
  vehicleInfoSeries = 'vehicleInfo.vehicleDocuments.series' as any,
  plateNumber = 'vehicleInfo.plateNumber' as any,
  type = 'vehicleInfo.vehicleDocuments.type' as any,
  vinNumber = 'vehicleInfo.vinNumber' as any,
  driversLastName = 'drivers.personalData.lastName' as any,
  driversFirstName = 'drivers.personalData.firstName' as any,
  driversMiddleName = 'drivers.personalData.middleName' as any,
  driversSeries = 'drivers.driverLicenses.series' as any,
  driversNumber = 'drivers.driverLicenses.number' as any,
}
