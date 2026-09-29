import { render, screen } from '@testing-library/react';
import React from 'react';

import { QrCodesSection } from './QrCodesSection';

describe('QrCodesSection', () => {
  it('should render QR codes list', () => {
    render(<QrCodesSection qrs={['QR001', 'QR002', 'QR003']} />);

    expect(screen.getByText('Отсканированные QR-коды')).toBeInTheDocument();

    expect(screen.getByText('QR001')).toBeInTheDocument();
    expect(screen.getByText('QR002')).toBeInTheDocument();
    expect(screen.getByText('QR003')).toBeInTheDocument();
  });

  it('should render single QR code', () => {
    render(<QrCodesSection qrs={['QR001']} />);

    expect(screen.getByText('QR001')).toBeInTheDocument();
  });

  it('should not render when qrs is empty array', () => {
    render(<QrCodesSection qrs={[]} />);

    expect(screen.queryByText('Отсканированные QR-коды')).not.toBeInTheDocument();
  });

  it('should not render when qrs is undefined', () => {
    render(<QrCodesSection />);

    expect(screen.queryByText('Отсканированные QR-коды')).not.toBeInTheDocument();
  });

  it('should render QR codes as separate items', () => {
    const qrs = ['QR-ABC-123', 'QR-XYZ-789'];
    render(<QrCodesSection qrs={qrs} />);

    expect(screen.getByText('QR-ABC-123')).toBeInTheDocument();
    expect(screen.getByText('QR-XYZ-789')).toBeInTheDocument();
  });
});
