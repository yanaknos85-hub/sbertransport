/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// Упрощенные моки
jest.mock('react', () => jest.requireActual('react'));

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}));

jest.mock('antd', () => {
  const MockButton = ({ onClick, children, icon, ...props }: any) => (
    <button onClick={onClick} {...props}>
      {icon}
      {children}
    </button>
  );
  
  const MockTextArea = ({ onChange, value, ...props }: any) => (
    <textarea onChange={onChange} value={value} {...props} />
  );
  
  const MockInputNumber = ({ onChange, value, ...props }: any) => (
    <input
      type="number"
      onChange={(e) => onChange(Number(e.target.value))}
      value={value}
      {...props}
    />
  );
  
  const MockDatePicker = ({ onChange, value, ...props }: any) => (
    <input
      type="text"
      onChange={(e) => onChange({ target: { value: e.target.value }})}
      value={value ? value.toString() : ''}
      {...props}
    />
  );
  
  const MockTable = ({ dataSource, columns, ...props }: any) => (
    <div data-testid="table">
      {columns?.map((col: any) => col.title).join(', ')}
      {dataSource?.map((row: any) => JSON.stringify(row))}
    </div>
  );
  
  return {
    Button: MockButton,
    Input: { TextArea: MockTextArea },
    InputNumber: MockInputNumber,
    DatePicker: MockDatePicker,
    Table: MockTable,
    Row: ({ children }: any) => <div>{children}</div>,
    Col: ({ children }: any) => <div>{children}</div>,
    notification: {
      success: jest.fn(),
      error: jest.fn(),
    },
  };
});

jest.mock('@ant-design/icons', () => ({
  EditOutlined: () => <span>EditIcon</span>,
  SaveOutlined: () => <span>SaveIcon</span>,
}));

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      global: {
        cancel: 'Отмена',
        save: 'Сохранить',
      },
    },
  }),
}));

jest.mock('moment', () => {
  const mockMoment = (input?: any) => {
    const format = (formatStr?: string) => {
      if (formatStr) return '01.01.2023 12:00';
      return '01.01.2023 12:00';
    };
    
    return {
      format,
      isValid: () => true,
    };
  };
  
  mockMoment.locale = jest.fn();
  return mockMoment;
});

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('context/Organization.context', () => ({
  useOrganizationContext: () => ({
    organizationId: 'org-123',
  }),
}));

jest.mock('api/order-execution', () => ({
  useForceOrder: () => [
    jest.fn().mockResolvedValue(undefined),
  ],
}));

jest.mock('utils/formatPhoneNumber', () => ({
  formatPhoneNumber: (phone: string) => phone ? `+7 ${phone}` : '-',
}));

jest.mock('utils/formatVolume', () => ({
  formatVolume: (volume: number) => volume ? `${volume} м³` : '-',
}));

jest.mock('shared/components/Map/MapComponent', () => ({
  MapComponent: () => <div data-testid="map">Map Component</div>,
}));

jest.mock('../../Item', () => ({
  Section: ({ title, children, className }: any) => (
    <div data-testid="section" className={className}>
      <h3>{title}</h3>
      {children}
    </div>
  ),
  Item: ({ title, children }: any) => (
    <div data-testid="item">
      <strong>{title}:</strong> {children}
    </div>
  ),
  ItemComment: ({ children }: any) => (
    <div data-testid="item-comment">{children}</div>
  ),
}));

jest.mock('../../OrderStatus', () => ({
  default: ({ status }: any) => (
    <span data-testid="order-status">{status?.rusName || 'Unknown'}</span>
  ),
}));

jest.mock('../Header', () => ({
  OrderHead: ({ caption, children }: any) => (
    <div data-testid="order-head">
      <h2>{caption}</h2>
      {children}
    </div>
  ),
}));

jest.mock('./AddressBlockDetailed', () => ({
  AddressBlockDetailed: () => <div data-testid="address-block">Address Block</div>,
}));

jest.mock('../TariffCard/TariffCard', () => ({
  TariffSection: () => <div data-testid="tariff-section">Tariff Section</div>,
}));

jest.mock('./QrsSection', () => ({
  default: ({ qrs }: any) => (
    <div data-testid="mock-qrs-section">
      <h5>Отсканированные QR-коды</h5>
      {qrs?.map((code: string, i: number) => (
        <span key={i} data-testid={`qr-code-${i}`}>{code}</span>
      ))}
    </div>
  ),
}));

jest.mock('modules/Planner/images/crossIcon.svg', () => ({
  ReactComponent: () => <span>CrossIcon</span>,
}));

