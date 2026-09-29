import React from 'react';
import { render, screen } from '@testing-library/react';
import { useTranslation } from 'i18n';
import VehicleAnalytics from '../VehicleAnalytics';

// Mock data
const mockVehicleAnalyticsData = [
  {
    month: 1, inExploitationСount: 10, notInExploitationСount: 2,
  },
  {
    month: 2, inExploitationСount: 15, notInExploitationСount: 3,
  },
  {
    month: 3, inExploitationСount: 8, notInExploitationСount: 1,
  },
];

const mockBrandsStatisticsData = [
  {
    month: 1,
    totalCount: 12,
    brands: [
      { brand: 'Brand A', count: 5 },
      { brand: 'Brand B', count: 3 },
      { brand: 'Brand C', count: 2 },
    ],
  },
  {
    month: 2,
    totalCount: 18,
    brands: [
      { brand: 'Brand A', count: 7 },
      { brand: 'Brand B', count: 5 },
      { brand: 'Brand C', count: 3 },
    ],
  },
  {
    month: 3,
    totalCount: 9,
    brands: [
      { brand: 'Brand A', count: 4 },
      { brand: 'Brand B', count: 2 },
      { brand: 'Brand C', count: 2 },
    ],
  },
];

// Mock translation
const mockT = {
  Analytics: {
    Vehicle: {
      title: 'Vehicle Analytics',
      tooltipText: 'Vehicle tooltip',
    },
  },
};

// Mock external dependencies
jest.mock('i18n');
jest.mock('api/analytics/analytics.api', () => ({
  useVehicleAnalytics: () => ({
    data: mockVehicleAnalyticsData,
  }),
  useBrandsStatistics: () => ({
    data: mockBrandsStatisticsData,
  }),
}));
jest.mock('../../../context/AnalyticsQuery', () => ({
  useAnalyticsQuery: () => ({
    query: {
      year: 2023,
    },
  }),
}));

describe('VehicleAnalytics', () => {
  beforeEach(() => {
    (useTranslation as jest.Mock).mockReturnValue({ t: mockT });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('renders the component with correct title and tooltip', () => {
    render(<VehicleAnalytics />);

    // Check if title is rendered
    expect(screen.getByText('Vehicle Analytics')).toBeInTheDocument();

    // Check if help icon is rendered (tooltip)
    const helpIcon = screen.getByAltText('help');
    expect(helpIcon).toBeInTheDocument();
  });

  test('renders BarChart with correct data and props', () => {
    render(<VehicleAnalytics />);

    // Check if BarChart is rendered
    const barChartContainer = screen.getByText('Янв').closest('svg')?.closest('div');
    expect(barChartContainer).toBeInTheDocument();

    // Check that the chart has the correct number of bars (12 months)
    const bars = barChartContainer?.querySelectorAll('g');
    expect(bars?.length).toBe(12);

    // Check that the first three months have correct data
    const firstBarText = bars?.[0].querySelector('text:last-of-type');
    expect(firstBarText).toHaveTextContent('Янв');

    const secondBarText = bars?.[1].querySelector('text:last-of-type');
    expect(secondBarText).toHaveTextContent('Фев');

    const thirdBarText = bars?.[2].querySelector('text:last-of-type');
    expect(thirdBarText).toHaveTextContent('Март');
  });
});

