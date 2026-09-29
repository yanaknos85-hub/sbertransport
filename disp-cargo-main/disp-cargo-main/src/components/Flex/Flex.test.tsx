import React from 'react';
import { render, screen } from '@testing-library/react';
import Flex from './Flex';

describe('Flex Component', () => {
  it('should render children correctly', () => {
    render(
      <Flex>
        <span>Child content</span>
      </Flex>
    );
    expect(screen.getByText('Child content')).toBeInTheDocument();
  });

  it('should apply default flex direction (row)', () => {
    const { container } = render(<Flex>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('flex-direction: row');
  });

  it('should apply column direction when specified', () => {
    const { container } = render(<Flex direction="column">Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('flex-direction: column');
  });

  it('should apply default justify-content (flex-start)', () => {
    const { container } = render(<Flex>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('justify-content: flex-start');
  });

  it('should apply justify-content when specified', () => {
    const { container } = render(<Flex justifyContent="center">Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('justify-content: center');
  });

  it('should apply justify-content space-between when specified', () => {
    const { container } = render(<Flex justifyContent="space-between">Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('justify-content: space-between');
  });

  it('should apply default align-items (start)', () => {
    const { container } = render(<Flex>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('align-items: start');
  });

  it('should apply align-items when specified', () => {
    const { container } = render(<Flex alignItems="center">Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('align-items: center');
  });

  it('should apply default gap (10px)', () => {
    const { container } = render(<Flex>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('gap: 10px');
  });

  it('should apply custom gap when specified', () => {
    const { container } = render(<Flex gap={20}>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('gap: 20px');
  });

  it('should not apply margin-bottom by default', () => {
    const { container } = render(<Flex>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('margin-bottom: 0px');
  });

  it('should apply margin-bottom when specified', () => {
    const { container } = render(<Flex marginBottom>Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveStyle('margin-bottom: 16px');
  });

  it('should render with custom className', () => {
    const { container } = render(<Flex className="custom-class">Content</Flex>);
    const flexContainer = container.firstChild as HTMLElement;
    expect(flexContainer).toHaveClass('custom-class');
  });

  it('should handle multiple children', () => {
    render(
      <Flex>
        <span>First</span>
        <span>Second</span>
        <span>Third</span>
      </Flex>
    );
    expect(screen.getByText('First')).toBeInTheDocument();
    expect(screen.getByText('Second')).toBeInTheDocument();
    expect(screen.getByText('Third')).toBeInTheDocument();
  });
});
