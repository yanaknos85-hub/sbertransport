/**
 * @fileoverview
 * Module containing field validation rules and patterns for the application.
 * Provides reusable validation configurations for Ant Design form fields,
 * including pattern matching rules, constraint validators, and helper functions.
 *
 * This module defines:
 * - Maximum integer values for various field types (general, cost, time)
 * - Minimum vehicle manufactured year constant
 * - Allowed vehicle registry character set for Cyrillic/Latin车牌 validation
 * - Validation patterns for various input formats (phone, passport, license, etc.)
 * - Comprehensive validation rules for form fields with custom validator functions
 * - Pattern matching helper for input field event handlers
 */

import { ChangeEventHandler } from 'react';
import moment, { DurationInputArg1, Moment } from 'moment';
import { FormInstance, RuleObject } from 'antd/lib/form';
import { StoreValue } from 'antd/lib/form/interface';

import { phoneNumberRegexpInternational } from 'utils/utils';

import { CostField } from './types/CostField';
import { TimeFields, YearFields } from './types/TimeFields';

/**
 * Maximum integer value (2^31 - 1) used as baseline for various validations.
 * This represents the largest positive integer that can be stored in a 32-bit signed integer.
 */
export const MAX_INT = 2147483647;

/**
 * Maximum integer value for cost fields, derived from MAX_INT divided by 100.
 * Used to validate monetary amounts to prevent overflow issues.
 */
export const MAX_INT_COST = MAX_INT / 100;

/**
 * Maximum integer value for time fields, derived from MAX_INT divided by 600 and then by 100.
 * Used to validate time durations (in minutes) with appropriate scaling.
 */
export const MAX_INT_TIME = Math.floor(MAX_INT / 600) / 100;

/**
 * Minimum allowed vehicle manufactured year.
 * Set to 1900 as the earliest valid year for vehicle registration validation.
 */
export const MIN_VEHICLE_MANUFACTURED_YEAR = 1900;

/**
 * Regular expression character class containing allowed characters for vehicle registry numbers.
 * Includes both Latin letters (A-X, E, K, M, H, O, P, C, T, Y, X, D) and Cyrillic equivalents
 * used in Russian vehicle registration plates.
 */
export const ALLOWED_VEHICLE_REGISTRY_CHARS = '[ABEKMHOPCTYXDabekmhopctyxDАВЕКМНОРСТУХавекмнорстух]';

/**
 * Object containing regular expression patterns for various field validations.
 * Each property represents a specific format pattern used for input validation.
 */
