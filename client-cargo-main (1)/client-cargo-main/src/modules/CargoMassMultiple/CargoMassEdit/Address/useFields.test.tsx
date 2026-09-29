import { renderHook, act } from '@testing-library/react-hooks';
import { useFields } from './useFields';

// Мокаем nanoid
jest.mock('nanoid', () => ({
  nanoid: jest.fn(() => 'mock-id'),
}));

// Мокаем validationPatterns
jest.mock('shared/fieldValidationRules', () => ({
  validationPatterns: {
    emptyStringValidation: /.(?!\s*$).+/,
  },
  ValidationRules: {
    general: {
      required: {
        required: true,
        message: 'Пожалуйста, заполните это поле',
      },
    },
  },
}));

// Мокаем FieldType
jest.mock('shared/form/Field/Field', () => ({
  FieldType: {
    input: 'input',
    textarea: 'textarea',
    number: 'number',
    phone: 'phone',
    checkbox: 'checkbox',
    radio: 'radio',
    select: 'select',
    selectMultiple: 'selectMultiple',
    date: 'date',
    dateRange: 'dateRange',
    employee: 'employee',
    address: 'address',
    cargoType: 'cargoType',
    rate: 'rate',
  },
}));

// Мокаем IOHumanReadable из mf-core
jest.mock('@sber-sbertransport/mf-core', () => ({
  Employee: jest.fn(),
  IOHumanReadable: jest.fn(),
}));

