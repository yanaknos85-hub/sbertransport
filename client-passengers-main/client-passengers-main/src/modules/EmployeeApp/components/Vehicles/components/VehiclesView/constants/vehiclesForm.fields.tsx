/* eslint-disable camelcase, @typescript-eslint/no-explicit-any */
import React from 'react';

import { DATE_FORMAT } from 'constants/constants.app';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import { Props as UploadProps } from 'shared/form/Upload/Dragger/Dragger';

import { Ownership, OwnershipLabels } from '../../../constants/vehicles.constants';

import styles from '../vehiclesView.module.scss';

const rules = { ...ValidationRules.general };
rules.required.message = 'Обязательное поле';

const dateParams = {
  suffixIcon: null,
  placeholder: 'дд.мм.гггг',
  format: DATE_FORMAT.BASE_REVERTED_DOTS,
};

const uploadProps: UploadProps = {
  listType: 'picture',
  disabled: false,
  accept: '.pdf, .jpeg, .jpg, .png,',
};

export enum FieldsEnum {
  // Собственность
  ownership = 'ownership',

  // Как основной
  asMain = 'asMain',

  // Транспортное средство
  vcRegistrationNumber = 'registrationNumber',
  vcColor = 'color',
  vcPassengerSeatsCount = 'passengerSeatsCount',

  // Свидетельство о браке
  docs_mc_series = 'documents_marriageCertificate_serias',
  docs_mc_number = 'documents_marriageCertificate_number',
  docs_mc_dateOfIssue = 'documents_marriageCertificate_issueDateDocument',

  // Водительское удостоверение
  docs_dl_fio = 'documents_driverLic_fio',
  docs_dl_dateOfIssue = 'documents_driverLic_dateOfIssue',
  docs_dl_validUntil = 'documents_driverLic_validUntil',
  docs_dl_series = 'documents_driverLic_series',
  docs_dl_number = 'documents_driverLic_number',
  docs_dl_issuedBy = 'documents_driverLic_issuedBy',
  docs_dl_whereIssued = 'documents_driverLic_whereIssued',
  docs_dl_category = 'documents_driverLic_category',
  docs_dl_file = 'documents_driverLic_file',

  // ПТС
  docs_pts_vin = 'documents_passportTs_vin',
  docs_pts_brandName = 'documents_passportTs_brandName',
  docs_pts_model = 'documents_passportTs_model',
  docs_pts_engineVolume = 'documents_passportTs_engineVolume',
  docs_pts_enginePower = 'documents_passportTs_enginePower',
  docs_pts_file = 'documents_passportTs_file',

  // ОСАГО
  docs_osago_series = 'documents_osago_series',
  docs_osago_number = 'documents_osago_number',
  docs_osago_startTime = 'documents_osago_startTime',
  docs_osago_endTime = 'documents_osago_finalTime',
  docs_osago_file = 'documents_osago_file',

  // ПДН
  docs_pdn_file = 'documents_agreementPdn_file',

  // Подтверждение
  confirmaDataAccuracy = 'confirmaDataAccuracy',
  agreementPersonalData = 'agreementPersonalData',
}

export const FileLabel = (
  <>
    <span className={styles.fileLabel}>Выберите файлы</span>
    {' '}
    или перетяните их сюда
  </>
);

