import React from 'react';
import { render, screen } from '@testing-library/react';
import ChartCard from '../ChartCard';

jest.mock('assets/icons/help.svg', () => 'help-icon.png');

describe('ChartCard', () => {
  // 1. Базовый рендеринг
  describe('Basic rendering', () => {
    it('should render title and children', () => {
      render(
        <ChartCard title="Sales Chart">
          <div data-testid="chart-content">Chart data</div>
        </ChartCard>
      );

      expect(screen.getByText('Sales Chart')).toBeInTheDocument();
      expect(screen.getByTestId('chart-content')).toBeInTheDocument();
    });

    it('should render container div', () => {
      const { container } = render(
        <ChartCard title="Test">
          Content
        </ChartCard>
      );

      const cardElement = container.firstChild;
      expect(cardElement).toBeInTheDocument();
      expect(cardElement?.nodeName).toBe('DIV');
    });
  });

  // 2. Тесты с tooltip
  describe('Tooltip', () => {
    it('should render help icon when tooltipProps is provided', () => {
      const tooltipProps = {
        title: 'This is a helpful tooltip',
        placement: 'top' as const,
      };

      render(
        <ChartCard title="Chart" tooltipProps={tooltipProps}>
          Content
        </ChartCard>
      );

      const helpIcon = screen.getByAltText('help');
      expect(helpIcon).toBeInTheDocument();
      expect(helpIcon).toHaveAttribute('src', 'help-icon.png');
    });

    it('should NOT render help icon when tooltipProps is not provided', () => {
      render(
        <ChartCard title="Chart">
          Content
        </ChartCard>
      );

      expect(screen.queryByAltText('help')).not.toBeInTheDocument();
    });

    it('should pass tooltipProps to Tooltip component', () => {
      const tooltipProps = {
        title: 'Custom tooltip',
        color: 'red',
        placement: 'right' as const,
      };

      render(
        <ChartCard title="Chart" tooltipProps={tooltipProps}>
          Content
        </ChartCard>
      );

      const helpIcon = screen.getByAltText('help');
      expect(helpIcon).toBeInTheDocument();
    });
  });

  // 3. Тесты с actions
  describe('Actions', () => {
    it('should render actions section when actions prop is provided', () => {
      const actions = (
        <div data-testid="chart-actions">
          <button>Refresh</button>
          <button>Export</button>
        </div>
      );

      render(
        <ChartCard title="Chart" actions={actions}>
          Content
        </ChartCard>
      );

      expect(screen.getByTestId('chart-actions')).toBeInTheDocument();
      expect(screen.getByText('Refresh')).toBeInTheDocument();
      expect(screen.getByText('Export')).toBeInTheDocument();
    });

    it('should NOT render actions when actions prop is not provided', () => {
      render(
        <ChartCard title="Chart">
          Content
        </ChartCard>
      );

      const buttons = screen.queryAllByRole('button');
      expect(buttons.length).toBe(0);
      expect(screen.getByText('Chart')).toBeInTheDocument();
    });
  });

  // 4. Тесты с className
  describe('Custom className', () => {
    it('should apply custom className to container', () => {
      const customClass = 'my-custom-chart-card';

      const { container } = render(
        <ChartCard
          title="Chart"
          className={customClass}
        >
          Content
        </ChartCard>
      );

      const cardElement = container.firstChild as HTMLElement;
      expect(cardElement.className).toContain(customClass);
    });

    it('should handle multiple class names', () => {
      const { container } = render(
        <ChartCard
          title="Chart"
          className="class1 class2 class3"
        >
          Content
        </ChartCard>
      );

      const cardElement = container.firstChild as HTMLElement;
      const classNames = cardElement.className.split(' ');
      expect(classNames).toContain('class1');
      expect(classNames).toContain('class2');
      expect(classNames).toContain('class3');
    });

    it('should render without className prop', () => {
      render(
        <ChartCard title="Chart">
          Content
        </ChartCard>
      );

      expect(screen.getByText('Chart')).toBeInTheDocument();
      expect(screen.getByText('Content')).toBeInTheDocument();
    });
  });

  // 5. Тесты с children
  describe('Children rendering', () => {
    it('should render string children', () => {
      render(
        <ChartCard title="Chart">
          Simple text content
        </ChartCard>
      );

      expect(screen.getByText('Simple text content')).toBeInTheDocument();
    });

    it('should render React element children', () => {
      render(
        <ChartCard title="Chart">
          <div data-testid="chart-wrapper">
            <h3>Chart Title</h3>
            <canvas data-testid="chart-canvas" />
          </div>
        </ChartCard>
      );

      expect(screen.getByTestId('chart-wrapper')).toBeInTheDocument();
      expect(screen.getByTestId('chart-canvas')).toBeInTheDocument();
    });

    it('should render multiple children', () => {
      render(
        <ChartCard title="Chart">
          <div>Header</div>
          <div>Body</div>
          <div>Footer</div>
        </ChartCard>
      );

      expect(screen.getByText('Header')).toBeInTheDocument();
      expect(screen.getByText('Body')).toBeInTheDocument();
      expect(screen.getByText('Footer')).toBeInTheDocument();
    });

    it('should handle null children', () => {
      render(
        <ChartCard title="Chart">
          {null}
        </ChartCard>
      );

      expect(screen.getByText('Chart')).toBeInTheDocument();
    });
  });

  // 6. Комплексные тесты
  describe('Complex scenarios', () => {
    it('should render with all props simultaneously', () => {
      const tooltipProps = {
        title: 'Help information about this chart',
        placement: 'bottom' as const,
      };

      const actions = (
        <div>
          <button data-testid="refresh-btn">Refresh</button>
          <button data-testid="settings-btn">Settings</button>
        </div>
      );

      render(
        <ChartCard
          title="Monthly Revenue"
          tooltipProps={tooltipProps}
          actions={actions}
          className="dashboard-chart-card"
        >
          <div data-testid="revenue-chart">
            Revenue chart visualization
          </div>
        </ChartCard>
      );

      expect(screen.getByText('Monthly Revenue')).toBeInTheDocument();
      expect(screen.getByAltText('help')).toBeInTheDocument();
      expect(screen.getByTestId('refresh-btn')).toBeInTheDocument();
      expect(screen.getByTestId('settings-btn')).toBeInTheDocument();
      expect(screen.getByTestId('revenue-chart')).toBeInTheDocument();
    });
  });

  // 7. Edge cases
  describe('Edge cases', () => {
    it('should handle very long titles', () => {
      const longTitle = 'Very long chart title that might wrap or need truncation in some scenarios';

      render(
        <ChartCard title={longTitle}>
          Content
        </ChartCard>
      );

      expect(screen.getByText(longTitle)).toBeInTheDocument();
    });

    it('should handle empty title string', () => {
      const { container } = render(
        <ChartCard title="">
          Content
        </ChartCard>
      );

      const pElement = container.querySelector('p');
      expect(pElement).toBeInTheDocument();
      expect(pElement?.textContent).toBe('');
    });

    it('should handle special characters in title', () => {
      const specialTitle = 'Chart with $pecial ©haracters & symbols > <';

      render(
        <ChartCard title={specialTitle}>
          Content
        </ChartCard>
      );

      expect(screen.getByText(specialTitle)).toBeInTheDocument();
    });
  });

  // 8. Интеграционные тесты
  describe('Integration tests', () => {
    it('should combine custom classes with module classes', () => {
      const { container } = render(
        <ChartCard
          title="Test"
          className="additional-class"
        >
          Content
        </ChartCard>
      );

      const cardElement = container.firstChild as HTMLElement;
      expect(cardElement.className).toContain('additional-class');
    });

    it('should render tooltip wrapper around help icon', () => {
      render(
        <ChartCard
          title="Test"
          tooltipProps={{ title: 'Help' }}
        >
          Content
        </ChartCard>
      );

      const helpIcon = screen.getByAltText('help');
      expect(helpIcon).toBeInTheDocument();
    });
  });
});
