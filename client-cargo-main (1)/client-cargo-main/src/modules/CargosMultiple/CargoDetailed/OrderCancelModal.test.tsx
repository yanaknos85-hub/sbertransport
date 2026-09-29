import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import type { FormInstance } from 'antd/lib/form';
import { OrderCancelModal } from './OrderCancelModal';

// Мокаем Ant Design компоненты и Form с Item
jest.mock('antd', () => {
  const originalAntd = jest.requireActual('antd');
  
  // Мокаем Form.Item - рендерит только children
  const MockFormItem = ({ children }: { children: React.ReactNode }) => {
    return <div data-testid="mock-form-item">{children}</div>;
  };
  
  // Мокаем Form - возвращает функцию с Item
  const MockForm = Object.assign(
    (props: { children: React.ReactNode }) => <div data-testid="mock-form">{props.children}</div>,
    { Item: MockFormItem }
  );
  
  // Мокаем Modal с правильной обработкой visible/open
  const MockModal = ({
    children,
    title,
    visible,
    open,
    onOk,
    onCancel,
    okText,
    cancelText,
    okButtonProps,
    destroyOnClose,
    ...props
  }: {
    children: React.ReactNode;
    title?: React.ReactNode;
    visible?: boolean;
    open?: boolean;
    onOk?: () => void;
    onCancel?: () => void;
    okText?: string;
    cancelText?: string;
    okButtonProps?: { disabled?: boolean };
    destroyOnClose?: boolean;
    [key: string]: any;
  }) => {
    const isVisible = visible !== undefined ? visible : open;

    if (!isVisible) return null;

    return (
      <div data-testid="mock-modal" {...props}>
        {title && <h3 data-testid="mock-modal-title">{title}</h3>}
        {children}
        <button
          data-testid="mock-modal-ok-button"
          onClick={onOk}
          disabled={okButtonProps?.disabled}
        >
          {okText || 'OK'}
        </button>
        <button data-testid="mock-modal-cancel-button" onClick={onCancel}>
          {cancelText || 'Cancel'}
        </button>
      </div>
    );
  };
  
  // Мокаем Select с правильной обработкой onSelect
  const MockSelect = ({
    children,
    value,
    onChange,
    onSelect,
    placeholder,
    ...props
  }: {
    children?: React.ReactNode;
    value?: string;
    onChange?: (value: string) => void;
    onSelect?: (value: string) => void;
    placeholder?: string;
    [key: string]: any;
  }) => {
    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
      const newValue = e.target.value;
      if (onChange) {
        onChange(newValue);
      }
      if (onSelect) {
        onSelect(newValue);
      }
    };
    
    return (
      <select data-testid="mock-select" value={value} onChange={handleChange} placeholder={placeholder} {...props}>
        {children}
      </select>
    );
  };
  
  return {
    ...originalAntd,
    Modal: MockModal,
    Select: MockSelect,
    Form: MockForm,
  };
});

// Мокаем TextArea
jest.mock('antd/es/input/TextArea', () => {
  const TextArea = ({ onChange, ...props }: React.TextareaHTMLAttributes<HTMLTextAreaElement>) => {
    const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      if (onChange) {
        onChange(e);
      }
    };
    return <textarea data-testid="mock-textarea" onChange={handleChange} {...props} />;
  };
  return TextArea;
});

// Мокаем константы
jest.mock('constants/CargoRequestStatuses.constants', () => ({
  CANCEL_ORDER_OPTIONS: [
    { value: 'mistake', label: 'Ошибочно создана' },
    { value: 'change_date_time', label: 'Изменились дата/время' },
    { value: 'change_cargo', label: 'Изменился груз' },
    { value: 'other_reason', label: 'Другая причина' },
  ],
}));

// Мокаем стили
jest.mock('./styles.module.scss', () => ({
  textArea: 'textArea',
}));

