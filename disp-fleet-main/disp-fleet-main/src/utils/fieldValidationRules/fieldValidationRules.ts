import { phoneNumberRegexpInternational } from 'utils/utils';
/* eslint-disable prefer-promise-reject-errors */
import { FormInstance, RuleObject } from 'antd/lib/form';
import { StoreValue } from 'antd/lib/form/interface';
import moment, { DurationInputArg1, Moment } from 'moment';
import { CostField } from './types/CostField';
import { TimeFields, YearFields } from './types/TimeFields';

/** Максимальное значение целого числа */
export const MAX_INT = 2147483647;
/** Максимальное значение стоимости (INT / 100) */
export const MAX_INT_COST = MAX_INT / 100;
/** Максимальное значение времени в минутах (INT / 600 / 100) */
export const MAX_INT_TIME = Math.floor(MAX_INT / 600) / 100;
/** Минимальный год производства транспортного средства */
export const MIN_VEHICLE_MANUFACTURED_YEAR = 1900;
/** Допустимые символы для регистрационного номера */
export const ALLOWED_VEHICLE_REGISTRY_CHARS = '[ABEKMHOPCTYXDabekmhopctyxDАВЕКМНОРСТУХавекмнорстух]';

export const validationPatterns = {
  stateNumberRegExp: new RegExp(`(${ALLOWED_VEHICLE_REGISTRY_CHARS}\\d{3}${ALLOWED_VEHICLE_REGISTRY_CHARS}{2}\\d{2,3}|${ALLOWED_VEHICLE_REGISTRY_CHARS}{2}\\d{3}\\d{2,3})(RUS|rus)`),
  semitrailerNumberRegExp: new RegExp(`^${ALLOWED_VEHICLE_REGISTRY_CHARS}{2}\\d{5}\\s?\\d{2,3}\\s?RUS|rus$`),
  vinRegExp: /^[A-HJ-NPR-Z0-9]{3}-?[A-HJ-NPR-Z0-9]{6}-?[A-HJ-NPR-Z0-9]-?[A-HJ-NPR-Z0-9]{7}$/,
  insuranceNumberRegExp: /^[A-Z]{3}\s?[0-9]{10}$/,
  vehiclePassport: /^[0-9]{2}\s[A-Z]{2}\s[0-9]{6}$/,
  phoneNumberRegExp: /\+7\s?\(\d{3}\)\s?\d{3}-?\d{2}-?\d{2}/,
  serviceLicenseRegexp: /^\w{3}-\d{2}-\d{6}$/,
  driverPassportRegexp: /^\d{4}\s\d{6}$/,
  driverLicenseRegexp: /^\d{2}\s\d{2}\s\d{6}$/,
  decimalRegexp: /^[0-9]+(\.[0-9])?$/,
  trimRegexp: /^[\s]+|[\s]+$/,
};

/**
 * Правила валидации для форм Ant Design
 */
