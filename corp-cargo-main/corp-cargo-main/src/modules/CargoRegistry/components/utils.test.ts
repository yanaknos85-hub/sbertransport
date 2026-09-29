// Мокаем глобальные зависимости
global.fetch = jest.fn();

// Перехватываем все импорты, которые начинаются с src/ или @/
jest.mock('src/ioc/ioc.container', () => ({}), { virtual: true });
jest.mock('src/stores/index', () => ({}), { virtual: true });
jest.mock('src/shared/hooks/useAppStoreContext', () => ({}), { virtual: true });
jest.mock('src/api/index', () => ({}), { virtual: true });
jest.mock('src/api/register-search', () => ({}), { virtual: true });
jest.mock('src/stores/PersonalSearch/PersonalSearch.interface', () => ({}), { virtual: true });
jest.mock('src/modules/Planner/types', () => ({}), { virtual: true });
jest.mock('src/stores/Planner/DIPlanner.store', () => ({}), { virtual: true });
jest.mock('src/ioc/ioc.stores', () => ({}), { virtual: true });
jest.mock('io-ts', () => ({}), { virtual: true });
jest.mock('@sber-sbertransport/mf-core', () => ({}), { virtual: true });

describe('hasDateValue', () => {
  it('возвращает false для undefined', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue(undefined)).toBe(false);
  });

  it('возвращает false для null', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue(null)).toBe(false);
  });

  it('возвращает false для пустой строки', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue('')).toBe(false);
  });

  it('возвращает false для false (boolean)', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue(false)).toBe(false);
  });

  it('возвращает true для true (boolean)', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue(true)).toBe(true);
  });

  it('возвращает false для числа 0 (ноль считается отсутствием значения)', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue(0)).toBe(false);
  });

  it('возвращает true для числа 123', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue(123)).toBe(true);
  });

  it('возвращает true для строки "2023-01-01"', () => {
    const { hasDateValue } = require('./utils');
    expect(hasDateValue('2023-01-01')).toBe(true);
  });

  it('возвращает true для объекта {mode, value} с валидными значениями', () => {
    const { hasDateValue } = require('./utils');
    const dateValue = { mode: 'date', value: ['2023-01-01', '2023-01-02'] };
    expect(hasDateValue(dateValue)).toBe(true);
  });

  it('возвращает false для объекта {mode, value} с пустым массивом', () => {
    const { hasDateValue } = require('./utils');
    const dateValue = { mode: 'date', value: [] };
    expect(hasDateValue(dateValue)).toBe(false);
  });

  it('возвращает false для объекта {mode, value} с null в массиве', () => {
    const { hasDateValue } = require('./utils');
    const dateValue = { mode: 'date', value: [null, null] };
    expect(hasDateValue(dateValue)).toBe(false);
  });

  it('возвращает true для объекта {mode, value} с одним валидным значением', () => {
    const { hasDateValue } = require('./utils');
    const dateValue = { mode: 'date', value: ['2023-01-01', null] };
    expect(hasDateValue(dateValue)).toBe(true);
  });
});

describe('hasAnyActiveFilter', () => {
  it('возвращает true когда задан requestHumanId', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: 'TR-001',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан contractorSet', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: ['contractor-1'],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан cargoTransportType', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: ['truck'],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан authorFIO', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: 'Иванов Иван',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан deadlineDate', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: true,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан expectedCost с валидными значениями', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: 1000, end: 5000 },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан desiredDate с валидным значением', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: ['2023-01-01', '2023-01-02'] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан creationDate с валидным значением', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: ['2023-01-01', '2023-01-02'] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан changeDate с валидным значением', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: ['2023-01-01', '2023-01-02'] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан requestStatusSet', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: ['pending'],
      requestTypeSet: [],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });

  it('возвращает true когда задан requestTypeSet', () => {
    const { hasAnyActiveFilter } = require('./utils');
    const values = {
      requestHumanId: '',
      desiredDate: { mode: 'date', value: [null, null] },
      creationDate: { mode: 'date', value: [null, null] },
      changeDate: { mode: 'date', value: [null, null] },
      expectedCost: { start: null, end: null },
      contractorSet: [],
      authorFIO: '',
      deadlineDate: false,
      requestStatusSet: [],
      requestTypeSet: ['cargo'],
      cargoTransportType: [],
    };
    expect(hasAnyActiveFilter(values)).toBe(true);
  });
});
