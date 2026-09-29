import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// Мокаем Field
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

// Мокаем валидацию
jest.mock('shared/fieldValidationRules', () => ({
  ValidationRules: {
    general: {
      maxLength: (max: number) => ({ max, message: `Максимум ${max} символов` }),
    },
  },
}));

// Мокаем типы
jest.mock('stores/Trip/Trip.interface', () => ({}));

// Мокаем SVG компонент
jest.mock('../../Evaluation/static/icons/closeIcon.svg', () => {
  const ReactComponent = () => <svg data-testid="mock-close-icon" />;
  return ReactComponent;
});

// Мокаем custom компоненты
jest.mock('shared/components/Cargo/TTypography', () => {
  const TTypography = ({ children, ...props }: { children: React.ReactNode; [key: string]: any }) => {
    return <div data-testid="mock-typergraphy" {...props}>{children}</div>;
  };
  return TTypography;
});

jest.mock('shared/components/SchedulerMulti/RowButtons/RowButtons', () => {
  const RowButtons = (props: { onChange?: (values: number[]) => void; values: number[]; names?: string[]; label?: string; [key: string]: any }) => {
    const { onChange, values, names } = props;

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = parseInt(e.target.value, 10);
      const currentIndex = values.indexOf(value);
      let newValues: number[];

      if (currentIndex === -1) {
        newValues = [...values, value];
      } else {
        newValues = values.filter((v) => v !== value);
      }

      if (onChange) {
        onChange(newValues);
      }
    };

    return (
      <div data-testid="mock-row-buttons">
        {names?.map((name: string, index: number) => (
          <label key={index}>
            <input
              type="checkbox"
              value={values[index]}
              checked={values.includes(values[index])}
              onChange={handleChange}
              data-testid={`mock-row-button-${index}`}
            />
            {name}
          </label>
        ))}
      </div>
    );
  };
  return RowButtons;
});

jest.mock('shared/form/FormField/FormField', () => {
  const FormField = ({ value, onChange, ...props }: { value: string; onChange?: (e: React.ChangeEvent<HTMLTextAreaElement>) => void; [key: string]: any }) => {
    const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      if (onChange) {
        onChange(e);
      }
    };

    return (
      <textarea
        data-testid="mock-form-field"
        value={value}
        onChange={handleChange}
        {...props}
      />
    );
  };
  return FormField;
});

jest.mock('shared/form/CargoType/CargoType.style', () => {
  const Modal = ({
    children,
    title,
    open,
    onOk,
    onCancel,
    afterClose,
    okText,
    okButtonProps,
    footer,
    closeIcon,
    destroyOnClose,
    ...props
  }: {
    children: React.ReactNode;
    title?: React.ReactNode;
    open?: boolean;
    onOk?: () => void;
    onCancel?: () => void;
    afterClose?: () => void;
    okText?: string;
    okButtonProps?: { disabled?: boolean; danger?: boolean };
    footer?: React.ReactNode;
    closeIcon?: React.ReactNode;
    destroyOnClose?: boolean;
    [key: string]: any;
  }) => {
    if (!open) return null;

    return (
      <div data-testid="mock-modal" {...props}>
        <div data-testid="mock-modal-title">{title}</div>
        {children}
        {footer}
        <button
          data-testid="mock-modal-ok-button"
          onClick={onOk}
          disabled={okButtonProps?.disabled}
        >
          {okText || 'OK'}
        </button>
        <button data-testid="mock-modal-cancel-button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    );
  };

  return { Modal };
});

// Импортируем компонент ПОСЛЕ всех моков
import ApprovalReasonModal from './ApprovalReasonModal';

describe('ApprovalReasonModal', () => {
  const mockOnOk = jest.fn();
  const mockOnCancel = jest.fn();

  const defaultProps = {
    id: 'test-id',
    visible: true,
    onOk: mockOnOk,
    onCancel: mockOnCancel,
    isApproval: true,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('отображает модальное окно когда visible = true', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal')).toBeInTheDocument();
  });

  it('не отображает модальное окно когда visible = false', () => {
    render(<ApprovalReasonModal {...defaultProps} visible={false} />);

    expect(screen.queryByTestId('mock-modal')).not.toBeInTheDocument();
  });

  it('отображает заголовок "Отклонить?" по умолчанию', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal-title')).toHaveTextContent('Отклонить?');
  });

  it('отображает заголовок "Отменить?" когда isJournal = true и isApproval = false', () => {
    render(<ApprovalReasonModal {...defaultProps} isJournal={true} isApproval={false} />);

    expect(screen.getByTestId('mock-modal-title')).toHaveTextContent('Отменить?');
  });

  it('отображает заголовок "Отменить?" когда isApproval = false', () => {
    render(<ApprovalReasonModal {...defaultProps} isApproval={false} />);

    expect(screen.getByTestId('mock-modal-title')).toHaveTextContent('Отменить?');
  });

  it('отображает текст приглашения для отклонения', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    expect(screen.getByText(/Пожалуйста, опишите причину отклонения заявки/i)).toBeInTheDocument();
  });

  it('отображает текст приглашения для отмены', () => {
    render(<ApprovalReasonModal {...defaultProps} isJournal={true} isApproval={false} />);

    expect(screen.getByText(/Пожалуйста, опишите причину отмены заявки/i)).toBeInTheDocument();
  });

  it('отображает RowButtons с вариантами причин', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    expect(screen.getByTestId('mock-row-buttons')).toBeInTheDocument();
    expect(screen.getByText('Нет лимита')).toBeInTheDocument();
    expect(screen.getByText('Ошибка')).toBeInTheDocument();
    expect(screen.getByText('Изменение данных')).toBeInTheDocument();
  });

  it('отображает TextArea для комментария', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    expect(screen.getByTestId('mock-form-field')).toBeInTheDocument();
  });

  it('вызывает onCancel при нажатии на кнопку "Cancel"', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    fireEvent.click(screen.getByTestId('mock-modal-cancel-button'));
    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('очищает состояние при закрытии модального окна', () => {
    const { rerender } = render(<ApprovalReasonModal {...defaultProps} />);

    const textarea = screen.getByTestId('mock-form-field');
    fireEvent.change(textarea, { target: { value: 'Текст' } });

    rerender(<ApprovalReasonModal {...defaultProps} visible={false} />);

    rerender(<ApprovalReasonModal {...defaultProps} visible={true} />);

    expect(screen.getByTestId('mock-form-field')).toHaveValue('');
  });

  it('вызывает onCancel и очищает tagReason при нажатии на кнопку "Cancel"', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    const checkbox = screen.getByTestId('mock-row-button-0');
    fireEvent.change(checkbox, { target: { checked: true } });

    expect(screen.getByTestId('mock-row-button-0')).toBeChecked();

    fireEvent.click(screen.getByTestId('mock-modal-cancel-button'));

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('отображает текст "Комментарий" перед TextArea', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    expect(screen.getByText('Комментарий')).toBeInTheDocument();
  });

  it('передает правильные пропсы в Modal', () => {
    render(<ApprovalReasonModal {...defaultProps} />);

    const modal = screen.getByTestId('mock-modal');
    expect(modal).toBeInTheDocument();
  });
});
