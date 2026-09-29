import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';

// Мокаем Ant Design Modal
jest.mock('antd', () => {
  const originalAntd = jest.requireActual('antd');

  const MockModal = ({
    children,
    title,
    open,
    onOk,
    onCancel,
    okText,
    cancelText,
    centered,
    closable,
    keyboard,
    width,
    className,
    ...props
  }: {
    children: React.ReactNode;
    title?: React.ReactNode;
    open?: boolean;
    onOk?: () => void;
    onCancel?: () => void;
    okText?: string;
    cancelText?: string;
    centered?: boolean;
    closable?: boolean;
    keyboard?: boolean;
    width?: number;
    className?: string;
    [key: string]: any;
  }) => {
    if (!open) return null;

    return (
      <div data-testid="mock-modal" className={className} {...props}>
        {title && <div data-testid="mock-modal-title">{title}</div>}
        <div data-testid="mock-modal-body">{children}</div>
        <button
          data-testid="mock-modal-ok-button"
          onClick={onOk}
        >
          {okText || 'OK'}
        </button>
        <button
          data-testid="mock-modal-cancel-button"
          onClick={onCancel}
        >
          {cancelText || 'Cancel'}
        </button>
      </div>
    );
  };

  return {
    ...originalAntd,
    Modal: MockModal,
  };
});

// Мокаем стили
jest.mock('../static/Cargos.module.scss', () => ({
  modal: 'modal',
}));

// Импортируем компонент ПОСЛЕ всех моков
import CancelModal from './CancelModal';

describe('CancelModal', () => {
  const mockOnCancel = jest.fn();
  const mockCancelOrder = jest.fn();

  const defaultProps = {
    visible: true,
    onCancel: mockOnCancel,
    cancelOrder: mockCancelOrder,
    title: 'Отменить заявку',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('отображает модальное окно когда visible = true', () => {
    render(<CancelModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal')).toBeInTheDocument();
  });

  it('не отображает модальное окно когда visible = false', () => {
    render(<CancelModal {...defaultProps} visible={false} />);

    expect(screen.queryByTestId('mock-modal')).not.toBeInTheDocument();
  });

  it('отображает заголовок модального окна', () => {
    render(<CancelModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal-title')).toHaveTextContent('Отменить заявку');
  });

  it('отображает текст предупреждения', () => {
    render(<CancelModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal-body')).toHaveTextContent(
      'После удаления запрос на доставку будет отменён, а списанные средства вернутся обратно на счёт лимита.'
    );
  });

  it('отображает кнопки Удалить и Отмена', () => {
    render(<CancelModal {...defaultProps} />);

    expect(screen.getByText('Удалить')).toBeInTheDocument();
    expect(screen.getByText('Отмена')).toBeInTheDocument();
  });

  it('вызывает cancelOrder при нажатии на кнопку Удалить', () => {
    render(<CancelModal {...defaultProps} />);

    fireEvent.click(screen.getByText('Удалить'));
    expect(mockCancelOrder).toHaveBeenCalledTimes(1);
  });

  it('вызывает onCancel при нажатии на кнопку Отмена', () => {
    render(<CancelModal {...defaultProps} />);

    fireEvent.click(screen.getByText('Отмена'));
    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('применяет правильный className к модальному окну', () => {
    render(<CancelModal {...defaultProps} />);

    expect(screen.getByTestId('mock-modal')).toHaveClass('modal');
  });

  it('уничтожает DOM элементы при закрытии модального окна', () => {
    const { rerender } = render(<CancelModal {...defaultProps} />);

    rerender(<CancelModal {...defaultProps} visible={false} />);
    expect(screen.queryByTestId('mock-modal')).not.toBeInTheDocument();
  });
});
