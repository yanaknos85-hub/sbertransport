import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ShowOrder from './ShowOrder';
import { useGetSearchMonitorRoute, useUpdateLoaders } from 'api/planner';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { notification } from 'antd';

// Мокаем зависимости
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn(() => ({ id: 'TEST-123' })),
}));

jest.mock('api/planner', () => ({
  useGetSearchMonitorRoute: jest.fn(),
  useUpdateLoaders: jest.fn(),
}));

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: jest.fn(),
}));

jest.mock('antd', () => ({
  notification: {
    success: jest.fn(),
    error: jest.fn(),
  },
  Button: ({ children, onClick, type, htmlType }: any) => (
    <button onClick={onClick} data-type={type} data-html-type={htmlType}>
      {children}
    </button>
  ),
}));

jest.mock('modules/Engineers/constants/Engineers.constants', () => ({
  FORMAT: 'DD.MM.YYYY HH:mm',
}));

// Мокаем константы статусов
jest.mock('../constants', () => {
  const StatusNames = {
    CARGO_PLANNING: 'CARGO_PLANNING',
    CARGO_PLANNING_FINISHED: 'CARGO_PLANNING_FINISHED',
    PLANNING: 'PLANNING',
    COMPLETED: 'COMPLETED',
  };

  const STATUSES = [
    { rusName: 'CARGO_PLANNING' },
    { rusName: 'CARGO_PLANNING_FINISHED' },
    { rusName: 'PLANNING' },
    { rusName: 'COMPLETED' },
  ];

  return {
    __esModule: true,
    default: {
      humanReadableId: 'ID маршрута',
      status: 'Статус маршрута',
      creationTime: 'Дата и время создания',
      departureTime: 'Дата и время отправления',
      shipmentTime: 'Фактическая дата и время доставки',
      author: 'Инициатор',
      contractorName: 'Контрагент',
      driver: 'ФИО водителя',
      driverPhone: 'Мобильный телефон водителя',
      registrationNumber: 'Регистрационный номер автомобиля',
      capacity: 'Грузоподъемность автомобиля, кг',
      autoVolume: 'Объем, м³',
      additionalServices: 'Дополнительные услуги',
      loaders: 'Грузчики',
      applications: 'Заявки',
      cargoType: 'Вид груза',
      occupiedPlacesCount: 'Мест',
      weightOfOne: 'Масса',
      volumeOfOne: 'Объем',
      desiredDate: 'Планируемая дата и время погрузки',
      shippingDate: 'Планируемая дата и время доставки',
      factDesiredDate: 'Фактическая дата и время погрузки',
      factShippingDate: 'Фактическая дата и время доставки',
      additionalInfo: 'Дополнительная информация',
      cost: 'Планируемая стоимость, руб',
      actualCost: 'Фактическая стоимость, руб',
      weight: 'Общий вес, кг',
      volume: 'Общий объем, м³',
      distance: 'Планируемый километраж, км',
      actualDistance: 'Фактический километраж, км',
      creationType: 'Тип создания маршрута',
      contractor: 'Перевозчик',
    },
    STATUSES,
    StatusNames,
  };
});

jest.mock('utils', () => ({
  convertToRubles: jest.fn((value) => value || 0),
  formatPhoneNumber: jest.fn((phone) => phone || ''),
  formatVolume: jest.fn((volume) => volume?.toString() || ''),
  ignore: jest.fn(),
}));

jest.mock('constants/constants.app', () => ({
  emptySign: '—',
}));

// Мокаем дочерние компоненты
jest.mock('./Header', () => ({ children, caption }: any) => (
  <div>
    <h2>{caption}</h2>
    {children}
  </div>
));
jest.mock('./OrderStatus', () => () => <div>Status</div>);
jest.mock('./AddressBlock/AddressBlock', () => () => <div>Address Block</div>);
jest.mock('./EditButton', () => ({
  EditButton: ({ editMode, setIsEditMode, activeEdit }: any) => {
    if (!activeEdit) return null;
    return (
      <button onClick={() => setIsEditMode(!editMode)}>
        {editMode ? 'Отменить' : 'Редактировать'}
      </button>
    );
  },
}));
jest.mock('./LoadersCount/LoadersCount', () => ({
  LoadersCount: ({ value, onChange }: any) => (
    <input
      type="number"
      value={value}
      onChange={(e) => onChange(Number(e.target.value))}
      data-testid="loaders-input"
    />
  ),
}));
jest.mock('./Item', () => ({
  Item: ({ title, children }: any) => (
    <div data-testid="item">
      <span>{title}:</span>
      <span>{children}</span>
    </div>
  ),
  Section: ({ title, children }: any) => (
    <div data-testid="section">
      <h3>{title}</h3>
      {children}
    </div>
  ),
  SubItem: ({ title, children }: any) => (
    <div data-testid="subitem">
      <span>{title}:</span>
      <span>{children}</span>
    </div>
  ),
  SubSection: ({ title, children }: any) => (
    <div data-testid="subsection">
      <h4>{title}</h4>
      {children}
    </div>
  ),
}));