describe('OrderCancelModal', () => {
  const mockForm = {
    getFieldDecorator: jest.fn(),
    getFieldValue: jest.fn(),
    setFieldsValue: jest.fn(),
    validateFields: jest.fn(),
  } as unknown as FormInstance;

  const defaultProps = {
    form: mockForm,
    reason: 'mistake',
    visible: true,
    textAreaVisible: false,
    onOk: jest.fn(),
    onCancel: jest.fn(),
    onSelect: jest.fn(),
    onChange: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('отображает модальное окно когда visible = true', () => {
    render(<OrderCancelModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal')).toBeInTheDocument();
    expect(screen.getByText('Отменить заявку')).toBeInTheDocument();
  });

  it('не отображает модальное окно когда visible = false', () => {
    render(<OrderCancelModal {...defaultProps} visible={false} />);

    expect(screen.queryByTestId('mock-modal')).not.toBeInTheDocument();
  });

  it('отображает заголовок модального окна', () => {
    render(<OrderCancelModal {...defaultProps} />);

    expect(screen.getByText('Отменить заявку')).toBeInTheDocument();
  });

  it('отображает кнопки Подтвердить и Отмена', () => {
    render(<OrderCancelModal {...defaultProps} />);

    expect(screen.getByText('Подтвердить')).toBeInTheDocument();
    expect(screen.getByText('Отмена')).toBeInTheDocument();
  });

  it('отображает Select с опциями отмены', () => {
    render(<OrderCancelModal {...defaultProps} />);

    expect(screen.getByPlaceholderText('Выберите причину отмены заявки')).toBeInTheDocument();
    expect(screen.getByTestId('mock-select')).toBeInTheDocument();
  });

  it('отображает TextArea когда textAreaVisible = true', () => {
    render(<OrderCancelModal {...defaultProps} textAreaVisible={true} />);

    expect(screen.getByPlaceholderText('Опишите причину отмены заявки')).toBeInTheDocument();
    expect(screen.getByTestId('mock-textarea')).toBeInTheDocument();
  });

  it('не отображает TextArea когда textAreaVisible = false', () => {
    render(<OrderCancelModal {...defaultProps} textAreaVisible={false} />);

    expect(screen.queryByPlaceholderText('Опишите причину отмены заявки')).not.toBeInTheDocument();
    expect(screen.queryByTestId('mock-textarea')).not.toBeInTheDocument();
  });

  it('вызывает onOk при нажатии на кнопку Подтвердить', () => {
    const onOk = jest.fn();
    render(<OrderCancelModal {...defaultProps} onOk={onOk} />);

    fireEvent.click(screen.getByText('Подтвердить'));
    expect(onOk).toHaveBeenCalledTimes(1);
  });

  it('вызывает onCancel при нажатии на кнопку Отмена', () => {
    const onCancel = jest.fn();
    render(<OrderCancelModal {...defaultProps} onCancel={onCancel} />);

    fireEvent.click(screen.getByText('Отмена'));
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('вызывает onSelect при выборе опции из Select', () => {
    const onSelect = jest.fn();
    render(<OrderCancelModal {...defaultProps} onSelect={onSelect} />);

    fireEvent.change(screen.getByPlaceholderText('Выберите причину отмены заявки'), {
      target: { value: 'change_date_time' },
    });

    expect(onSelect).toHaveBeenCalled();
  });

  it('вызывает onChange при изменении TextArea', () => {
    const onChange = jest.fn();
    render(<OrderCancelModal {...defaultProps} textAreaVisible={true} onChange={onChange} />);

    fireEvent.change(screen.getByTestId('mock-textarea'), {
      target: { value: 'Текст описания' },
    });

    expect(onChange).toHaveBeenCalledTimes(1);
  });

  it('блокирует кнопку Подтвердить когда reason пустой', () => {
    render(<OrderCancelModal {...defaultProps} reason="" />);

    const okButton = screen.getByText('Подтвердить');
    expect(okButton).toBeDisabled();
  });

  it('разблокирует кнопку Подтвердить когда reason заполнен', () => {
    render(<OrderCancelModal {...defaultProps} />);
    expect(screen.getByText('Подтвердить')).not.toBeDisabled();
  });

  it('передает правильные пропсы в Modal', () => {
    render(<OrderCancelModal {...defaultProps} />);

    const modal = screen.getByTestId('mock-modal');
    expect(modal).toBeInTheDocument();
  });

  it('уничтожает DOM элементы при закрытии модального окна', () => {
    const { rerender } = render(<OrderCancelModal {...defaultProps} />);

    rerender(<OrderCancelModal {...defaultProps} visible={false} />);
    expect(screen.queryByTestId('mock-modal')).not.toBeInTheDocument();
  });
});
