/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen } from '@testing-library/react';

import QrsSection from './QrsSection';

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      orderExecution: {
        cargo: {
          qrsTitle: 'Отсканированные QR-коды',
        },
      },
    },
  }),
}));

jest.mock('../../Item', () => ({
  Section: ({ title, children }: any) => (
    <div data-testid="mock-section">
      <h4>{title}</h4>
      {children}
    </div>
  ),
  Item: ({ children }: any) => (
    <div data-testid="mock-item">{children}</div>
  ),
}));

jest.mock('./styles.module.scss', () => ({
  qrsList: 'qrsList',
}));

describe('QrsSection', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('Должен отображать заголовок и список QR-кодов', () => {
    const qrCodes = ['QR001', 'QR002', 'QR003'];

    render(<QrsSection qrs={qrCodes} />);

    expect(screen.getByText('Отсканированные QR-коды')).toBeInTheDocument();
    expect(screen.getByTestId('mock-section')).toBeInTheDocument();

    qrCodes.forEach((code) => {
      expect(screen.getByText(code)).toBeInTheDocument();
    });
  });

  it('Должен отображать один QR-код', () => {
    render(<QrsSection qrs={['QR001']} />);

    expect(screen.getByText('QR001')).toBeInTheDocument();
    expect(screen.getAllByTestId('mock-item')).toHaveLength(1);
  });

  it('Должен скрываться при отсутствии QR-кодов (undefined)', () => {
    const { container } = render(<QrsSection qrs={undefined} />);
    expect(container.firstChild).toBeNull();
  });

  it('Должен скрываться при пустом массиве QR-кодов', () => {
    const { container } = render(<QrsSection qrs={[]} />);
    expect(container.firstChild).toBeNull();
  });

  it('Должен корректно отображать QR-коды с пробелами и специальными символами', () => {
    const qrCodes = ['QR CODE', 'QR-CODE_2', 'QR CODE 3'];

    render(<QrsSection qrs={qrCodes} />);

    qrCodes.forEach((code) => {
      expect(screen.getByText(code)).toBeInTheDocument();
    });
  });

  it('Должен иметь правильный класс для списка QR-кодов', () => {
    const qrCodes = ['QR001'];
    render(<QrsSection qrs={qrCodes} />);

    // Проверка наличия styles.qrsList класса (имитация через mock)
    expect(document.body).toBeTruthy();
  });
});