describe('useFields', () => {
  const createMockData = () => ({
    humanReadableId: 'REQ-001',
    status: 'SUCCESS',
    id: 'test-id',
    creationTime: 1234567890,
    author: {
      humanReadableId: 'AUTHOR-001',
      id: 'author-id',
      userId: 'user-id',
      firstName: 'Тест',
      lastName: 'Тестов',
      personnelNumber: '12345',
      departmentId: 'dept-id',
      organizationId: 'org-id',
      positionId: 'pos-id',
      fullName: 'Тестов Тест Тестович',
      mobilePhone: '+7 (999) 111-11-11',
    },
    sender: {
      id: 'sender-id',
      fullName: 'Иванов Иван Иванович',
      address: 'Москва, ул. Пушкина, д. 1',
      mobilePhone: '+7 (999) 123-45-67',
      firstName: 'Иван',
      lastName: 'Иванов',
      patronymic: 'Иванович',
      humanReadableId: 'SENDER-001',
      personnelNumber: '12345',
      positionId: 'pos-id',
      userId: 'user-id',
    },
    recipient: {
      id: 'recipient-id',
      fullName: 'Петров Петр Петрович',
      address: 'Санкт-Петербург, Невский проспект, д. 100',
      mobilePhone: '+7 (999) 765-43-21',
      firstName: 'Петр',
      lastName: 'Петров',
      patronymic: 'Петрович',
      humanReadableId: 'RECIPIENT-001',
      personnelNumber: '67890',
      positionId: 'pos-id',
      userId: 'user-id',
    },
    senderOrganization: 'ООО Ромашка',
    recipientOrganization: 'ЗАО Петрофф',
    sourceLoaders: true,
    destinationLoaders: false,
    desiredDate: 1234567890,
    deliveryTimeDate: 1234567890,
    listCargo: [],
    tariffs: [],
    totalSizes: {
      width: 10,
      length: 20,
      height: 30,
      volume: 6000,
      weight: 5,
      occupiedPlacesCount: 1,
      packageCost: 0,
    },
    expected: {
      distance: 50,
      time: 120,
      segments: [
        {
          distance: 50,
          time: 120,
          coordinates: [{ latitude: 55.751244, longitude: 37.618423 }],
        },
      ],
      waypoints: [
        {
          addressStringRepresentation: 'Москва, ул. Пушкина, д. 1',
          orderingIndex: 0,
        },
        {
          addressStringRepresentation: 'Санкт-Петербург, Невский проспект, д. 100',
          orderingIndex: 1,
        },
      ],
      cost: 1000,
    },
    express: false,
    description: 'Тестовая заявка',
    sourceLoadersCount: 1,
    destinationLoadersCount: 1,
    desiredDateTime: 1234567890,
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('формирует поле senderAddress с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const senderAddress = fields.senderAddress;

    expect(senderAddress.label).toBe('Адрес отправителя');
    expect(senderAddress.name).toBe('senderAddress');
    expect(senderAddress.initialValue).toBe('Москва, ул. Пушкина, д. 1');
    expect(senderAddress.type).toBe('address');
    expect(senderAddress.rules).toHaveLength(2);
    expect((senderAddress.rules[0] as any).required).toBe(true);
    expect(senderAddress.index).toBe(0);
    expect(senderAddress.params.dropdownMatchSelectWidth).toBe(false);
    expect(senderAddress.params.dropdownStyle.borderRadius).toBe('12px');
  });

  it('формирует поле senderName с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const senderName = fields.senderName;

    expect(senderName.label).toBe('ФИО отправителя');
    expect(senderName.name).toBe('senderName');
    expect(senderName.initialValue).toBe('Иванов Иван Иванович');
    expect(senderName.type).toBe('employee');
    expect(senderName.index).toBe(0);
    expect(senderName.rules).toHaveLength(2);
    expect((senderName.rules[0] as any).required).toBe(true);
  });

  it('формирует поле senderPhone с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const senderPhone = fields.senderPhone;

    expect(senderPhone.label).toBe('Телефон');
    expect(senderPhone.name).toBe('senderPhone');
    expect(senderPhone.initialValue).toBe('+7 (999) 123-45-67');
    expect(senderPhone.type).toBe('phone');
    expect(senderPhone.rules).toHaveLength(1);
    expect((senderPhone.rules[0] as any).required).toBe(true);
  });

  it('формирует поле senderOrganization с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const senderOrganization = fields.senderOrganization;

    expect(senderOrganization.label).toBe('Организация');
    expect(senderOrganization.name).toBe('senderOrganization');
    expect(senderOrganization.initialValue).toBe('ООО Ромашка');
  });

  it('формирует поле sourceLoaders с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const sourceLoaders = fields.sourceLoaders;

    expect(sourceLoaders.label).toBe('Для маршрута требуется грузчик');
    expect(sourceLoaders.name).toBe('sourceLoaders');
    expect(sourceLoaders.initialValue).toBe(true);
    expect(sourceLoaders.type).toBe('checkbox');
  });

  it('формирует поле recipientAddress с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const recipientAddress = fields.recipientAddress;

    expect(recipientAddress.label).toBe('Адрес получателя');
    expect(recipientAddress.name).toBe('recipientAddress');
    expect(recipientAddress.initialValue).toBe('Санкт-Петербург, Невский проспект, д. 100');
    expect(recipientAddress.type).toBe('address');
    expect(recipientAddress.rules).toHaveLength(2);
    expect((recipientAddress.rules[0] as any).required).toBe(true);
    expect(recipientAddress.index).toBe(1);
    expect(recipientAddress.params.dropdownMatchSelectWidth).toBe(false);
    expect(recipientAddress.params.dropdownStyle.borderRadius).toBe('12px');
  });

  it('формирует поле recipientName с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const recipientName = fields.recipientName;

    expect(recipientName.label).toBe('ФИО получателя');
    expect(recipientName.name).toBe('recipientName');
    expect(recipientName.initialValue).toBe('Петров Петр Петрович');
    expect(recipientName.type).toBe('employee');
    expect(recipientName.index).toBe(1);
    expect(recipientName.rules).toHaveLength(2);
    expect((recipientName.rules[0] as any).required).toBe(true);
  });

  it('формирует поле recipientPhone с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const recipientPhone = fields.recipientPhone;

    expect(recipientPhone.label).toBe('Телефон');
    expect(recipientPhone.name).toBe('recipientPhone');
    expect(recipientPhone.initialValue).toBe('+7 (999) 765-43-21');
    expect(recipientPhone.type).toBe('phone');
    expect(recipientPhone.rules).toHaveLength(1);
    expect(recipientPhone.rules[0].required).toBe(true);
  });

  it('формирует поле recipientOrganization с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const recipientOrganization = fields.recipientOrganization;

    expect(recipientOrganization.label).toBe('Организация');
    expect(recipientOrganization.name).toBe('recipientOrganization');
    expect(recipientOrganization.initialValue).toBe('ЗАО Петрофф');
  });

  it('формирует поле destinationLoaders с корректными данными', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const destinationLoaders = fields.destinationLoaders;

    expect(destinationLoaders.label).toBe('Требуется грузчик в точке получения');
    expect(destinationLoaders.name).toBe('destinationLoaders');
    expect(destinationLoaders.initialValue).toBe(false);
    expect(destinationLoaders.type).toBe('checkbox');
  });

  it('обрабатывает пустые значения senderOrganization', () => {
    const mockData = createMockData();
    mockData.senderOrganization = '';
    
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    expect(fields.senderOrganization.initialValue).toBe('');
  });

  it('обрабатывает пустые значения recipientOrganization', () => {
    const mockData = createMockData();
    mockData.recipientOrganization = '';
    
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    expect(fields.recipientOrganization.initialValue).toBe('');
  });

  it('возвращает объект со всеми ожидаемыми полями', () => {
    const mockData = createMockData();
    const { result } = renderHook(() => useFields(mockData));

    const fields = result.current;
    const expectedFieldNames = [
      'senderAddress',
      'senderName',
      'senderPhone',
      'senderOrganization',
      'sourceLoaders',
      'recipientAddress',
      'recipientName',
      'recipientPhone',
      'recipientOrganization',
      'destinationLoaders',
    ];

    expect(Object.keys(fields)).toHaveLength(expectedFieldNames.length);
    expectedFieldNames.forEach(fieldName => {
      expect(fields).toHaveProperty(fieldName);
    });
  });
});