export const validationPatterns = {
  /**
   * Pattern for Russian vehicle state number plates.
   * Format: Letter 3 digits Letter 2 Optional space 2-3 digits Optional space RUS/rus
   * Example: A123AA 123 RUS
   */
  stateNumberRegExp: new RegExp(`^${ALLOWED_VEHICLE_REGISTRY_CHARS}\\d{3}${ALLOWED_VEHICLE_REGISTRY_CHARS}{2}\\s?\\d{2,3}(\\s?RUS|\\s?rus)?$`),
  /**
   * Pattern for Russian semi-trailer registration numbers.
   * Format: 2 Letters 5 digits Optional space 2-3 digits Optional space RUS/rus
   * Example: AA12345 123 RUS
   */
  semitrailerNumberRegExp: new RegExp(`^${ALLOWED_VEHICLE_REGISTRY_CHARS}{2}\\d{5}\\s?\\d{2,3}(\\s?RUS|\\s?rus)?$`),
  /**
   * Pattern for Vehicle Identification Number (VIN).
   * Format: 3 chars Dash 6 chars Dash 1 char Dash 7 chars (chars can be letters or digits)
   * Excludes I, O, Q to avoid confusion with 1 and 0
   * Example: AAA-AAAAAA-A-AAAAAAA
   */
  vinRegExp: /^[A-HJ-NPR-Z0-9]{3}-?[A-HJ-NPR-Z0-9]{6}-?[A-HJ-NPR-Z0-9]-?[A-HJ-NPR-Z0-9]{7}$/,
  /**
   * Pattern for insurance policy numbers.
   * Format: 3 letters Optional space 10 digits
   * Example: ABC 1234567890
   */
  insuranceNumberRegExp: /^[A-Z]{3}\s?[0-9]{10}$/,
  /**
   * Pattern for vehicle passport numbers.
   * Format: 2 digits Space 2 letters Space 6 digits
   * Example: 12 AA 123456
   */
  vehiclePassport: /^[0-9]{2}\s[A-Z]{2}\s[0-9]{6}$/,
  /**
   * Pattern for Russian phone numbers in international format.
   * Format: +7 Space Opening paren 3 digits Closing paren 3 digits Optional dash 2 digits Optional dash 2 digits
   * Example: +7 (999) 999-99-99
   */
  phoneNumberRegExp: /^\+7\s\(\d{3}\)\s\d{3}-\d{2}-\d{2}$/,
  /**
   * Pattern for service license numbers.
   * Format: 3 word characters Dash 2 digits Dash 6 digits
   * Example: AAA-12-123456
   */
  serviceLicenseRegexp: /^\w{3}-\d{2}-\d{6}$/,
  /**
   * Pattern for Russian citizen's internal passport number.
   * Format: 4 digits Space 6 digits
   * Example: 1234 123456
   */
  driverPassportRegexp: /^\d{4}\s\d{6}$/,
  /**
   * Pattern for driver's license number in Russia.
   * Format: 2 digits Space 2 digits Space 6 digits
   * Example: 12 12 123456
   */
  driverLicenseRegexp: /^\d{2}\s\d{2}\s\d{6}$/,
  /**
   * Pattern for decimal numbers with at most one decimal place.
   * Format: One or more digits Optional dot and one digit
   * Example: 123 or 123.4
   */
  decimalRegexp: /^[0-9]+(\.[0-9])?$/,
  /**
   * Pattern to detect leading or trailing whitespace in strings.
   * Used to identify fields that need trimming.
   */
  trimRegexp: /^[\s]+|[\s]+$/,
  /**
   * Pattern for email addresses supporting both Latin and Cyrillic domains.
   * Format: Local part (alphanumeric with special chars) @ Domain (alphanumeric with dots) TLD (2-6 letters)
   */
  emailRegexp: /^[a-zA-Z0-9_%.+-]+(\.[a-zA-Z0-9_%.+-]+)*@[a-zA-ZА-ЯЁа-яё0-9.-]+\.[a-zA-ZА-ЯЁа-яё]{2,6}$/,
  /**
   * Pattern for name fields containing only letters (Latin and Cyrillic) and hyphens.
   * Does not allow numbers, spaces, or other special characters.
   */
  nameRegexp: /^[a-zA-ZА-ЯЁа-яё-]+$/,
  /**
   * Pattern for patronymic fields containing letters, spaces, and hyphens.
   * Used for Russian middle names which may contain multiple words.
   */
  patronymicRegexp: /^[-a-zA-ZА-ЯЁа-яё\s]+$/,
};

/**
 * Object containing comprehensive validation rule configurations for Ant Design form fields.
 * Each rule can be used directly in form field configurations and supports custom validation logic.
 * The object is organized into logical groups for general, numeric, date, and document-specific validations.
 */