const RegistrationNumberPattern = new RegExp(/^(?![Ъъ])[А-Яа-я][0-9]{3}((?![Ъъ])[А-Яа-я]){2}[0-9]{2,3}$|^((?![Ъъ])[А-Яа-я]){3}[0-9]{4}$|^((?![Ъъ])[А-Яа-я]){2}[0-9]{4,6}$|^((?![Ъъ])[А-Яа-я]){2}[0-9]{3}(?![Ъъ])[А-Яа-я][0-9]{2}$|^(?![Ъъ])[А-Яа-я][0-9]{4}((?![Ъъ])[А-Яа-я]){2}$|^(?![Ъъ])[А-Яа-я][0-9]{6}$|^[0-9]{4}((?![Ъъ])[А-Яа-я]){3}$|^[0-9]{4}((?![Ъъ])[А-Яа-я]){2}[0-9]{2}$/);
const mcSeriesPattern = new RegExp(/^[A-Z]{1,3}-((?![ЬЪЁ])[А-Я]){2}$/);
const mcNumberPattern = new RegExp(/^[0-9]{6}$/);
const dlSeriesPattern = new RegExp(/^[0-9]{2}[А-ЯABEKMHOPCTYX03]{2}$|^[0-9]{4}$/);
const dlNumberPattern = new RegExp(/^[0-9]{6}$/);
const dlIssuedByPattern = new RegExp(/^[А-ЯЁ0-9№ ]{0,20}$/);
const dlWhereIssuedPattern = new RegExp(/^[А-ЯЁ ]{0,50}$/);
const dlCategoryPattern = new RegExp(/^[A-Z0-9, ]{0,20}$/);
const ptsVinPattern = new RegExp(/^((?![IOQioq])[0-9A-Za-z-]){14,17}$|^(Отсутствует|отсутствует|ОТСУТСТВУЕТ)$/);
const ptsBrandNamePattern = new RegExp(/^[a-zA-Zа-яА-Я0-9\-/().«»"_ ]{2,50}$/);
const ptsModelPattern = new RegExp(/^[a-zA-Zа-яА-Я0-9&*.,+\-?"@'!/() ]{1,50}$/);
const ptsEngineVolumePattern = new RegExp(/^[0-9]{3,5}$/);
const ptsEnginePowerPattern = new RegExp(/^[0-9,]{1,8}$/);
const osagoSeriesPattern = new RegExp(/^((?![ЬЪЁ])[А-Я]){3}$/);
const osagoNumberPattern = new RegExp(/^[0-9]{10}$/);
const carColorPattern = new RegExp(/^[а-яА-Я\s\-./]{1,50}$/);

const labels = {
  // Собственность
  [FieldsEnum.ownership]: '',

  // Как основной
  [FieldsEnum.asMain]: 'Использовать автомобиль как основной',

  // Транспортное средство
  [FieldsEnum.vcRegistrationNumber]: 'Регистрационный знак',
  [FieldsEnum.vcColor]: 'Цвет',
  [FieldsEnum.vcPassengerSeatsCount]: 'Количество пассажирских мест',

  // Свидетельство о браке
  [FieldsEnum.docs_mc_series]: 'Серия',
  [FieldsEnum.docs_mc_number]: 'Номер',
  [FieldsEnum.docs_mc_dateOfIssue]: 'Дата выдачи',

  // Водительское удостоверение
  [FieldsEnum.docs_dl_fio]: 'ФИО',
  [FieldsEnum.docs_dl_dateOfIssue]: 'Дата выдачи',
  [FieldsEnum.docs_dl_validUntil]: 'Действуют до',
  [FieldsEnum.docs_dl_series]: 'Серия',
  [FieldsEnum.docs_dl_number]: 'Номер',
  [FieldsEnum.docs_dl_issuedBy]: 'Кем выдано',
  [FieldsEnum.docs_dl_whereIssued]: 'Где выдано',
  [FieldsEnum.docs_dl_category]: 'Категория',
  [FieldsEnum.docs_dl_file]: FileLabel,

  // ПТС
  [FieldsEnum.docs_pts_vin]: 'Идентификационный номер (VIN)',
  [FieldsEnum.docs_pts_brandName]: 'Марка',
  [FieldsEnum.docs_pts_model]: 'Модель',
  [FieldsEnum.docs_pts_engineVolume]: (
    <>
      Объем двигателя, см
      <sup>3</sup>
    </>
  ),
  [FieldsEnum.docs_pts_enginePower]: 'Мощность двигателя',
  [FieldsEnum.docs_pts_file]: FileLabel,

  // ОСАГО
  [FieldsEnum.docs_osago_series]: 'Серия',
  [FieldsEnum.docs_osago_number]: 'Номер',
  [FieldsEnum.docs_osago_startTime]: 'Начало действия',
  [FieldsEnum.docs_osago_endTime]: 'Конец действия',
  [FieldsEnum.docs_osago_file]: FileLabel,

  // ПДН
  [FieldsEnum.docs_pdn_file]: FileLabel,

  // Подтверждение
  [FieldsEnum.confirmaDataAccuracy]: 'Я подтверждаю достоверность введенных сведений об автомобиле и собственнике',
  [FieldsEnum.agreementPersonalData]: 'Я согласен на обработку указанных персональных данных',
};

export const baseFields: Record<FieldsEnum, { type?: FieldType; rules?: any[]; params?: any }> = {
  // Собственность
  [FieldsEnum.ownership]: {
    type: FieldType.radio,
    rules: [rules.required],
    params: {
      options: [
        { label: OwnershipLabels[Ownership.USER], value: Ownership.USER },
        { label: OwnershipLabels[Ownership.SPOUSE], value: Ownership.SPOUSE },
        {
          label: (
            <>
              {OwnershipLabels[Ownership.THIRD_PARTY]}
              <br />
              <span className={styles.ownershipNote}>компенсация облагается НДФЛ и страховыми взносами</span>
            </>
          ),
          value: Ownership.THIRD_PARTY,
        },
      ],
    },
  },

  // Как основной
  [FieldsEnum.asMain]: {
    type: FieldType.checkbox,
  },

  // Транспортное средство
  [FieldsEnum.vcRegistrationNumber]: {
    rules: [rules.required, {
      pattern: RegistrationNumberPattern,
      whitespace: true,
      message: 'Образец заполнения С111ТН077',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.vcColor]: {
    rules: [rules.required, rules.minLengthString, rules.checkingSpacesStartAndEndLine, {
      pattern: carColorPattern,
      message: 'Образец заполнения Серебряный',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.vcPassengerSeatsCount]: {
    type: FieldType.number,
    rules: [rules.required, rules.digits, rules.maxPassengerCount(5)],
    params: {
      min: 0,
    },
  },

  // Свидетельство о браке
  [FieldsEnum.docs_mc_series]: {
    rules: [rules.required, {
      pattern: mcSeriesPattern,
      message: 'Образец заполнения I-ПК',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_mc_number]: {
    rules: [rules.required, {
      pattern: mcNumberPattern,
      message: 'Образец заполнения 777888',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_mc_dateOfIssue]: {
    type: FieldType.date,
    params: dateParams,
    rules: [rules.required, rules.isValidMarriageCertDate()],
  },

  // Водительское удостоверение
  [FieldsEnum.docs_dl_fio]: {
    rules: [rules.required],
    params: {
      disabled: true,
    },
  },
  [FieldsEnum.docs_dl_dateOfIssue]: {
    type: FieldType.date,
    params: dateParams,
    rules: [rules.isValidStartDateDl()],
  },
  [FieldsEnum.docs_dl_validUntil]: {
    type: FieldType.date,
    params: dateParams,
    rules: [rules.isValidFinalDateDl()],
  },
  [FieldsEnum.docs_dl_series]: {
    rules: [rules.required, {
      pattern: dlSeriesPattern,
      message: 'Образец заполнения 77АО',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_dl_number]: {
    rules: [rules.required, {
      pattern: dlNumberPattern,
      message: 'Образец заполнения 100009',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_dl_issuedBy]: {
    rules: [rules.required, rules.checkingSpacesStartAndEndLine, {
      pattern: dlIssuedByPattern,
      message: 'Образец заполнения ГИБДД 7789',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_dl_whereIssued]: {
    rules: [rules.required, rules.checkingSpacesStartAndEndLine, {
      pattern: dlWhereIssuedPattern,
      message: 'Образец заполнения Ростовская область',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_dl_category]: {
    rules: [rules.required, rules.checkingSpacesStartAndEndLine, {
      pattern: dlCategoryPattern,
      message: 'Образец заполнения В1, С, С1',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_dl_file]: {
    type: FieldType.dragger,
    params: uploadProps,
    rules: [rules.required, rules.checkImageFormat(), rules.checkImageSize()],
  },

  // ПТС
  [FieldsEnum.docs_pts_vin]: {
    rules: [rules.required, {
      pattern: ptsVinPattern,
      message: 'Образец заполнения WAUZZZ44ZEN096063',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_pts_brandName]: {
    rules: [rules.required, rules.checkingSpacesStartAndEndLine, {
      pattern: ptsBrandNamePattern,
      message: 'Образец заполнения Volkswagen',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_pts_model]: {
    rules: [rules.required, rules.checkingSpacesStartAndEndLine, {
      pattern: ptsModelPattern,
      message: 'Образец заполнения Golf Sportsvan',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_pts_engineVolume]: {
    rules: [rules.required, {
      pattern: ptsEngineVolumePattern,
      message: 'Образец заполнения 2100',
      validateTrigger: ['onBlur'],
    },
    rules.checkEngineVolume()],
  },
  [FieldsEnum.docs_pts_enginePower]: {
    rules: [rules.required, {
      pattern: ptsEnginePowerPattern,
      message: 'Образец заполнения 104,7',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_pts_file]: {
    type: FieldType.dragger,
    params: uploadProps,
    rules: [rules.required, rules.checkImageFormat(), rules.checkImageSize()],
  },

  // ОСАГО
  [FieldsEnum.docs_osago_series]: {
    rules: [rules.required, {
      pattern: osagoSeriesPattern,
      message: 'Образец заполнения ААА',
      validateTrigger: ['onBlur'],
    }],

  },
  [FieldsEnum.docs_osago_number]: {
    rules: [rules.required, {
      pattern: osagoNumberPattern,
      message: 'Образец заполнения 1234567890',
      validateTrigger: ['onBlur'],
    }],
  },
  [FieldsEnum.docs_osago_startTime]: {
    type: FieldType.date,
    params: dateParams,
    rules: [rules.isValidStartDateOsago()],
  },
  [FieldsEnum.docs_osago_endTime]: {
    type: FieldType.date,
    params: dateParams,
    rules: [rules.isValidFinalDateOsago()],
  },
  [FieldsEnum.docs_osago_file]: {
    type: FieldType.dragger,
    params: uploadProps,
    rules: [rules.required, rules.checkImageFormat(), rules.checkImageSize()],
  },

  // ПДН
  [FieldsEnum.docs_pdn_file]: {
    type: FieldType.dragger,
    params: uploadProps,
    rules: [rules.required, rules.checkImageFormat(), rules.checkImageSize()],
  },

  // Подтверждение
  [FieldsEnum.confirmaDataAccuracy]: {
    type: FieldType.checkbox,
    rules: [rules.checkCheckbox()],
  },
  [FieldsEnum.agreementPersonalData]: {
    type: FieldType.checkbox,
    rules: [rules.checkCheckbox()],
  },
};

export const fields = (Object.keys(baseFields) as (keyof typeof baseFields)[]).reduce(
  (acc, name) => ({
    ...acc,
    [name]: {
      ...baseFields[name],
      name,
      label: labels[name],
    },
  }),
  {}
) as Record<FieldsEnum, typeof baseFields[keyof typeof baseFields] & { name: FieldsEnum; label: string }>;

export const fieldNames = Object.keys(fields);

export type FieldNames = typeof fieldNames[number];

export default fields;
