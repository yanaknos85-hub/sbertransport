import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { translation } from 'i18n/ru';
import React from 'react';
import { FraudSection } from '../components/FraudSection/FraudSection';
import { fraudItemsMock } from './mocks';

describe(FraudSection.name, () => {
  const renderFraudSection = () => render(<FraudSection fraudItems={fraudItemsMock} />);

  it('should be defined', () => {
    expect(FraudSection).toBeDefined();
  });

  it('should render with correct title', () => {
    renderFraudSection();

    expect(screen.getByText(new RegExp(translation.fraudMonitoring.details.fraudSectionTitle))).toBeInTheDocument();
  });

  it('should show fraud comments by default', () => {
    renderFraudSection();

    expect(screen.getByTestId('fraud-section-body')).toBeInTheDocument();
  });

  it('should hide fraud comments when clicked on header', async () => {
    renderFraudSection();

    await userEvent.click(screen.getByTestId('fraud-section-header'));

    expect(screen.queryByTestId('fraud-section-body')).not.toBeInTheDocument();
  });

  it('should render correct number of fraud comments', () => {
    renderFraudSection();

    expect(screen.getAllByTestId('fraud-list-item').length).toBe(fraudItemsMock.length);
  });
});