export const ValidationRules = {
  general: {
    /**
     * Обязательное поле
     */
    required: {
      required: true,
      message: 'Заполните поле',
    },
    shortPassword: {
      required: true,
      message: 'Пароль должен иметь минимум 8 символов',
    },
    alphaNum: {
      pattern: /^[а-яА-Я\w]+$/,
      message: 'Поле должно состоять из букв, цифр и символов подчеркивания',
    },
    alphaNumWithSpaces: {
      pattern: /^[а-яА-Я\w\s]+$/,
      message: 'Поле должно состоять из букв, цифр, пробелов и символов подчеркивания',
    },
    minMaxLength: (min: number, max: number) => (): { min: number; max: number } => ({
      min,
      max,
    }),
    email: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return /^(?!.*[,])\S+@\S+\.\S+$/.test(value) ? Promise.resolve() : Promise.reject('Введите корректный e-mail');
      },
    }),
    onlyDigits: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return /^\d*$/.test(value) ? Promise.resolve() : Promise.reject('Может содержать только цифры!');
      },
    }),
    floatDecimal: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return /^[0-9]+(\.[0-9])?$/.test(value)
          ? Promise.resolve()
          : Promise.reject('Может содержать только числа с одним знаком после запятой!');
      },
    }),
    floatTwoSymbols: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return /^[0-9]+(\.[0-9]{1,2})?$/.test(value)
          ? Promise.resolve()
          : Promise.reject('Может содержать только числа с одним или двумя знаками после запятой!');
      },
    }),
    validationFloatingNumbers: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return /^\d*$/.test(value) ? Promise.resolve() : Promise.reject('Может содержать только целые числа');
      },
    }),
    positiveNumbers: (): RuleObject => ({
      validator: (_, value) => {
        if (value === null || value === undefined) {
          return Promise.resolve();
        }
        return /^[1-9]\d*$/.test(value)
          ? Promise.resolve()
          : Promise.reject('Может содержать только целые числа больше нуля');
      },
    }),
    checkPhoneMask: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return phoneNumberRegexpInternational.test(value)
          ? Promise.resolve()
          : Promise.reject('Маска ввода должна соответствовать формату +7 (999) 999-99-99');
      },
    }),
    checkPhoneNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.phoneNumberRegExp.test(value)
          ? Promise.resolve()
          : Promise.reject('Номер телефона должен иметь следующий формат 8 (999) 999-99-99');
      },
    }),
    maskRegEx: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return /^[0-9A-Za-z!@.,;:'"?-]{12}$/.test(value)
          ? Promise.resolve()
          : Promise.reject('Допустим ввод только цифр. Длина ИНН должна быть 12 символов');
      },
    }),
    maxLength: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length <= n ? Promise.resolve() : Promise.reject(`Не более ${n} символов`);
      },
    }),
    minLength: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length >= n ? Promise.resolve() : Promise.reject(`Не менее ${n} символов`);
      },
    }),
    max: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return parseFloat(value) <= n ? Promise.resolve() : Promise.reject(`Не более ${n}`);
      },
    }),
    maxInt: (name?: string) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        let max = MAX_INT;
        let message = `Не более ${max}`;
        if (name && name in CostField) {
          max = MAX_INT_COST;
          message = `Не более ${max} руб.`;
        }
        if (name && name in TimeFields) {
          max = MAX_INT_TIME;
          message = `Не более ${max} мин.`;
        }
        if (name && name in YearFields) {
          max = +moment().format('gggg');
          message = `Не более ${max} года`;
        }
        return parseFloat(value) <= max ? Promise.resolve() : Promise.reject(message);
      },
    }),
    minInt: (name?: string) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        let min = -MAX_INT;
        let message = `Не менее ${min}`;
        if (name && name in YearFields) {
          min = MIN_VEHICLE_MANUFACTURED_YEAR;
          message = `Не менее ${min} года`;
        }
        return parseFloat(value) >= min ? Promise.resolve() : Promise.reject(message);
      },
    }),
    passportNumberRule: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.driverPassportRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject('Номер паспорта должен иметь следующий формат 1234 123456');
      },
    }),
    driverLicenseRule: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.driverLicenseRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject('Номер водительских прав должен иметь следующий формат 12 12 123456');
      },
    }),
    serviceLicenseRule: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.serviceLicenseRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject('Номер лицензии должен иметь следующий формат AAA-12-123456, где A - латинская буква/цифра');
      },
    }),
    checkStateNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }

        return validationPatterns.stateNumberRegExp.test(value.replace(/[_ ]/g, ''))
          ? Promise.resolve()
          : Promise.reject('Регистрационный знак должен соответствовать выбранному формату');
      },
    }),
    checkSemitrailerNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }

        return validationPatterns.semitrailerNumberRegExp.test(value.replace(/_/g, ''))
          ? Promise.resolve()
          : Promise.reject('Регистрационный знак должен иметь следующий формат: AА12345 123 RUS');
      },
    }),
    checkVin: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.vinRegExp.test(value)
          ? Promise.resolve()
          : Promise.reject(
            'Идентификационный номер должен содержать 17 символов: латинские буквы (кроме I, O, Q) и цифры'
          );
      },
    }),
    checkInsuranceNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.insuranceNumberRegExp.test(value)
          ? Promise.resolve()
          : Promise.reject('Полис страхования должен иметь следующий формат AAA 1234567890, где A - латинская буква');
      },
    }),
    checkVehiclePassport: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.vehiclePassport.test(value)
          ? Promise.resolve()
          : Promise.reject(
            'Паспорт транспортного средства должен иметь следующий формат 12 AA 123456, где A - латинская буква'
          );
      },
    }),
    minManufactureYear: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return parseFloat(value) >= n ? Promise.resolve() : Promise.reject(`Год производства не ранее ${n} года`);
      },
    }),
    validatorStartDate: (form: FormInstance, endDateKey = 'endDate'): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        const endValue = form.getFieldValue(endDateKey);

        if (!endValue) {
          return Promise.resolve();
        }

        return value < endValue ? Promise.resolve() : Promise.reject('Дата начала не может быть раньше конечной даты');
      },
    }),
    validatorEndDate: (
      form: FormInstance,
      startDateKey = 'startDate',
      [diff, unit]: [DurationInputArg1, moment.unitOfTime.DurationConstructor] = [1, 'month']
    ): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        const startValue = form.getFieldValue(startDateKey);

        if (!startValue) {
          return Promise.resolve();
        }

        return value < (startValue as Moment).clone().add(diff, unit) ? Promise.resolve() : Promise.reject('Превышен допустимый диапазон');
      },
    }),
    maxWaitingTime: (): RuleObject => ({
      validator: async (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        if (!/^\d*$/.test(value)) {
          return Promise.reject('Укажите время ожидания от 0 до 500 минут');
        }
        if (value > 500) {
          return Promise.reject('Укажите время ожидания от 0 до 500 минут');
        }
        return Promise.resolve();
      },
    }),
    maxLoadersTime: (
    ): RuleObject => ({
      validator: async (_, value) => {
        if (value > 500) {
          return Promise.reject('Максимальное время работы грузчиков 500 минут');
        }
        return Promise.resolve();
      },
    }),
    checkTrimmedField: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }

        return !validationPatterns.trimRegexp.test(value) ? Promise.resolve() : Promise.reject('Поле не должно содержать пробелы');
      },
    }),
  },
};
