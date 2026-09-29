/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import FormItem from '.';

// Мокаем зависимости
jest.mock('antd', () => ({
  Form: {
    Item: jest.fn(({ children, className, validateStatus, colon, labelCol, wrapperCol, ...props }) => (
      <div 
        data-testid="form-item" 
        className={className}
        data-validate-status={validateStatus}
        data-colon={colon}
        data-label-col={JSON.stringify(labelCol)}
        data-wrapper-col={JSON.stringify(wrapperCol)}
        {...props}
      >
        {children}
      </div>
    )),
  },
}));

jest.mock('./formItem.module.scss', () => ({
  formItem: 'mock-form-item-class',
}));

describe('FormItem', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('должен рендерить Form.Item', () => {
    render(<FormItem>Test Content</FormItem>);
    expect(screen.getByTestId('form-item')).toBeInTheDocument();
  });

  it('должен применять стили из модуля', () => {
    render(<FormItem>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveClass('mock-form-item-class');
  });

  it('должен рендерить children', () => {
    render(
      <FormItem>
        <input data-testid="child-input" />
      </FormItem>
    );
    expect(screen.getByTestId('child-input')).toBeInTheDocument();
  });

  it('должен пробрасывать стандартные пропсы в Form.Item', () => {
    render(
      <FormItem
        name="username"
        label="Username"
        help="Required field"
      >
        <input />
      </FormItem>
    );
    
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('name', 'username');
    expect(formItem).toHaveAttribute('label', 'Username');
    expect(formItem).toHaveAttribute('help', 'Required field');
  });

  it('должен пробрасывать validateStatus через data-атрибут', () => {
    render(
      <FormItem validateStatus="error">
        <input />
      </FormItem>
    );
    
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('data-validate-status', 'error');
  });

  it('должен пробрасывать required', () => {
    render(<FormItem required>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('required', '');
  });

  it('должен пробрасывать colon через data-атрибут', () => {
    render(<FormItem colon={false}>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('data-colon', 'false');
  });

  it('должен пробрасывать labelCol через data-атрибут', () => {
    const labelCol = { span: 6 };
    render(<FormItem labelCol={labelCol}>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('data-label-col', JSON.stringify(labelCol));
  });

  it('должен пробрасывать wrapperCol через data-атрибут', () => {
    const wrapperCol = { span: 18 };
    render(<FormItem wrapperCol={wrapperCol}>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('data-wrapper-col', JSON.stringify(wrapperCol));
  });

  it('должен пробрасывать tooltip', () => {
    render(<FormItem tooltip="Helpful tooltip">Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('tooltip', 'Helpful tooltip');
  });

  it('должен пробрасывать extra', () => {
    render(<FormItem extra="Additional info">Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('extra', 'Additional info');
  });

  it('должен пробрасывать id', () => {
    render(<FormItem id="custom-id">Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('id', 'custom-id');
  });

  it('должен обрабатывать пустые пропсы', () => {
    render(<FormItem>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toBeInTheDocument();
  });

  it('должен обрабатывать undefined children', () => {
    render(<FormItem>{undefined}</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toBeInTheDocument();
    expect(formItem).toBeEmptyDOMElement();
  });

  it('должен пробрасывать data-атрибуты', () => {
    render(<FormItem data-testid="custom-test" data-custom="value">Content</FormItem>);
    const formItem = screen.getByTestId('custom-test');
    expect(formItem).toHaveAttribute('data-custom', 'value');
  });

  it('должен пробрасывать style', () => {
    render(<FormItem style={{ marginBottom: '24px', color: 'red' }}>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveStyle({ marginBottom: '24px', color: 'red' });
  });

  it('должен пробрасывать hidden', () => {
    render(<FormItem hidden>Content</FormItem>);
    const formItem = screen.getByTestId('form-item');
    expect(formItem).toHaveAttribute('hidden', '');
  });
});