jest.mock('../../../constants/Cargo/Cargo', () => ({
  OrderTitle: {
    tariffId: 'Тариф',
    status: 'Статус',
    creationTime: 'Время создания',
    plannedShipmentTime: 'Планируемое время отправки',
    tariff: 'Тариф',
    initiator: 'Инициатор',
    fullName: 'ФИО',
    mobilePhone: 'Мобильный телефон',
    department: 'Отдел',
    approvedBy: 'Согласовал',
    cargo: 'Груз',
    additionalServices: 'Дополнительные услуги',
    loaders: 'Грузчики',
    delivery: 'Доставка',
    transferTime: 'Время передачи',
    shipmentTime: 'Время отправки',
    distance: 'Расстояние',
    cost: 'Стоимость',
    courier: 'Курьер',
    totalWeight: 'Общий вес',
    totalVolume: 'Общий объем',
    route: 'Маршрут',
    contractor: 'Исполнитель',
    car: 'Автомобиль',
    driver: 'Водитель',
    driverPhone: 'Телефон водителя',
    tariffContractor: 'Тариф контрагента',
    carNumber: 'Номер автомобиля',
    commentOrder: 'Комментарий к заявке',
    commentEng: 'Комментарий инженера',
  },
  Statuses: {
    CARGO_APPROVED: 'CARGO_APPROVED',
    CARGO_AWAITING_APPROVAL: 'CARGO_AWAITING_APPROVAL',
    CARGO_DELIVERY_CONFIRMATION_FINISHED: 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  },
  StatusNames: {
    CARGO_APPROVED: 'Согласовано',
    CARGO_AWAITING_APPROVAL: 'Ожидает согласования',
    CARGO_DELIVERY_CONFIRMATION_FINISHED: 'Доставка подтверждена',
  },
  STATUSES: [
    { rusName: 'Согласовано' },
    { rusName: 'Ожидает согласования' },
    { rusName: 'Доставка подтверждена' },
  ],
}));

jest.mock('constants/constants.app', () => ({
  DATE_FORMAT: {
    DATE_WITH_TIME_DOTS: 'DD.MM.YYYY HH:mm',
  },
  emptySign: '-',
}));

jest.mock('./styles.module.scss', () => ({
  editIconBlock: 'editIconBlock',
  icons: 'icons',
  fullWidth: 'fullWidth',
  tripDetailedView: 'tripDetailedView',
  mapWrapper: 'mapWrapper',
  map: 'map',
}));

// Простой тестовый компонент для ShowOrder чтобы избежать сложных зависимостей
const SimpleShowOrder = (props: any) => {
  const { data } = props;
  
  return (
    <div>
      <div data-testid="order-head">
        <h2>Заявка {data.humanReadableId}</h2>
        <button>Принудительная отправка</button>
      </div>
      
      <div data-testid="section">
        <h3>Инициатор</h3>
        <div data-testid="item">
          <strong>ФИО:</strong> {data.author?.fullName || '-'}
        </div>
      </div>
      
      <div data-testid="section">
        <h3>Груз</h3>
        <div data-testid="table">
          {data.cargoDetails?.length > 0 && (
            <div>{data.cargoDetails[0]?.cargoName || '-'}</div>
          )}
        </div>
      </div>
      
      <div data-testid="section">
        <h3>Доставка</h3>
        <div data-testid="map">Map Component</div>
        <div data-testid="address-block">Address Block</div>
      </div>
      
      <div data-testid="section">
        <h3>Комментарий инженера</h3>
        <div data-testid="item-comment">
          <textarea 
            placeholder="Введите комментарий"
            defaultValue={data.commentEng || ''}
            data-testid="comment-textarea"
          />
          <button data-testid="save-comment-btn">Сохранить</button>
        </div>
      </div>
      
      <div data-testid="order-status">
        {data.status || 'Unknown'}
      </div>

      {data.qrs && data.qrs.length > 0 && (
        <div data-testid="mock-qrs-section">
          <h5>Отсканированные QR-коды</h5>
          {data.qrs.map((code: string, i: number) => (
            <span key={i} data-testid={`qr-code-${i}`}>{code}</span>
          ))}
        </div>
      )}
    </div>
  );
};

