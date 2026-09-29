// TODO рефакторинг в i18n
export enum TripPurposesHandbookTexts {
  pageHeader = 'pageHeader',
  deleteConfirm = 'deleteConfirm',
  createNew = 'createNew',
  save = 'save',
  cancel = 'cancel',
  remove = 'remove',
  tripPurposes = 'tripPurposes',
  decline = 'decline',

  nameRequired = 'nameRequired',
  name = 'name',
  attributeRequired = 'attributeRequired',
  organizationRequired = 'organizationRequired',
  departureTimeRequired = 'departureTimeRequired',
  departureDateRequired = 'departureDateRequired',
  daysOfWeekRequired = 'daysOfWeekRequired',
  targetType = 'targetType',

  attributeCondition = 'attributeCondition',
  departmentCondition = 'departmentCondition',
  weekdaysCondition = 'weekdaysCondition',
  timeCondition = 'timeCondition',
  datesCondition = 'datesCondition',
  addCondition = 'addCondition',

  condition = 'condition',
  employeeAttribute = 'employeeAttribute',
  daysOfWeek = 'daysOfWeek',
  departureDate = 'departureDate',
  departureTime = 'departureTime',
  organization = 'organization',
}

export const TripPurposesHandbookTextsCyrillic = {
  [TripPurposesHandbookTexts.pageHeader]: 'К списку целей поездки',
  [TripPurposesHandbookTexts.deleteConfirm]: 'Вы уверены что хотите удалить эту цель?',
  [TripPurposesHandbookTexts.createNew]: 'Добавить цель',
  [TripPurposesHandbookTexts.save]: 'Сохранить',
  [TripPurposesHandbookTexts.remove]: 'Удалить',
  [TripPurposesHandbookTexts.cancel]: 'Отменить',
  [TripPurposesHandbookTexts.tripPurposes]: 'Справочник "Цели Поездки"',
  [TripPurposesHandbookTexts.decline]: 'Нет',

  [TripPurposesHandbookTexts.nameRequired]: 'Введите наименование цели поездки',
  [TripPurposesHandbookTexts.name]: 'Наименование',
  [TripPurposesHandbookTexts.attributeRequired]: 'Укажите признак сотрудника',
  [TripPurposesHandbookTexts.organizationRequired]: 'Укажите организацию',
  [TripPurposesHandbookTexts.departureTimeRequired]: 'Укажите время отправления',
  [TripPurposesHandbookTexts.departureDateRequired]: 'Укажите дату отправления',
  [TripPurposesHandbookTexts.daysOfWeekRequired]: 'Укажите дни отправления',
  [TripPurposesHandbookTexts.targetType]: 'Тип цели',

  [TripPurposesHandbookTexts.attributeCondition]: 'По признаку',
  [TripPurposesHandbookTexts.departmentCondition]: 'По Орг. структуре',
  [TripPurposesHandbookTexts.weekdaysCondition]: 'По дням недели',
  [TripPurposesHandbookTexts.timeCondition]: 'По времени отправления',
  [TripPurposesHandbookTexts.datesCondition]: 'По дате отправления',
  [TripPurposesHandbookTexts.addCondition]: 'Добавить условие',

  [TripPurposesHandbookTexts.condition]: 'Условие',
  [TripPurposesHandbookTexts.employeeAttribute]: 'Признак сотрудника',
  [TripPurposesHandbookTexts.daysOfWeek]: 'Дни недели',
  [TripPurposesHandbookTexts.departureDate]: 'Дата отправления',
  [TripPurposesHandbookTexts.departureTime]: 'Время отправления',
  [TripPurposesHandbookTexts.organization]: 'Орг. структура',
};

export const weekdaysNames = {
  SUNDAY: 'SUNDAY',
  MONDAY: 'MONDAY',
  TUESDAY: 'TUESDAY',
  WEDNESDAY: 'WEDNESDAY',
  THURSDAY: 'THURSDAY',
  FRIDAY: 'FRIDAY',
  SATURDAY: 'SATURDAY',
} as const;

export const weekdaysNamesToLabel = {
  [weekdaysNames.MONDAY]: 'Понедельник',
  [weekdaysNames.TUESDAY]: 'Вторник',
  [weekdaysNames.WEDNESDAY]: 'Среда',
  [weekdaysNames.THURSDAY]: 'Четверг',
  [weekdaysNames.FRIDAY]: 'Пятница',
  [weekdaysNames.SATURDAY]: 'Суббота',
  [weekdaysNames.SUNDAY]: 'Воскресенье',
};

export const tripPurposeJsonKeys = {
  label: 'label',
  tripPurposeAttributes: 'tripPurposeAttributes',
  tripPurposeDates: 'tripPurposeDates',
  tripPurposeDepartments: 'tripPurposeDepartments',
  tripPurposeTimes: 'tripPurposeTimes',
  tripPurposeWeekdays: 'tripPurposeWeekdays',
};

export const weekdaysSelectOptions = [
  { value: 'MONDAY', label: 'Понедельник' },
  { value: 'TUESDAY', label: 'Вторник' },
  { value: 'WEDNESDAY', label: 'Среда' },
  { value: 'THURSDAY', label: 'Четверг' },
  { value: 'FRIDAY', label: 'Пятница' },
  { value: 'SATURDAY', label: 'Суббота' },
  { value: 'SUNDAY', label: 'Воскресенье' },
];

export enum ConditionType {
  attribute = 'attribute',
  daysOfWeek = 'daysOfWeek',
  departureDate = 'departureDate',
  departureTime = 'departureTime',
  organization = 'organization',
}

export const conditionSelectOptions = [
  { value: ConditionType.attribute, label: 'По признаку' },
  { value: ConditionType.departureDate, label: 'По дате отправления' },
  { value: ConditionType.daysOfWeek, label: 'По дням недели' },
  { value: ConditionType.departureTime, label: 'По времени отправления' },
  { value: ConditionType.organization, label: 'По Орг. структуре' },
];
