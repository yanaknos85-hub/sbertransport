import { Employee } from '@sber-sbertransport/mf-core';
import { RuleObject } from 'antd/lib/form';
import { StoreValue } from 'antd/lib/form/interface';
import moment from 'moment';
import { Information } from 'stores/Trip/Trip.interface';

import { declOfNumForSymbols, formatRubles, phoneNumberRegexp } from 'utils';
import { checkingImageFormat } from 'utils/checkingImageFormat';
import { MAX_FILE_SIZE_BYTES } from './constants/validation';

export const ValidationRules = {
  general: {
    required: {
      required: true,
      message: 'Пожалуйста, заполните это поле',
    },
    checkingSpacesStartAndEndLine: {
      pattern: new RegExp(/^[^\s](.*[^\s])?$/),
      message: 'Недопустимое значение, пробелы запрещены в начале и в конце строки',
      validateTrigger: ['onBlur'],
    },
    digits: {
      pattern: new RegExp(/^[\d]+$/),
      message: 'Недопустимое значение',
    },
    latters: {
      pattern: new RegExp(/^[\u0400-\u04FFa-zA-Z\s]+$/),
      message: 'Недопустимое значение',
    },
    checkPattern: (pattern: RegExp, error: string): RuleObject => ({
      validator: async (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.reject(new Error('Обязательное поле'));
        }

        const formatString = String(value);
        const reg = formatString.match(pattern);

        if (value && reg) {
          return Promise.resolve();
        } else {
          return Promise.reject(new Error(error));
        }
      },
    }),
    minLengthString: {
      pattern: new RegExp(/^.{1,50}$/),
      message: 'Недопустимое значение',
    }, // ДЛЯ ЗАДАЧИ С ПРИСОЕДИНЕНИЕМ ПАССАЖИРОВ
    isValidIndicationPassangers: (employees: Employee[]): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.reject(new Error('Обязательное поле'));
        }
        const userId = value.userId;
        let count = 0;

        for (const key in employees) {
          if (employees[key].userId === userId) {
            count++;
            if (count > 1) {
              return Promise.reject(new Error('Выбранный пассажир уже присутствует в поездке'));
            }
          }
        }

        return Promise.resolve();
      },
    }),
    isValidStartDateOsago: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        const now = moment().add(-1, 'days');

        if (!value) {
          return Promise.reject(new Error('Обязательное поле'));
        }

        return value <= now ? Promise.resolve() : Promise.reject(new Error('Недопустимое значение'));
      },
    }),
    isValidStartDateDl: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        const now = moment();
        const maxDate = moment([1999]).endOf('year');

        if (!value) {
          return Promise.reject(new Error('Обязательное поле'));
        }

        return value <= now && value >= maxDate ? Promise.resolve() : Promise.reject(new Error('Недопустимое значение'));
      },
    }),
    isValidFinalDateOsago: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.reject(new Error('Обязательное поле'));
        }

        const now = moment();
        const maxDate = moment().add(16, 'months');

        return value >= now && value <= maxDate ? Promise.resolve() : Promise.reject(new Error('Недопустимое значение'));
      },
    }),
    isValidFinalDateDl: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.reject(new Error('Обязательное поле'));
        }

        const now = moment();
        const maxDate = moment([2040]).endOf('year');

        return value > now && value < maxDate ? Promise.resolve() : Promise.reject(new Error('Недопустимое значение'));
      },
    }),
    isValidMarriageCertDate: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        const now = moment();
        const maxDate = moment().add(70, 'years');

        return value <= now && value < maxDate ? Promise.resolve() : Promise.reject(new Error('Недопустимое значение'));
      },
    }),
    isValidTripRequestDate: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.reject();
        }

        const currentDate = moment().startOf('minute');

        return !value.isBefore(currentDate) ? Promise.resolve() : Promise.reject(new Error('Выбранная дата меньше текущего времени'));
      },
    }),
    maxPassengerCount: (n: number): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => +value <= n ? Promise.resolve() : Promise.reject(new Error(`Максимальное количество ${n}`)),
    }),
    minMaxLength: (min: number, max: number) => (): RuleObject => ({
      min,
      max,
      message: `Значения поле должны быть между ${min} и ${max} ${declOfNumForSymbols(max)}`,
    }),
    checkImageFormat: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value || !value[0] || !value[0].type) {
          return Promise.resolve();
        }

        return checkingImageFormat(value[0].type)
          ? Promise.resolve()
          : Promise.reject('формат изображения должен соответствовать допустимым форматам JPEG, PNG, PDF, TIFF, JPG, HEIF');
      },
    }),
    checkImageFormatForPublicDocuments: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value || !value.file.type) {
          return Promise.resolve();
        }

        return checkingImageFormat(value.file.type)
          ? Promise.resolve()
          : Promise.reject('формат изображения должен соответствовать допустимым форматам JPEG, PNG, PDF, TIFF, JPG, HEIF');
      },
    }),
    checkImageSize: (limitMegabytesInBytes = MAX_FILE_SIZE_BYTES): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value || !value[0] || !value[0].size) {
          return Promise.resolve();
        }

        return value[0].size < limitMegabytesInBytes
          ? Promise.resolve()
          : Promise.reject(`Размер файла превышает допустимые ${MAX_FILE_SIZE_BYTES / 1024 / 1024}МБ`);
      },
    }),
    checkImageSizeForPublicDocuments: (limitMegabytesInBytes = MAX_FILE_SIZE_BYTES): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value || !value.file.size) {
          return Promise.resolve();
        }

        return value.file.size < limitMegabytesInBytes
          ? Promise.resolve()
          : Promise.reject(`Размер файла превышает допустимые ${MAX_FILE_SIZE_BYTES / 1024 / 1024}МБ`);
      },
    }),
    checkCheckbox: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        return value
          ? Promise.resolve()
          : Promise.reject('Обязательное поле');
      },
    }),
    checkEngineVolume: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        return value >= 500
          ? Promise.resolve()
          : Promise.reject('не менее 500 см³.');
      },
    }),
    checkPhoneMask: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return phoneNumberRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject('Маска ввода должна соответствовать формату +79999999999');
      },
    }),
    min: (n: number) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return Number(value) >= n ? Promise.resolve() : Promise.reject(new Error(`Минимальное значение - ${n}`));
      },
    }),
    minLength: (n: number) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length >= n
          ? Promise.resolve()
          : Promise.reject(new Error(`Не менее чем ${n} ${declOfNumForSymbols(n)}`));
      },
    }),
    maxLength: (n: number) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length <= n
          ? Promise.resolve()
          : Promise.reject(new Error(`Не более ${n} ${declOfNumForSymbols(n)}`));
      },
    }),
    maxMoneyValue: (n: number): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => +value <= n ? Promise.resolve() : Promise.reject(new Error(`Не более ${formatRubles(n)} рублей`)),
    }),
    minMoneyValue: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => value !== 0 ? Promise.resolve() : Promise.reject(new Error(`Недопустимое значение`)),
    }),
    includes: (list: (string | number)[] = []) => (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return list.includes(value) ? Promise.resolve() : Promise.reject(new Error(`Недопустимое значение`));
      },
    }),
    checkClientFullName: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        return value
          ? Promise.resolve()
          : Promise.reject('Необходимо заполнить поле или добавить фразу "Нет пассажира"');
      },
    }),
    isValidChildSeat: (list: Information): RuleObject => ({
      validator: (_: RuleObject): Promise<void> => {
        if (!list.childSeatDetails) {
          return Promise.reject(new Error('Заполните это поле'));
        }

        return Promise.resolve();
      },
    }),
  },
};

export const availablePhoneCode = [
  '73',
  '78',
  '79',
  '74',
  '70',
  '372',
  '77',
  '374',
  '352',
  '995',
  '371',
  '370',
  '375',
  '998',
  '996',
  '44',
  '43',
  '33',
  '1',
  '38',
  '972',
  '381',
  '373',
  '358',
  '49',
  '420',
  '421',
  '36',
];

const availablePhoneCodeReg = `(${availablePhoneCode.join('|')})`;
const phoneCodeReg = new RegExp(`^[+]${availablePhoneCodeReg}[0-9]{9}$`);

const phoneReg = new RegExp(/[+\d+\s()]{18}/g);

export const validationPatterns = {
  phoneValidation: phoneCodeReg,
  numberValidation: phoneReg,
};