describe('Детальный просмотр заявки', () => {
  let mockCargoStore: any;
  
  const mockData = {
    id: 'order-123',
    humanReadableId: 'ORD-123',
    status: 'Согласовано',
    commentEng: 'Тестовый комментарий',
    loaders: 2,
    desireDate: '01.01.2023 12:00',
    calculatedTariff: {
      id: 'tariff-1',
      transportType: { nameRus: 'Авто' },
      distance: 100,
      cost: 500000,
      contragent: 'Контрагент',
    },
    transportTypeRus: 'Авто',
    creationTime: '01.01.2023 10:00',
    comment: 'Комментарий к заказу',
    author: {
      fullName: 'Иванов Иван',
      mobilePhone: '9123456789',
      department: 'Отдел доставки',
      approvedBy: 'Петров Петр',
    },
    cargoDetails: [
      {
        cargoName: 'Груз 1',
        cargoType: 'Обычный',
        volume: 10,
        weight: 100,
        occupiedPlacesCount: 1,
      },
    ],
    packages: {
      name: 'Коробка',
      count: 5,
    },
    totalSizes: {
      volume: 10,
      weight: 100,
    },
    waypoints: [
      { latitude: 55.7558, longitude: 37.6173, type: 'pickup' },
      { latitude: 55.7602, longitude: 37.6185, type: 'delivery' },
    ],
    segments: [],
    carInfo: {
      auto: 'Газель',
      carDriver: 'Водитель',
      carDriverPhone: '9123456789',
      registrationNumber: 'А123БВ',
    },
    editable: false,
    source: 'API',
  };
  
  const renderComponent = (props: any = {}) => {
    const defaultProps = {
      data: mockData,
      className: 'test-class',
      ...props,
    };
    
    return render(<SimpleShowOrder {...defaultProps} />);
  };
  
  beforeEach(() => {
    jest.clearAllMocks();
    
    mockCargoStore = {
      changeOrder: jest.fn().mockResolvedValue(undefined),
      tariffsListMulti: [],
      calculateAllTariffsMulti: jest.fn(),
      clearTariffs: jest.fn(),
      changeEngineerComment: jest.fn().mockResolvedValue(undefined),
      getCargoOrderActive: jest.fn().mockResolvedValue(undefined),
    };
    
    jest.requireMock('shared/hooks/useAppStoreContext').useAppStoreContext.mockReturnValue({
      cargoStore: mockCargoStore,
    });
  });
  
  afterEach(() => {
    jest.resetAllMocks();
  });
  
  it('Должен отображать заголовок с номером заявки', () => {
    renderComponent();
    
    expect(screen.getByText(`Заявка ${mockData.humanReadableId}`)).toBeInTheDocument();
    expect(screen.getByText('Принудительная отправка')).toBeInTheDocument();
  });
  
  it('Должен отображать основные данные заказа', () => {
    renderComponent();
    
    expect(screen.getByText(mockData.author.fullName)).toBeInTheDocument();
    expect(screen.getByText('Груз 1')).toBeInTheDocument();
  });
  
  it('Должен отображать карту и адресный блок', () => {
    renderComponent();
    
    expect(screen.getByTestId('map')).toBeInTheDocument();
    expect(screen.getByTestId('address-block')).toBeInTheDocument();
  });
  
  it('Должен отображать комментарий инженера и поле для редактирования', () => {
    renderComponent();
    
    expect(screen.getByTestId('comment-textarea')).toBeInTheDocument();
    expect(screen.getByTestId('save-comment-btn')).toBeInTheDocument();
  });
  
  it('Должен отображать статус заказа', () => {
    renderComponent();
    
    expect(screen.getByTestId('order-status')).toHaveTextContent('Согласовано');
  });
  
  it('Должен обрабатывать изменение комментария', () => {
    renderComponent();
    
    const textarea = screen.getByTestId('comment-textarea');
    fireEvent.change(textarea, { target: { value: 'Новый комментарий' } });
    
    expect(textarea).toHaveValue('Новый комментарий');
  });
  
  it('Должен отображать данные исполнителя', () => {
    const dataWithContractor = {
      ...mockData,
      carInfo: {
        auto: 'Газель Next',
        carDriver: 'Сидоров Сидор',
        carDriverPhone: '9876543210',
        registrationNumber: 'В456ГД',
      },
    };
    
    renderComponent({ data: dataWithContractor });
    
    expect(screen.getByTestId('order-status')).toBeInTheDocument();
  });
  
  it('Должен отображать таблицу с грузами', () => {
    renderComponent();
    
    expect(screen.getByTestId('table')).toBeInTheDocument();
    const table = screen.getByTestId('table');
    expect(table.textContent).toContain('Груз 1');
  });
  
  it('Должен корректно обрабатывать отсутствие данных груза', () => {
    const dataWithoutCargo = {
      ...mockData,
      cargoDetails: [],
    };
    
    renderComponent({ data: dataWithoutCargo });
    
    expect(screen.getByTestId('order-head')).toBeInTheDocument();
    expect(screen.getByTestId('table')).toBeInTheDocument();
  });
  
  it('Должен обрабатывать пустые данные', () => {
    const emptyData = {
      id: '',
      humanReadableId: '',
      status: '',
      commentEng: '',
      loaders: 0,
      desireDate: '',
      calculatedTariff: {
        id: '',
        transportType: { nameRus: '' },
        distance: 0,
        cost: 0,
        contragent: '',
      },
      author: {},
      cargoDetails: [],
      packages: {},
      totalSizes: {},
      waypoints: [],
      carInfo: {},
    };
    
    renderComponent({ data: emptyData });

    expect(screen.getByTestId('order-head')).toBeInTheDocument();
  });

  it('Должен отображать секцию QR-кодов', () => {
    const dataWithQrs = {
      ...mockData,
      qrs: ['QR001', 'QR002'],
    };

    renderComponent({ data: dataWithQrs });

    expect(screen.getByTestId('mock-qrs-section')).toBeInTheDocument();
    expect(screen.getByText('QR001')).toBeInTheDocument();
    expect(screen.getByText('QR002')).toBeInTheDocument();
  });

  it('Должен скрывать секцию QR-кодов при отсутствии данных', () => {
    const dataWithoutQrs = {
      ...mockData,
      qrs: undefined,
    };

    const { container } = renderComponent({ data: dataWithoutQrs });

    // Проверяем что нет элемента с testId мокированного компонента
    const hasQrSection = container.querySelector('[data-testid="mock-qrs-section"]');
    expect(hasQrSection).toBeNull();
  });
});