export const ValidationRules = {
  /**
   * General-purpose validation rules for text fields and basic input validation.
   * Includes rules for required fields, length constraints, character sets, and common formats.
   */
  general: {
    /**
     * Validates that the field is not empty.
     * @returns Rule configuration with required flag and Russian error message.
     */
    required: {
      required: true,
      message: 'Заполните поле',
    },
    /**
     * Validates that password meets minimum 8 character requirement.
     * @returns Rule configuration with required flag and password length message.
     */
    shortPassword: {
      required: true,
      message: 'Пароль должен иметь минимум 8 символов',
    },
    /**
     * Validates that field contains only alphanumeric characters and underscores.
     * Supports Cyrillic and Latin characters.
     * @returns Rule configuration with pattern and error message.
     */
    alphaNum: {
      pattern: /^[а-яА-Я\w]+$/,
      message: 'Поле должно состоять из букв, цифр и символов подчеркивания',
    },
    /**
     * Validates that field contains letters, digits, spaces, and underscores only.
     * Supports Cyrillic and Latin characters.
     * @returns Rule configuration with pattern allowing spaces and error message.
     */
    alphaNumWithSpaces: {
      pattern: /^[а-яА-Я\w\s]+$/,
      message: 'Поле должно состоять из букв, цифр, пробелов и символов подчеркивания',
    },
    /**
     * Validates that field contains only letters (Latin or Cyrillic) without spaces or numbers.
     * @returns Rule configuration using nameRegexp pattern.
     */
    nameRule: {
      pattern: validationPatterns.nameRegexp,
      message: 'Поле должно содержать только буквы',
    },
    /**
     * Validates that field contains only letters, spaces, and hyphens.
     * Suitable for patronymic/middle name fields.
     * @returns Rule configuration using patronymicRegexp pattern.
     */
    patronymicRule: {
      pattern: validationPatterns.patronymicRegexp,
      message: 'Поле должно содержать только буквы, пробелы и дефисы',
    },
    /**
     * Creates a length validation rule with specified minimum and maximum lengths.
     * @param min - Minimum allowed length (inclusive).
     * @param max - Maximum allowed length (inclusive).
     * @returns Function that returns rule object with min and max properties.
     */
    minMaxLength: (min: number, max: number) => (): { min: number; max: number } => ({
      min,
      max,
    }),
    /**
     * Validates email format using the emailRegexp pattern.
     * @returns Rule object with custom validator that checks email format.
     */
    email: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.emailRegexp.test(value) ? Promise.resolve() : Promise.reject(new Error('Введите корректный e-mail'));
      },
    }),
    /**
     * Validates that field contains only digits (0-9).
     * @returns Rule object with custom validator that rejects non-digit input.
     */
    onlyDigits: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        // Reject if value contains anything other than digits 0-9
        if (!/^\d+$/.test(value)) {
          return Promise.reject(new Error('Может содержать только цифры!'));
        }
        return Promise.resolve();
      },
    }),
    /**
     * Validates that field contains a decimal number with at most one decimal place.
     * @returns Rule object with custom validator for decimal format.
     */
    floatDecimal: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        // Reject if value has more than 1 decimal place, negative sign, or non-digits
        // Valid: 123, 123.4 - reject: 123.45, -12.3, abc
        if (!/^[0-9]+(\.[0-9])?$/.test(value)) {
          return Promise.reject(new Error('Может содержать только числа с одним знаком после запятой!'));
        }
        return Promise.resolve();
      },
    }),
    /**
     * Validates that field contains a decimal number with one or two decimal places.
     * @returns Rule object with custom validator for decimal format with up to 2 places.
     */
    floatTwoSymbols: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        // Reject if value has more than 2 decimal places, negative sign, or non-digits
        // Valid: 123, 123.4, 123.45 - reject: 123.456, -12.34, abc
        if (!/^[0-9]+(\.[0-9]{1,2})?$/.test(value)) {
          return Promise.reject(new Error('Может содержать только числа с одним или двумя знаками после запятой!'));
        }
        return Promise.resolve();
      },
    }),
    /**
     * Validates that field contains only integer values (no decimals).
     * @returns Rule object with custom validator for integer format.
     */
    validationFloatingNumbers: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        // Check if it's a valid integer (no decimal point, can be negative)
        // Use ! to check for decimal point in value
        if (!/^-?\d+$/.test(value) || value.includes('.')) {
          return Promise.reject(new Error('Может содержать только целые числа'));
        }
        return Promise.resolve();
      },
    }),
    /**
     * Validates that field contains only positive integers (1, 2, 3...).
     * Does not allow zero or negative numbers.
     * @returns Rule object with custom validator for positive integers.
     */
    positiveNumbers: (): RuleObject => ({
      validator: (_, value) => {
        if (value === null || value === undefined || value === '') {
          return Promise.resolve();
        }
        return /^[1-9]\d*$/.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Может содержать только целые числа больше нуля'));
      },
    }),
    /**
     * Validates phone number format using the international format pattern.
     * @returns Rule object with custom validator for phone number format.
     */
    checkPhoneMask: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return phoneNumberRegexpInternational.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Маска ввода должна соответствовать формату +7 (999) 999-99-99'));
      },
    }),
    /**
     * Validates phone number format using the domestic pattern (8 XXX XXX-XX-XX).
     * @returns Rule object with custom validator for domestic phone format.
     */
    checkPhoneNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.phoneNumberRegExp.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Номер телефона должен иметь следующий формат 8 (999) 999-99-99'));
      },
    }),
    /**
     * Validates mask field format (12 characters of alphanumeric and special symbols).
     * @returns Rule object with custom validator for mask format.
     */
    maskRegEx: (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return /^[0-9A-Za-z!@.,;:'"?-]{12}$/.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Допустим ввод только цифр. Длина ИНН должна быть 12 символов'));
      },
    }),
    /**
     * Creates a maximum length validation rule.
     * @param n - Maximum allowed number of characters.
     * @returns Rule object with custom validator checking character count.
     */
    maxLength: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length <= n ? Promise.resolve() : Promise.reject(new Error(`Не более ${n} символов`));
      },
    }),
    /**
     * Creates a minimum length validation rule.
     * @param n - Minimum required number of characters.
     * @returns Rule object with custom validator checking character count.
     */
    minLength: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return value.toString().length >= n ? Promise.resolve() : Promise.reject(new Error(`Не менее ${n} символов`));
      },
    }),
    /**
     * Creates a maximum numeric value validation rule.
     * @param n - Maximum allowed numeric value.
     * @returns Rule object with custom validator checking numeric value.
     */
    max: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return parseFloat(value) <= n ? Promise.resolve() : Promise.reject(new Error(`Не более ${n}`));
      },
    }),
    /**
     * Creates a maximum value validation rule that adapts based on field name.
     * Handles different max values for cost, time, and year fields.
     * @param name - Optional field name to determine max value type.
     * @returns Rule object with context-aware maximum validation.
     */
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
        return parseFloat(value) <= max ? Promise.resolve() : Promise.reject(new Error(message));
      },
    }),
    /**
     * Creates a minimum value validation rule that adapts based on field name.
     * Uses MIN_VEHICLE_MANUFACTURED_YEAR for year fields.
     * @param name - Optional field name to determine min value type.
     * @returns Rule object with context-aware minimum validation.
     */
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
        return parseFloat(value) >= min ? Promise.resolve() : Promise.reject(new Error(message));
      },
    }),
    /**
     * Validates Russian citizen's passport number format (XXXX XXXXXX).
     * @returns Rule object with custom validator using driverPassportRegexp pattern.
     */
    passportNumberRule: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.driverPassportRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Номер паспорта должен иметь следующий формат 1234 123456'));
      },
    }),
    /**
     * Validates Russian driver's license number format (XX XX XXXXXX).
     * @returns Rule object with custom validator using driverLicenseRegexp pattern.
     */
    driverLicenseRule: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.driverLicenseRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Номер водительских прав должен иметь следующий формат 12 12 123456'));
      },
    }),
    /**
     * Validates service license number format (AAA-12-123456).
     * @returns Rule object with custom validator using serviceLicenseRegexp pattern.
     */
    serviceLicenseRule: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.serviceLicenseRegexp.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Номер лицензии должен иметь следующий формат AAA-12-123456, где A - латинская буква/цифра'));
      },
    }),
    /**
     * Validates Russian vehicle state number plate format.
     * @returns Rule object with custom validator using stateNumberRegExp pattern.
     */
    checkStateNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }

        return validationPatterns.stateNumberRegExp.test(value.replace(/_/g, ''))
          ? Promise.resolve()
          : Promise.reject(new Error('Регистрационный знак должен иметь следующий формат: A123AA 123 RUS'));
      },
    }),
    /**
     * Validates Russian semi-trailer registration number format.
     * @returns Rule object with custom validator using semitrailerNumberRegExp pattern.
     */
    checkSemitrailerNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }

        return validationPatterns.semitrailerNumberRegExp.test(value.replace(/_/g, ''))
          ? Promise.resolve()
          : Promise.reject(new Error('Регистрационный знак должен иметь следующий формат: AА12345 123 RUS'));
      },
    }),
    /**
     * Validates Vehicle Identification Number (VIN) format.
     * @returns Rule object with custom validator using vinRegExp pattern.
     */
    checkVin: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.vinRegExp.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error(
            'Идентификационный номер должен иметь следующий формат AAA-AAAAAA-A-AAAAAAA, где A - латинская буква/цифра'
          ));
      },
    }),
    /**
     * Validates insurance policy number format.
     * @returns Rule object with custom validator using insuranceNumberRegExp pattern.
     */
    checkInsuranceNumber: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.insuranceNumberRegExp.test(value)
          ? Promise.resolve()
          : Promise.reject(new Error('Полис страхования должен иметь следующий формат AAA 1234567890, где A - латинская буква'));
      },
    }),
    /**
     * Validates vehicle passport number format.
     * @returns Rule object with custom validator using vehiclePassport pattern.
     */
    checkVehiclePassport: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }
        return validationPatterns.vehiclePassport.test(value)
          ? Promise.resolve()
          : Promise.reject(
            new Error('Паспорт транспортного средства должен иметь следующий формат 12 AA 123456, где A - латинская буква')
          );
      },
    }),
    /**
     * Creates a minimum manufacture year validation rule for vehicles.
     * @param n - Minimum allowed year.
     * @returns Rule object with custom validator checking year constraint.
     */
    minManufactureYear: (n: number) => (): RuleObject => ({
      validator: (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        return parseFloat(value) >= n ? Promise.resolve() : Promise.reject(new Error(`Год производства не ранее ${n} года`));
      },
    }),
    /**
     * Creates a start date validation rule that compares against an end date.
     * Ensures start date is not later than end date (can be same day if configured).
     * @param form - Ant Design form instance for accessing field values.
     * @param endDateKey - Key of the end date field in the form.
     * @param isSameDay - If true, allows start date to equal end date.
     * @returns Rule object with custom date comparison validator.
     */
    validatorStartDate: (form: FormInstance, endDateKey = 'endDate', isSameDay = false): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue): Promise<void> => {
        const endValue = form.getFieldValue(endDateKey);

        if (!endValue) {
          return Promise.resolve();
        }

        return (isSameDay ? value <= endValue : value < endValue)
          ? Promise.resolve()
          : Promise.reject(new Error('Дата начала не может быть раньше конечной даты'));
      },
    }),
    /**
     * Creates an end date validation rule that validates against a start date.
     * Ensures end date is within an allowed range from start date.
     * @param form - Ant Design form instance for accessing field values.
     * @param startDateKey - Key of the start date field in the form.
     * @param diff - Duration value for the range calculation (default: 1).
     * @param unit - Time unit for the range calculation (default: 'month').
     * @returns Rule object with custom date range validator.
     */
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

        return value < (startValue as Moment).clone().add(diff, unit) ? Promise.resolve() : Promise.reject(new Error('Превышен допустимый диапазон'));
      },
    }),
    /**
     * Validates waiting time is within allowed range (0-500 minutes).
     * Ensures input is an integer and within bounds.
     * @returns Rule object with custom validator for waiting time constraint.
     */
    maxWaitingTime: (): RuleObject => ({
      validator: async (_, value) => {
        if (!value) {
          return Promise.resolve();
        }
        if (!/^\d*$/.test(value)) {
          return Promise.reject(new Error('Укажите время ожидания от 0 до 500 минут'));
        }
        if (value > 500) {
          return Promise.reject(new Error('Укажите время ожидания от 0 до 500 минут'));
        }
        return Promise.resolve();
      },
    }),
    /**
     * Validates loaders working time does not exceed 500 minutes.
     * Used for cargo handling time constraints.
     * @returns Rule object with custom validator for loaders time constraint.
     */
    maxLoadersTime: (
    ): RuleObject => ({
      validator: async (_, value) => {
        if (value > 500) {
          return Promise.reject(new Error('Максимальное время работы грузчиков 500 минут'));
        }
        return Promise.resolve();
      },
    }),
    /**
     * Validates that field does not contain leading or trailing whitespace.
     * @returns Rule object with custom validator using trimRegexp pattern.
     */
    checkTrimmedField: (): RuleObject => ({
      validator: (_: RuleObject, value: StoreValue) => {
        if (!value) {
          return Promise.resolve();
        }

        return !validationPatterns.trimRegexp.test(value) ? Promise.resolve() : Promise.reject(new Error('Поле не должно содержать пробелы'));
      },
    }),
  },
};

/**
 * Creates an input event handler that filters input based on a regular expression pattern.
 * Only characters matching the pattern will be kept in the input field.
 * @param pattern - Regular expression defining allowed characters.
 * @returns ChangeEventHandler that filters and updates input value on change.
 *
 * @example
 * const phoneHandler = matchPattern(/\d/); // Allows only digits
 * const alphaHandler = matchPattern(/[a-zA-Z]/); // Allows only letters
 */
export const matchPattern = (pattern: RegExp): ChangeEventHandler<HTMLInputElement> => e => {
  e.target.value = e.target.value.match(pattern)?.join('') ?? '';
};
