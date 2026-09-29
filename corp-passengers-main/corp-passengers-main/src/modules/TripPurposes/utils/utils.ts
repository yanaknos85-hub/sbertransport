import moment from 'moment/moment';
import { TripPurposeJournal, TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';
import { DATE_FORMAT } from 'constants/constants.app';
import { FormInstance } from 'antd/es/form';
import {
  Condition, SubmitTripPurposeData, TripPurposeDates, TripPurposeTimes
} from '../types/types';
import {
  ConditionType,
  TripPurposesHandbookTexts,
  TripPurposesHandbookTextsCyrillic,
  weekdaysNamesToLabel
} from '../constants/TripPurposes.constants';

/**
 * Пересобирает объект цели поездки в пригодный для отображения в форме редактирования
 * @param purpose - объект цели поездки
 * @return processedArray - пересобранный объект для итерации при заполнении полей карточки
 */
export const parseInitialConditions = ({
  tripPurposeAttributes,
  tripPurposeDepartments,
  tripPurposeWeekdays,
  tripPurposeDates,
  tripPurposeTimes,
}: TripPurpose): Condition[] => {
  const weekdaysNames: string[] = tripPurposeWeekdays.map(({ weekday }) => weekday);
  const now = moment();
  const [hour, minute, second] = [now.hour(), now.minute(), now.second()];

  return [
    ...tripPurposeAttributes.map(attr => ({
      conditionSelectOption: ConditionType.attribute,
      attributeOption: attr.attribute.id,
    })),

    ...tripPurposeDepartments.map(dep => ({
      conditionSelectOption: ConditionType.organization,
      organizationOption: dep.department.id,
      organizationOptionName: dep.department.departmentName,
    })),

    tripPurposeWeekdays.map(_ => ({
      conditionSelectOption: ConditionType.daysOfWeek,
      departureWeekdays: weekdaysNames,
    })),

    ...tripPurposeDates.map(date => ({
      conditionSelectOption: ConditionType.departureDate,
      departureDates: [
        moment(date.startDate).set({
          hour, minute, second,
        }),
        moment(date.endDate).set({
          hour, minute, second,
        }),
      ],
    })),

    ...tripPurposeTimes.map(time => ({
      conditionSelectOption: ConditionType.departureTime,
      departureTimes: [moment(time.startTime), moment(time.endTime)],
    })),
  ]
    .filter(condition => (Array.isArray(condition) ? condition.length !== 0 : condition))
    .map((condition, index) =>
      // tripPurposeWeekdays is array when more than one weekday specified
      // All weekdays should be put in one Condition
      // eslint-disable-next-line @stylistic/implicit-arrow-linebreak
      ({
        indexNo: index + 1,
        ...(Array.isArray(condition) ? condition[0] : condition),
      })
    );
};

/**
 * Обрабатывает TripPurposeJson[] целей поездок в TripPurposeJournal[] для отображения в журнале
 * @param data - массив json-ов целей поездок
 */
export const processIncomingJsonArray = (data: TripPurpose[]): TripPurposeJournal[] => {
  const tripPurposes: TripPurposeJournal[] = [];
  data.forEach(el => {
    let attributesText = '';
    let datesText = '';
    let depsText = '';
    let timesText = '';
    let daysText = '';
    const conditionTexts: string[] = [];

    // Если есть параметры условий, инициализируем текст условия
    if (el.tripPurposeAttributes.length !== 0) {
      attributesText = `${TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.attributeCondition]}:\n`;
    }
    if (el.tripPurposeDepartments.length !== 0) {
      depsText = `${TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.departmentCondition]}:\n`;
    }
    if (el.tripPurposeDates.length !== 0) {
      datesText = `${TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.datesCondition]}:\n`;
    }
    if (el.tripPurposeTimes.length !== 0) {
      timesText = `${TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.timeCondition]}:\n`;
    }
    if (el.tripPurposeWeekdays.length !== 0) {
      daysText = `${TripPurposesHandbookTextsCyrillic[TripPurposesHandbookTexts.weekdaysCondition]}:\n`;
    }

    // Заполняем параметры для условий, если они существуют
    // По признаку сотрудника
    el.tripPurposeAttributes.forEach(attr => {
      if (attributesText.length !== 0) {
        attributesText = attributesText.concat(`${attr.attribute.name}; `);
      }
    });
    if (attributesText.length !== 0) {
      conditionTexts.push(`${attributesText}\n`);
    }
    // По Орг. структуре
    el.tripPurposeDepartments.forEach(dep => {
      if (depsText.length !== 0) {
        depsText = depsText.concat(`${dep.department.departmentName}; `);
      }
    });
    if (depsText.length !== 0) {
      conditionTexts.push(`${depsText}\n`);
    }
    // По дням недели
    el.tripPurposeWeekdays.forEach(day => {
      if (daysText.length !== 0) {
        // @ts-ignore  todo типизировать
        daysText = daysText.concat(`${weekdaysNamesToLabel[day.weekday]}; `);
      }
    });
    if (daysText.length !== 0) {
      conditionTexts.push(`${daysText}\n`);
    }
    // По дате отправления
    el.tripPurposeDates.forEach(date => {
      if (datesText.length !== 0) {
        datesText = datesText.concat(
          `с ${new Date(date.startDate).toLocaleDateString()} по ${new Date(date.endDate).toLocaleDateString()}; `
        );
      }
    });
    if (datesText.length !== 0) {
      conditionTexts.push(`${datesText}\n`);
    }
    // По времени отправления
    el.tripPurposeTimes.forEach(time => {
      if (timesText.length !== 0) {
        timesText = timesText.concat(
          `c ${moment(time.startTime).format(DATE_FORMAT.TIME_SHORT)} до ${moment(time.endTime).format(
            DATE_FORMAT.TIME_SHORT
          )}; `
        );
      }
    });
    if (timesText.length !== 0) {
      conditionTexts.push(`${timesText}\n`);
    }

    const condition = conditionTexts.length ? conditionTexts.reduce((text, res) => res + text) : '';
    tripPurposes.push({
      id: el.id, label: el.label, condition,
    });
  });
  return tripPurposes;
};

/**
 * Собирает пригодный для отправки на сервер объект цели поездки
 * @param data
 * @param id
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const processFieldsForSubmitting = (data: any, id?: string): SubmitTripPurposeData => {
  let label = '';
  let purposeType = '';

  const tripPurposeTimes: TripPurposeTimes[] = [];
  const tripPurposeDates: TripPurposeDates[] = [];

  const tripPurposeAttributesSet = new Set<string>([]);
  const tripPurposeDepartmentsSet = new Set<string>([]);
  const tripPurposeWeekdaysSet = new Set<string>([]);

  Object.keys(data).forEach(key => {
    if (key === 'name') {
      label = data[key];
    }
    if (key === 'purposeType') {
      purposeType = data[key];
    }
    if (key.startsWith(ConditionType.attribute)) {
      tripPurposeAttributesSet.add(data[key]);
    }
    if (key.startsWith(ConditionType.organization)) {
      tripPurposeDepartmentsSet.add(data[key]);
    }
    if (key.startsWith(ConditionType.departureDate)) {
      const dates = data[key];
      tripPurposeDates.push({
        startDate: moment(dates[0]),
        endDate: moment(dates[1]),
      });
    }
    if (key.startsWith(ConditionType.departureTime)) {
      const time = data[key];
      tripPurposeTimes.push({
        startTime: moment(time[0]),
        endTime: moment(time[1]),
      });
    }
    if (key.includes(ConditionType.daysOfWeek)) {
      const days: string[] = Object.values(data[key]);
      days.forEach(day => tripPurposeWeekdaysSet.add(day));
    }
  });

  const tripPurposeAttributes = Array.from(tripPurposeAttributesSet).map(val => ({ id: val }));
  const tripPurposeDepartments = Array.from(tripPurposeDepartmentsSet).map(val => ({ id: val }));
  const tripPurposeWeekdays = Array.from(tripPurposeWeekdaysSet).map(val => ({ weekday: val }));

  const processedJson: SubmitTripPurposeData = { label, purposeType };
  if (id) {
    processedJson.id = id;
  }
  if (tripPurposeAttributes.length !== 0) {
    processedJson.tripPurposeAttributes = tripPurposeAttributes;
  }
  if (tripPurposeDepartments.length !== 0) {
    processedJson.tripPurposeDepartments = tripPurposeDepartments;
  }
  if (tripPurposeWeekdays.length !== 0) {
    processedJson.tripPurposeWeekdays = tripPurposeWeekdays;
  }
  if (tripPurposeDates.length !== 0) {
    processedJson.tripPurposeDates = tripPurposeDates;
  }
  if (tripPurposeTimes.length !== 0) {
    processedJson.tripPurposeTimes = tripPurposeTimes;
  }

  return processedJson;
};

export const fillFormOnUpdate = (
  form: FormInstance,
  initialName: string,
  conditions: Condition[],
  initialType: string
): void => {
  form.setFieldsValue({ name: initialName, purposeType: initialType });

  conditions.forEach(condition => {
    const attribute = `attribute-${condition.indexNo}`;
    const organization = `organization-${condition.indexNo}`;
    const departureTime = `departureTime-${condition.indexNo}`;
    const daysOfWeek = `daysOfWeek-${condition.indexNo}`;
    const departureDate = `departureDate-${condition.indexNo}`;
    const conditionOption = `condition-${condition.indexNo}`;
    form.setFieldsValue({
      [attribute]: condition.attributeOption,
      [organization]: condition.organizationOption,
      [departureTime]: condition.departureTimes,
      [daysOfWeek]: condition.departureWeekdays,
      [departureDate]: condition.departureDates,
      [conditionOption]: condition.conditionSelectOption,
    });
  });
};

export const getInitialConditionArray = (): Condition[] => [
  {
    indexNo: 1,
    attributeOption: '',
    conditionSelectOption: undefined,
    departureDates: [],
    departureTimes: [],
    departureWeekdays: [],
    organizationOption: '',
  },
];

export const addCondition = (conditions: Condition[]): Condition[] => conditions.length
  ? conditions.concat([
    {
      indexNo: conditions.length ? conditions[conditions.length - 1].indexNo + 1 : 1,
      attributeOption: '',
      conditionSelectOption: undefined,
      departureDates: [],
      departureTimes: [],
      departureWeekdays: [],
      organizationOption: '',
    },
  ])
  : getInitialConditionArray();

export const getFormRule = (required: boolean, message?: string) => [{ required, message: message ?? '' }];

export const getInitialOrganizationOptions = (condition: Condition) => {
  if (condition.organizationOptionName) {
    return [{ label: condition.organizationOptionName, value: condition.organizationOption! }];
  }

  return [];
};