describe('ShowOrder', () => {
  const mockData = {
    id: '123',
    humanReadableId: 'TEST-123',
    status: 'PLANNING',
    creationTime: '2024-01-01T10:00:00',
    desiredDate: '2024-01-02T10:00:00',
    actualShipmentTime: '2024-01-03T10:00:00',
    author: 'Test Author',
    contractorName: 'Test Contractor',
    driver: 'Test Driver',
    driverPhone: '+79991234567',
    registrationNumber: 'A123BC',
    capacity: '10т',
    autoVolume: '20м³',
    loaders: 2,
    requestsForOto: [
      {
        id: '1',
        humanReadableId: 'ORDER-001',
        addressFrom: 'Moscow',
        addressTo: 'Saint Petersburg',
        cargoType: 'Electronics',
        occupiedPlacesCount: 5,
        weight: 100.5,
        volume: 2.5,
        desiredDate: '2024-01-02T10:00:00',
        deliveryTimeDate: '2024-01-03T10:00:00',
        transferTime: '2024-01-04T10:00:00',
        shipmentTime: '2024-01-05T10:00:00',
      }
    ],
    cost: 15000,
    actualCost: 16000,
    weight: 500.75,
    volume: 10.5,
    distance: 150.5,
    actualDistance: 160.5,
    creationType: 'Manual',
  };

  const mockRefetch = jest.fn();
  const mockUpdateLoaders = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();

    (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
      data: mockData,
      refetch: mockRefetch,
    });

    (useUpdateLoaders as jest.Mock).mockReturnValue([mockUpdateLoaders]);

    (useAppStoreContext as jest.Mock).mockReturnValue({
      cargoStore: {
        sendRouteToContractor: jest.fn().mockResolvedValue({}),
      },
    });
  });

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <ShowOrder />
      </BrowserRouter>
    );
  };

  describe('Базовый рендеринг', () => {
    it('рендерит заголовок с ID маршрута', () => {
      renderComponent();
      expect(screen.getByText('Маршрут TEST-123')).toBeInTheDocument();
    });

    it('рендерит кнопку "Отправить контрагенту"', () => {
      renderComponent();
      expect(screen.getByText('Отправить контрагенту')).toBeInTheDocument();
    });

    it('рендерит основную информацию о маршруте', () => {
      renderComponent();
      expect(screen.getByText('TEST-123')).toBeInTheDocument();
      expect(screen.getByText('Test Author')).toBeInTheDocument();
    });

    it('рендерит информацию о контрагенте', () => {
      renderComponent();
      expect(screen.getByText('Test Contractor')).toBeInTheDocument();
      expect(screen.getByText('Test Driver')).toBeInTheDocument();
      expect(screen.getByText('+79991234567')).toBeInTheDocument();
      expect(screen.getByText('A123BC')).toBeInTheDocument();
    });

    it('рендерит количество грузчиков', () => {
      renderComponent();
      expect(screen.getByText('2')).toBeInTheDocument();
    });

    it('рендерит список заявок', () => {
      renderComponent();
      expect(screen.getByText('1. ORDER-001')).toBeInTheDocument();
      expect(screen.getByText('Electronics')).toBeInTheDocument();
    });

    it('рендерит дополнительную информацию', () => {
      renderComponent();
      expect(screen.getByText('15000,00')).toBeInTheDocument();
      expect(screen.getByText('16000,00')).toBeInTheDocument();
    });
  });

  describe('Отображение пустых значений', () => {
    it('отображает пустой знак при отсутствии данных', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          contractorName: null,
          driver: null,
          driverPhone: null,
          registrationNumber: null,
          actualCost: null,
          weight: null,
          volume: null,
        },
        refetch: mockRefetch,
      });

      renderComponent();
      const emptySigns = screen.getAllByText('—');
      expect(emptySigns.length).toBeGreaterThan(0);
    });
  });

  describe('Редактирование количества грузчиков', () => {
    it('показывает кнопку редактирования для статуса CARGO_PLANNING', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      expect(screen.getByText('Редактировать')).toBeInTheDocument();
    });

    it('показывает кнопку редактирования для статуса CARGO_PLANNING_FINISHED', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING_FINISHED',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      expect(screen.getByText('Редактировать')).toBeInTheDocument();
    });

    it('не показывает кнопку редактирования для статуса PLANNING', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      expect(screen.queryByText('Редактировать')).not.toBeInTheDocument();
    });

    it('не показывает кнопку редактирования для статуса COMPLETED', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'COMPLETED',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      expect(screen.queryByText('Редактировать')).not.toBeInTheDocument();
    });

    it('включает режим редактирования при клике на кнопку', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      const editButton = screen.getByText('Редактировать');
      fireEvent.click(editButton);

      expect(screen.getByTestId('loaders-input')).toBeInTheDocument();
      expect(screen.getByText('Отмена')).toBeInTheDocument();
      expect(screen.getByText('Сохранить')).toBeInTheDocument();
    });

    it('изменяет значение грузчиков', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      fireEvent.click(screen.getByText('Редактировать'));

      const input = screen.getByTestId('loaders-input');
      fireEvent.change(input, { target: { value: '5' } });

      expect(input).toHaveValue(5);
    });

    it('отменяет редактирование и возвращает исходное значение', () => {
      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      fireEvent.click(screen.getByText('Редактировать'));

      const input = screen.getByTestId('loaders-input');
      fireEvent.change(input, { target: { value: '5' } });

      fireEvent.click(screen.getByText('Отмена'));

      expect(screen.getByText('2')).toBeInTheDocument();
      expect(screen.queryByTestId('loaders-input')).not.toBeInTheDocument();
    });

    it('сохраняет изменения', async () => {
      mockUpdateLoaders.mockResolvedValue({});

      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      fireEvent.click(screen.getByText('Редактировать'));

      const input = screen.getByTestId('loaders-input');
      fireEvent.change(input, { target: { value: '5' } });

      fireEvent.click(screen.getByText('Сохранить'));

      await waitFor(() => {
        expect(mockUpdateLoaders).toHaveBeenCalledWith({
          requestId: 'TEST-123',
          loaders: 5,
        });
      });
    });

    it('обрабатывает ошибку при сохранении', async () => {
      const error = new Error('Save error');
      mockUpdateLoaders.mockRejectedValue(error);

      (useGetSearchMonitorRoute as jest.Mock).mockReturnValue({
        data: {
          ...mockData,
          status: 'CARGO_PLANNING',
        },
        refetch: mockRefetch,
      });

      renderComponent();
      fireEvent.click(screen.getByText('Редактировать'));

      const input = screen.getByTestId('loaders-input');
      fireEvent.change(input, { target: { value: '5' } });

      fireEvent.click(screen.getByText('Сохранить'));

      await waitFor(() => {
        expect(mockUpdateLoaders).toHaveBeenCalled();
      });
    });
  });

  describe('Отправка маршрута контрагенту', () => {
    it('отправляет маршрут и показывает успешное уведомление', async () => {
      const mockSendRoute = jest.fn().mockResolvedValue({});
      (useAppStoreContext as jest.Mock).mockReturnValue({
        cargoStore: {
          sendRouteToContractor: mockSendRoute,
        },
      });

      renderComponent();
      fireEvent.click(screen.getByText('Отправить контрагенту'));

      await waitFor(() => {
        expect(mockSendRoute).toHaveBeenCalledWith(mockData.id);
        expect(notification.success).toHaveBeenCalledWith({
          message: 'Маршрут TEST-123 отправлен контрагенту',
        });
      });
    });

    it('вызывает refetch через 2 секунды после успешной отправки', async () => {
      jest.useFakeTimers();
      const mockSendRoute = jest.fn().mockResolvedValue({});
      (useAppStoreContext as jest.Mock).mockReturnValue({
        cargoStore: {
          sendRouteToContractor: mockSendRoute,
        },
      });

      renderComponent();
      fireEvent.click(screen.getByText('Отправить контрагенту'));

      await waitFor(() => {
        expect(mockSendRoute).toHaveBeenCalled();
      });

      jest.advanceTimersByTime(2000);
      expect(mockRefetch).toHaveBeenCalled();

      jest.useRealTimers();
    });

    it('показывает ошибку при неудачной отправке', async () => {
      const errorMessage = 'Ошибка отправки';
      const mockSendRoute = jest.fn().mockRejectedValue({
        response: { data: { message: errorMessage } },
      });

      (useAppStoreContext as jest.Mock).mockReturnValue({
        cargoStore: {
          sendRouteToContractor: mockSendRoute,
        },
      });

      renderComponent();
      fireEvent.click(screen.getByText('Отправить контрагенту'));

      await waitFor(() => {
        expect(notification.error).toHaveBeenCalledWith({
          message: errorMessage,
        });
      });
    });
  });
});
