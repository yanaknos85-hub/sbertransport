import { render, screen, within } from '@testing-library/react';
import { InformationSection } from '../components/InformationSection/InformationSection';
import React from 'react';
import {
  fraudDetailsResponseMock, groupsMock, groupsWithNoFieldsMock, groupsWithNullValueMock
} from './mocks';
import { useInformationGroups } from '../components/InformationSection/hooks/useInformationGroups';
import { VALUE_NOT_FOUND } from 'constants/constants.app';

jest.mock('../components/InformationSection/hooks/useInformationGroups', () => ({
  useInformationGroups: jest.fn(),
}));

describe(InformationSection.name, () => {
  const renderInformationSection = () => render(<InformationSection fraudDetails={fraudDetailsResponseMock} />);
  const mockUseInformationGroups = (mock?: ReturnType<typeof useInformationGroups>) => {
    (useInformationGroups as jest.Mock).mockReturnValue(mock ?? groupsMock);
  };

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should be defined', () => {
    expect(InformationSection).toBeDefined();
  });

  it('should render correct count of blocks', () => {
    mockUseInformationGroups();
    renderInformationSection();

    expect(screen.getAllByTestId('information-block').length).toBe(groupsMock.length);
  });

  it('should render correct title of block', () => {
    mockUseInformationGroups();
    renderInformationSection();

    expect(screen.getByText(new RegExp(`^${groupsMock[0].groupName}$`))).toBeInTheDocument();
  });

  it('should render correct count of fields', () => {
    mockUseInformationGroups();
    renderInformationSection();

    const desiredGroup = groupsMock[0];
    const firstGroupContainer = screen.getAllByTestId('information-block')[0];

    expect(within(firstGroupContainer).getAllByTestId('information-block-field').length).toBe(desiredGroup.fields.length);
  });

  it('should not render block if there are no fields in it', () => {
    mockUseInformationGroups(groupsWithNoFieldsMock);
    renderInformationSection();

    expect(screen.queryByTestId('information-block')).not.toBeInTheDocument();
  });

  it('should render - if there is no value in field', () => {
    mockUseInformationGroups(groupsWithNullValueMock);
    renderInformationSection();

    expect(screen.getByTestId('information-block-field-value').textContent).toBe(VALUE_NOT_FOUND);
  });
});
