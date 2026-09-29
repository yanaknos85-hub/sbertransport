/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, within } from '@testing-library/react';
import '@testing-library/jest-dom';

import { HeaderWithIcon } from './HeaderWithIcon';

jest.mock('@ant-design/icons', () => ({
  InfoCircleFilled: ({ style }: any) => (
    <span data-testid="info-icon" data-cursor={style?.cursor} />
  ),
}));

jest.mock('antd', () => ({
  Tooltip: ({ children, title }: any) => {
    const titleAttr =
      typeof title === 'string'
        ? title
        : Array.isArray(title)
          ? title.join('|')
          : null;
    return (
      <div data-testid="tooltip" data-title={titleAttr}>
        {children}
        <div data-testid="tooltip-title">{title}</div>
      </div>
    );
  },
}));

describe('HeaderWithIcon', () => {
  test('должен рендерить переданный title', () => {
    render(<HeaderWithIcon title="Номер ЭТрН" />);
    expect(screen.getByText('Номер ЭТрН')).toBeInTheDocument();
  });

  test('без tooltip не должен рендерить Tooltip', () => {
    render(<HeaderWithIcon title="Заголовок" />);
    expect(screen.queryByTestId('tooltip')).not.toBeInTheDocument();
    expect(screen.getByTestId('info-icon')).toBeInTheDocument();
  });

  test('с tooltip-строкой должен передавать её в Tooltip как есть', () => {
    render(<HeaderWithIcon title="Заголовок" tooltip="Подсказка" />);
    const tooltip = screen.getByTestId('tooltip');
    expect(tooltip).toBeInTheDocument();
    expect(tooltip).toHaveAttribute('data-title', 'Подсказка');
  });

  test('с tooltip-массивом должен рендерить каждый элемент в отдельном div', () => {
    render(<HeaderWithIcon title="Заголовок" tooltip={['строка 1', 'строка 2']} />);
    const tooltip = screen.getByTestId('tooltip');
    expect(tooltip).toBeInTheDocument();

    const tooltipTitle = screen.getByTestId('tooltip-title');
    expect(tooltipTitle).toHaveTextContent('строка 1');
    expect(tooltipTitle).toHaveTextContent('строка 2');
  });

  test('с tooltip как ReactNode должен рендерить узел как есть', () => {
    render(
      <HeaderWithIcon
        title="Заголовок"
        tooltip={<span data-testid="custom-tooltip">JSX контент</span>}
      />,
    );
    const tooltipTitle = screen.getByTestId('tooltip-title');
    expect(tooltipTitle).toBeInTheDocument();
    expect(within(tooltipTitle).getByTestId('custom-tooltip')).toBeInTheDocument();
    expect(within(tooltipTitle).getByText('JSX контент')).toBeInTheDocument();
  });

  test('иконка без tooltip получает cursor: help', () => {
    render(<HeaderWithIcon title="Заголовок" />);
    expect(screen.getByTestId('info-icon')).toHaveAttribute('data-cursor', 'help');
  });

  test('иконка с tooltip получает cursor: help', () => {
    render(<HeaderWithIcon title="Заголовок" tooltip="t" />);
    expect(screen.getByTestId('info-icon')).toHaveAttribute('data-cursor', 'help');
  });

  test('tooltip-массив должен рендерить все элементы по порядку', () => {
    render(<HeaderWithIcon title="Заголовок" tooltip={['A', 'B', 'C']} />);
    const tooltipTitle = screen.getByTestId('tooltip-title');
    expect(tooltipTitle).toHaveTextContent('A');
    expect(tooltipTitle).toHaveTextContent('B');
    expect(tooltipTitle).toHaveTextContent('C');
  });
});
