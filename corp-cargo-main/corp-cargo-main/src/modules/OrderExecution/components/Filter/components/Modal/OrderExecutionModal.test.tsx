/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import Modal from '.';

// Мокаем зависимости
jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => args.filter(Boolean).join(' ')),
}));

jest.mock('antd', () => ({
  Modal: jest.fn(({ children, closeIcon, className, visible, centered, confirmLoading, maskClosable, keyboard, zIndex, wrapClassName, ...props }) => (
    <div 
      data-testid="antd-modal" 
      className={className}
      data-visible={visible}
      data-centered={centered}
      data-confirm-loading={confirmLoading}
      data-mask-closable={maskClosable}
      data-keyboard={keyboard}
      data-z-index={zIndex}
      data-wrap-class-name={wrapClassName}
      {...props}
    >
      <div data-testid="close-icon-container">{closeIcon}</div>
      <div data-testid="modal-content">{children}</div>
    </div>
  )),
}));

jest.mock('../../../Icon/CloseIcon', () => {
  return function MockCloseIcon() {
    return <span data-testid="close-icon">✗</span>;
  };
});

jest.mock('./modal.module.scss', () => ({
  filterModal: 'mock-modal-class',
}));

describe('Modal', () => {
  const mockClassName = jest.requireMock('classnames').default;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('должен рендерить модальное окно', () => {
    render(<Modal visible>Content</Modal>);
    expect(screen.getByTestId('antd-modal')).toBeInTheDocument();
  });

  it('должен рендерить children', () => {
    render(<Modal visible>Test Content</Modal>);
    expect(screen.getByTestId('modal-content')).toHaveTextContent('Test Content');
  });

  it('должен применять стили из модуля', () => {
    render(<Modal visible>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveClass('mock-modal-class');
  });

  it('должен объединять переданный className с стилями модуля', () => {
    render(<Modal visible className="custom-class">Content</Modal>);
    expect(mockClassName).toHaveBeenCalledWith('mock-modal-class', 'custom-class');
  });

  it('должен отображать кастомную иконку закрытия', () => {
    render(<Modal visible>Content</Modal>);
    expect(screen.getByTestId('close-icon')).toBeInTheDocument();
  });

  it('должен вызывать onClose при клике на иконку закрытия', () => {
    const onCloseMock = jest.fn();
    render(<Modal visible onClose={onCloseMock}>Content</Modal>);
    
    const closeIcon = screen.getByTestId('close-icon').parentElement;
    fireEvent.click(closeIcon!);
    
    expect(onCloseMock).toHaveBeenCalledTimes(1);
  });

  it('должен пробрасывать visible через data-атрибут', () => {
    render(<Modal visible>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-visible', 'true');
  });

  it('должен пробрасывать title', () => {
    render(<Modal visible title="Modal Title">Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('title', 'Modal Title');
  });

  it('должен пробрасывать width', () => {
    render(<Modal visible width={600}>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('width', '600');
  });

  it('должен пробрасывать centered через data-атрибут', () => {
    render(<Modal visible centered>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-centered', 'true');
  });

  it('должен пробрасывать confirmLoading через data-атрибут', () => {
    render(<Modal visible confirmLoading>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-confirm-loading', 'true');
  });

  it('должен пробрасывать maskClosable через data-атрибут', () => {
    render(<Modal visible maskClosable={false}>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-mask-closable', 'false');
  });

  it('должен пробрасывать keyboard через data-атрибут', () => {
    render(<Modal visible keyboard={false}>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-keyboard', 'false');
  });

  it('должен пробрасывать zIndex через data-атрибут', () => {
    render(<Modal visible zIndex={1000}>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-z-index', '1000');
  });

  it('должен работать без onClose', () => {
    render(<Modal visible>Content</Modal>);
    const closeIcon = screen.getByTestId('close-icon').parentElement;
    fireEvent.click(closeIcon!);
    expect(screen.getByTestId('antd-modal')).toBeInTheDocument();
  });

  it('должен пробрасывать wrapClassName через data-атрибут', () => {
    render(<Modal visible wrapClassName="custom-wrap">Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveAttribute('data-wrap-class-name', 'custom-wrap');
  });

  it('должен пробрасывать style', () => {
    render(<Modal visible style={{ top: 100 }}>Content</Modal>);
    const modal = screen.getByTestId('antd-modal');
    expect(modal).toHaveStyle({ top: '100px' });
  });
});