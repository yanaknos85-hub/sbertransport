import { render, screen } from '@testing-library/react';
import React from 'react';
import { TitleSection } from '../components/TitleSection/TitleSection';
import userEvent from '@testing-library/user-event';
import { useHistory } from 'react-router-dom';

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: jest.fn(),
}));

describe(TitleSection.name, () => {
  const ORDER_NUMBER_MOCK = '123';

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should be defined', () => {
    expect(TitleSection).toBeDefined();
  });

  it('should render correct order number', () => {
    render(<TitleSection>{ORDER_NUMBER_MOCK}</TitleSection>);

    expect(screen.getByText(new RegExp(ORDER_NUMBER_MOCK))).toBeInTheDocument();
  });

  it('should goBack when clicked in back arrow', async () => {
    const goBackMock = jest.fn();

    (useHistory as jest.Mock).mockReturnValue({
      goBack: goBackMock,
    });

    render(<TitleSection>{ORDER_NUMBER_MOCK}</TitleSection>);

    expect(goBackMock).not.toHaveBeenCalled();

    await userEvent.click(screen.getByTestId(/arrow-left/));
    expect(goBackMock).toHaveBeenCalledTimes(1);
  });
});
